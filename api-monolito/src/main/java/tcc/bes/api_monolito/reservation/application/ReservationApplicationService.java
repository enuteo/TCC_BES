package tcc.bes.api_monolito.reservation.application;

import org.springframework.http.HttpStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tcc.bes.api_monolito.identity.application.ManagerIdentity;
import tcc.bes.api_monolito.reservation.domain.ReservationState;
import tcc.bes.api_monolito.shared.error.ApiException;
import tcc.bes.api_monolito.shared.idempotency.IdempotencyService;
import tcc.bes.api_monolito.shared.idempotency.IdempotentResult;
import tcc.bes.api_monolito.shared.observability.DomainMetrics;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static tcc.bes.api_monolito.shared.persistence.JdbcTimestamps.timestamp;

@Service
@ConditionalOnProperty(name = "app.module.reservation.enabled", havingValue = "true", matchIfMissing = true)
public class ReservationApplicationService {

    private final JdbcTemplate jdbcTemplate;
    private final IdempotencyService idempotencyService;
    private final DomainMetrics domainMetrics;
    private final Clock clock;

    public ReservationApplicationService(
            JdbcTemplate jdbcTemplate,
            IdempotencyService idempotencyService,
            DomainMetrics domainMetrics,
            Clock clock
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.idempotencyService = idempotencyService;
        this.domainMetrics = domainMetrics;
        this.clock = clock;
    }

    @Transactional
    public IdempotentResult<ResourceView> createResource(
            ManagerIdentity manager,
            String name,
            int totalQuantity,
            String idempotencyKey
    ) {
        CreateResourceCommand command = new CreateResourceCommand(name, totalQuantity);
        String requestHash = idempotencyService.requireRequestHash(command);
        var replay = idempotencyService.findReplay(
                "resource.create",
                manager.id().toString(),
                idempotencyKey,
                requestHash,
                ResourceView.class
        );
        if (replay.isPresent()) {
            return new IdempotentResult<>(replay.get().statusCode(), replay.get().body());
        }

        UUID resourceId = UUID.randomUUID();
        Instant now = clock.instant();
        jdbcTemplate.update("""
                        INSERT INTO resources
                            (id, manager_id, name, total_quantity, available_quantity, held_quantity, confirmed_quantity, created_at)
                        VALUES (?, ?, ?, ?, ?, 0, 0, ?)
                        """,
                resourceId,
                manager.id(),
                name,
                totalQuantity,
                totalQuantity,
                timestamp(now)
        );

        ResourceView response = getResource(manager, resourceId);
        idempotencyService.save("resource.create", manager.id().toString(), idempotencyKey,
                requestHash, HttpStatus.CREATED.value(), response);
        return new IdempotentResult<>(HttpStatus.CREATED.value(), response);
    }

    public List<ResourceView> listResources(ManagerIdentity manager) {
        return jdbcTemplate.query("""
                        SELECT id, manager_id, name, total_quantity, available_quantity,
                               held_quantity, confirmed_quantity, created_at
                        FROM resources
                        WHERE manager_id = ?
                        ORDER BY created_at, id
                        """,
                this::mapResource,
                manager.id()
        );
    }

    public ResourceView getResource(ManagerIdentity manager, UUID resourceId) {
        return jdbcTemplate.query("""
                        SELECT id, manager_id, name, total_quantity, available_quantity,
                               held_quantity, confirmed_quantity, created_at
                        FROM resources
                        WHERE id = ? AND manager_id = ?
                        """,
                this::mapResource,
                resourceId,
                manager.id()
        ).stream().findFirst().orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "Resource not found."));
    }

    public ResourceView getResourceForInternalUse(UUID resourceId) {
        return jdbcTemplate.query("""
                        SELECT id, manager_id, name, total_quantity, available_quantity,
                               held_quantity, confirmed_quantity, created_at
                        FROM resources
                        WHERE id = ?
                        """,
                this::mapResource,
                resourceId
        ).stream().findFirst().orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "Resource not found."));
    }

    public void assertManagerOwnsResource(ManagerIdentity manager, UUID resourceId) {
        getResource(manager, resourceId);
    }

    @Transactional
    public HoldDecision tryCreateHold(UUID resourceId, UUID entryId, int quantity, Instant expiresAt) {
        var existing = findReservationByEntryId(entryId);
        if (existing != null) {
            return HoldDecision.held(existing);
        }

        StockRow stock = lockStock(resourceId);

        if (stock.availableQuantity() >= quantity) {
            UUID reservationId = UUID.randomUUID();
            Instant now = clock.instant();
            jdbcTemplate.update("""
                            UPDATE resources
                            SET available_quantity = available_quantity - ?,
                                held_quantity = held_quantity + ?
                            WHERE id = ?
                            """,
                    quantity,
                    quantity,
                    resourceId
            );
            jdbcTemplate.update("""
                            INSERT INTO reservations
                                (id, resource_id, entry_id, quantity, expires_at, state, created_at)
                            VALUES (?, ?, ?, ?, ?, ?, ?)
                            """,
                    reservationId,
                    resourceId,
                    entryId,
                    quantity,
                    timestamp(expiresAt),
                            ReservationState.HELD.name(),
                            timestamp(now)
            );
            domainMetrics.reservationCreated();
            return HoldDecision.held(getReservationInternal(reservationId));
        }

        if (stock.availableQuantity() + stock.heldQuantity() >= quantity) {
            return HoldDecision.waitForRecoverableCapacity();
        }

        return HoldDecision.unfulfillable();
    }

    public ReservationView getReservation(UUID reservationId) {
        return getReservationInternal(reservationId);
    }

    @Transactional
    public ReservationView confirm(UUID reservationId) {
        ReservationView current = lockReservation(reservationId);
        if (ReservationState.CONFIRMED.name().equals(current.state())) {
            return current;
        }
        ensureHeld(current);

        Instant now = clock.instant();
        jdbcTemplate.update("""
                        UPDATE resources
                        SET held_quantity = held_quantity - ?,
                            confirmed_quantity = confirmed_quantity + ?
                        WHERE id = ?
                        """,
                current.quantity(),
                current.quantity(),
                current.resourceId()
        );
        jdbcTemplate.update("""
                        UPDATE reservations
                        SET state = ?, confirmed_at = ?
                        WHERE id = ?
                        """,
                ReservationState.CONFIRMED.name(),
                timestamp(now),
                reservationId
        );
        domainMetrics.reservationCompleted(ReservationState.CONFIRMED.name());
        ReservationView reservation = getReservationInternal(reservationId);
        recordTerminalEvent(reservation);
        return reservation;
    }

    @Transactional
    public ReservationView cancel(UUID reservationId) {
        ReservationView current = lockReservation(reservationId);
        if (ReservationState.CANCELLED.name().equals(current.state())) {
            return current;
        }
        ensureHeld(current);
        return releaseHold(reservationId, current, ReservationState.CANCELLED, clock.instant());
    }

    @Transactional
    public List<ReservationView> expireDue(int limit) {
        Instant now = clock.instant();
        List<UUID> ids = jdbcTemplate.query("""
                        SELECT id
                        FROM reservations
                        WHERE state = ? AND expires_at <= ?
                        ORDER BY expires_at, id
                        LIMIT ?
                        """,
                (rs, rowNum) -> rs.getObject("id", UUID.class),
                ReservationState.HELD.name(),
                timestamp(now),
                limit
        );

        return ids.stream()
                .map(id -> expireOne(id, now))
                .toList();
    }

    private ReservationView expireOne(UUID reservationId, Instant now) {
        ReservationView current = lockReservation(reservationId);
        if (!ReservationState.HELD.name().equals(current.state()) || current.expiresAt().isAfter(now)) {
            return current;
        }
        return releaseHold(reservationId, current, ReservationState.EXPIRED, now);
    }

    private ReservationView releaseHold(
            UUID reservationId,
            ReservationView current,
            ReservationState terminalState,
            Instant now
    ) {
        jdbcTemplate.update("""
                        UPDATE resources
                        SET held_quantity = held_quantity - ?,
                            available_quantity = available_quantity + ?
                        WHERE id = ?
                        """,
                current.quantity(),
                current.quantity(),
                current.resourceId()
        );

        if (terminalState == ReservationState.CANCELLED) {
            jdbcTemplate.update("""
                            UPDATE reservations
                            SET state = ?, cancelled_at = ?
                            WHERE id = ?
                            """,
                    terminalState.name(),
                    timestamp(now),
                    reservationId
            );
        } else {
            jdbcTemplate.update("""
                            UPDATE reservations
                            SET state = ?, expired_at = ?
                            WHERE id = ?
                            """,
                    terminalState.name(),
                    timestamp(now),
                    reservationId
            );
        }

        domainMetrics.reservationCompleted(terminalState.name());
        ReservationView reservation = getReservationInternal(reservationId);
        recordTerminalEvent(reservation);
        return reservation;
    }

    public List<ReservationTerminalEventView> listPendingTerminalEvents(int limit) {
        return jdbcTemplate.query("""
                        SELECT id, reservation_id, entry_id, state, occurred_at
                        FROM reservation_terminal_events
                        WHERE acknowledged_at IS NULL
                        ORDER BY occurred_at, id
                        LIMIT ?
                        """,
                this::mapTerminalEvent,
                limit
        );
    }

    @Transactional
    public void acknowledgeTerminalEvents(List<UUID> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return;
        }
        eventIds.forEach(eventId -> jdbcTemplate.update("""
                        UPDATE reservation_terminal_events
                        SET acknowledged_at = ?
                        WHERE id = ?
                        """,
                timestamp(clock.instant()),
                eventId
        ));
    }

    private void ensureHeld(ReservationView reservation) {
        if (!ReservationState.HELD.name().equals(reservation.state())) {
            throw new ApiException(HttpStatus.CONFLICT, "RESERVATION_ALREADY_TERMINAL",
                    "Reservation is already in state " + reservation.state() + ".");
        }
    }

    private StockRow lockStock(UUID resourceId) {
        return jdbcTemplate.query("""
                        SELECT available_quantity, held_quantity, confirmed_quantity
                        FROM resources
                        WHERE id = ?
                        FOR UPDATE
                        """,
                (rs, rowNum) -> new StockRow(
                        rs.getInt("available_quantity"),
                        rs.getInt("held_quantity"),
                        rs.getInt("confirmed_quantity")
                ),
                resourceId
        ).stream().findFirst().orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "Resource not found."));
    }

    private ReservationView lockReservation(UUID reservationId) {
        return jdbcTemplate.query("""
                        SELECT id, resource_id, entry_id, quantity, expires_at, state, created_at,
                               confirmed_at, cancelled_at, expired_at
                        FROM reservations
                        WHERE id = ?
                        FOR UPDATE
                        """,
                this::mapReservation,
                reservationId
        ).stream().findFirst().orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "RESERVATION_NOT_FOUND", "Reservation not found."));
    }

    private ReservationView getReservationInternal(UUID reservationId) {
        return jdbcTemplate.query("""
                        SELECT id, resource_id, entry_id, quantity, expires_at, state, created_at,
                               confirmed_at, cancelled_at, expired_at
                        FROM reservations
                        WHERE id = ?
                        """,
                this::mapReservation,
                reservationId
        ).stream().findFirst().orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "RESERVATION_NOT_FOUND", "Reservation not found."));
    }

    private ReservationView findReservationByEntryId(UUID entryId) {
        return jdbcTemplate.query("""
                        SELECT id, resource_id, entry_id, quantity, expires_at, state, created_at,
                               confirmed_at, cancelled_at, expired_at
                        FROM reservations
                        WHERE entry_id = ?
                        """,
                this::mapReservation,
                entryId
        ).stream().findFirst().orElse(null);
    }

    private void recordTerminalEvent(ReservationView reservation) {
        jdbcTemplate.update("""
                        INSERT INTO reservation_terminal_events
                            (id, reservation_id, entry_id, state, occurred_at)
                        VALUES (?, ?, ?, ?, ?)
                        ON CONFLICT (reservation_id) DO NOTHING
                        """,
                UUID.randomUUID(),
                reservation.id(),
                reservation.entryId(),
                reservation.state(),
                timestamp(terminalInstant(reservation))
        );
    }

    private Instant terminalInstant(ReservationView reservation) {
        if (reservation.confirmedAt() != null) {
            return reservation.confirmedAt();
        }
        if (reservation.cancelledAt() != null) {
            return reservation.cancelledAt();
        }
        if (reservation.expiredAt() != null) {
            return reservation.expiredAt();
        }
        return clock.instant();
    }

    private ResourceView mapResource(ResultSet rs, int rowNum) throws SQLException {
        return new ResourceView(
                rs.getObject("id", UUID.class),
                rs.getObject("manager_id", UUID.class),
                rs.getString("name"),
                rs.getInt("total_quantity"),
                rs.getInt("available_quantity"),
                rs.getInt("held_quantity"),
                rs.getInt("confirmed_quantity"),
                rs.getTimestamp("created_at").toInstant()
        );
    }

    private ReservationView mapReservation(ResultSet rs, int rowNum) throws SQLException {
        return new ReservationView(
                rs.getObject("id", UUID.class),
                rs.getObject("resource_id", UUID.class),
                rs.getObject("entry_id", UUID.class),
                rs.getInt("quantity"),
                rs.getTimestamp("expires_at").toInstant(),
                rs.getString("state"),
                rs.getTimestamp("created_at").toInstant(),
                timestampOrNull(rs, "confirmed_at"),
                timestampOrNull(rs, "cancelled_at"),
                timestampOrNull(rs, "expired_at")
        );
    }

    private ReservationTerminalEventView mapTerminalEvent(ResultSet rs, int rowNum) throws SQLException {
        return new ReservationTerminalEventView(
                rs.getObject("id", UUID.class),
                rs.getObject("reservation_id", UUID.class),
                rs.getObject("entry_id", UUID.class),
                rs.getString("state"),
                rs.getTimestamp("occurred_at").toInstant()
        );
    }

    private Instant timestampOrNull(ResultSet rs, String column) throws SQLException {
        var timestamp = rs.getTimestamp(column);
        return timestamp == null ? null : timestamp.toInstant();
    }

    private record CreateResourceCommand(String name, int totalQuantity) {
    }

    private record StockRow(int availableQuantity, int heldQuantity, int confirmedQuantity) {
    }
}
