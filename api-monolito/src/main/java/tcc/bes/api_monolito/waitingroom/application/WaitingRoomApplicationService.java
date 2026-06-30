package tcc.bes.api_monolito.waitingroom.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tcc.bes.api_monolito.identity.application.IdentityPort;
import tcc.bes.api_monolito.identity.application.ManagerIdentity;
import tcc.bes.api_monolito.queuemanagement.application.QueueJoinSlot;
import tcc.bes.api_monolito.queuemanagement.application.QueueManagementPort;
import tcc.bes.api_monolito.queuemanagement.application.QueueView;
import tcc.bes.api_monolito.queuemanagement.domain.QueueState;
import tcc.bes.api_monolito.reservation.application.HoldDecision;
import tcc.bes.api_monolito.reservation.application.ReservationPort;
import tcc.bes.api_monolito.reservation.application.ReservationTerminalEventView;
import tcc.bes.api_monolito.reservation.application.ReservationView;
import tcc.bes.api_monolito.shared.error.ApiException;
import tcc.bes.api_monolito.shared.idempotency.IdempotencyService;
import tcc.bes.api_monolito.shared.idempotency.IdempotentResult;
import tcc.bes.api_monolito.shared.observability.DomainMetrics;
import tcc.bes.api_monolito.shared.security.BearerTokens;
import tcc.bes.api_monolito.shared.security.HashingService;
import tcc.bes.api_monolito.shared.security.TokenGenerator;
import tcc.bes.api_monolito.waitingroom.domain.EntryState;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static tcc.bes.api_monolito.shared.persistence.JdbcTimestamps.timestamp;

@Service
@ConditionalOnProperty(name = "app.module.waiting-room.enabled", havingValue = "true", matchIfMissing = true)
public class WaitingRoomApplicationService {

    private final JdbcTemplate jdbcTemplate;
    private final QueueManagementPort queueManagementPort;
    private final ReservationPort reservationPort;
    private final IdempotencyService idempotencyService;
    private final TokenGenerator tokenGenerator;
    private final HashingService hashingService;
    private final DomainMetrics domainMetrics;
    private final BearerTokens bearerTokens;
    private final IdentityPort identityPort;
    private final Clock clock;
    private final int entryTokenBytes;

    public WaitingRoomApplicationService(
            JdbcTemplate jdbcTemplate,
            QueueManagementPort queueManagementPort,
            ReservationPort reservationPort,
            IdempotencyService idempotencyService,
            TokenGenerator tokenGenerator,
            HashingService hashingService,
            DomainMetrics domainMetrics,
            BearerTokens bearerTokens,
            IdentityPort identityPort,
            Clock clock,
            @Value("${app.security.entry-token-bytes}") int entryTokenBytes
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.queueManagementPort = queueManagementPort;
        this.reservationPort = reservationPort;
        this.idempotencyService = idempotencyService;
        this.tokenGenerator = tokenGenerator;
        this.hashingService = hashingService;
        this.domainMetrics = domainMetrics;
        this.bearerTokens = bearerTokens;
        this.identityPort = identityPort;
        this.clock = clock;
        this.entryTokenBytes = entryTokenBytes;
    }

    @Transactional
    public IdempotentResult<EntryJoinResponse> join(
            UUID queueId,
            String participantKey,
            int quantity,
            String idempotencyKey
    ) {
        String participantHash = hashingService.sha256(participantKey);
        JoinEntryCommand command = new JoinEntryCommand(queueId, participantHash, quantity);
        String requestHash = idempotencyService.requireRequestHash(command);
        String actor = queueId + ":" + participantHash;
        var replay = idempotencyService.findReplay(
                "entry.join",
                actor,
                idempotencyKey,
                requestHash,
                EntryJoinResponse.class
        );
        if (replay.isPresent()) {
            return new IdempotentResult<>(replay.get().statusCode(), replay.get().body());
        }

        QueueJoinSlot slot = queueManagementPort.allocateSequenceForJoin(queueId, quantity);
        String token = tokenGenerator.generate(entryTokenBytes);
        UUID entryId = UUID.randomUUID();
        Instant now = clock.instant();
        try {
            jdbcTemplate.update("""
                            INSERT INTO entries
                                (id, queue_id, resource_id, participant_hash, quantity, sequence,
                                 state, token_hash, created_at)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """,
                    entryId,
                    queueId,
                    slot.resourceId(),
                    participantHash,
                    quantity,
                    slot.sequence(),
                    EntryState.WAITING.name(),
                    hashingService.sha256(token),
                    timestamp(now)
            );
            domainMetrics.entryCreated();
        } catch (DataIntegrityViolationException ex) {
            throw new ApiException(HttpStatus.CONFLICT, "ENTRY_ALREADY_ACTIVE",
                    "Participant already has a non-terminal entry in this queue.");
        }

        EntryRow row = getEntryRow(entryId);
        EntryJoinResponse response = new EntryJoinResponse(
                row.id(),
                row.queueId(),
                row.state(),
                row.quantity(),
                calculatePosition(row),
                row.sequence(),
                row.createdAt(),
                token
        );
        idempotencyService.save("entry.join", actor, idempotencyKey, requestHash,
                HttpStatus.CREATED.value(), response);
        return new IdempotentResult<>(HttpStatus.CREATED.value(), response);
    }

    public EntryView getEntry(UUID entryId, String authorizationHeader) {
        EntryRow row = authorizeEntry(entryId, authorizationHeader, true);
        return toView(row);
    }

    @Transactional
    public EntryView cancelEntry(UUID entryId, String authorizationHeader) {
        EntryRow row = lockEntry(entryId);
        authorizeEntry(row, authorizationHeader, true);

        if (EntryState.CANCELLED.name().equals(row.state())) {
            return toView(getEntryRow(entryId));
        }
        if (EntryState.WAITING.name().equals(row.state())) {
            markTerminal(entryId, EntryState.CANCELLED);
            return toView(getEntryRow(entryId));
        }
        if (EntryState.HOLD_GRANTED.name().equals(row.state()) && row.reservationId() != null) {
            ReservationView reservation = reservationPort.cancel(row.reservationId());
            markReservationCancelled(reservation.entryId());
            return toView(getEntryRow(entryId));
        }

        throw new ApiException(HttpStatus.CONFLICT, "ENTRY_ALREADY_TERMINAL",
                "Entry is already in state " + row.state() + ".");
    }

    @Transactional
    public IdempotentResult<ReservationView> confirmReservation(
            UUID reservationId,
            String authorizationHeader,
            String idempotencyKey
    ) {
        EntryRow row = authorizeReservationAccess(reservationId, authorizationHeader, false);
        ConfirmReservationCommand command = new ConfirmReservationCommand(reservationId);
        String requestHash = idempotencyService.requireRequestHash(command);
        var replay = idempotencyService.findReplay(
                "reservation.confirm",
                reservationId.toString(),
                idempotencyKey,
                requestHash,
                ReservationView.class
        );
        if (replay.isPresent()) {
            return new IdempotentResult<>(replay.get().statusCode(), replay.get().body());
        }

        ReservationView reservation = reservationPort.confirm(reservationId);
        markReservationConfirmed(row.id());
        idempotencyService.save("reservation.confirm", reservationId.toString(), idempotencyKey,
                requestHash, HttpStatus.OK.value(), reservation);
        return new IdempotentResult<>(HttpStatus.OK.value(), reservation);
    }

    @Transactional
    public ReservationView cancelReservation(UUID reservationId, String authorizationHeader) {
        EntryRow row = authorizeReservationAccess(reservationId, authorizationHeader, true);
        ReservationView reservation = reservationPort.cancel(reservationId);
        markReservationCancelled(row.id());
        return reservation;
    }

    public ReservationView getReservation(UUID reservationId, String authorizationHeader) {
        authorizeReservationAccess(reservationId, authorizationHeader, true);
        return reservationPort.getReservation(reservationId);
    }

    @Transactional
    public void processQueue(UUID queueId) {
        QueueView queue = queueManagementPort.getQueueInternal(queueId);
        if (!QueueState.OPEN.name().equals(queue.state())) {
            domainMetrics.workerCycle("skipped");
            return;
        }

        List<EntryRow> entries = jdbcTemplate.query("""
                        SELECT id, queue_id, resource_id, participant_hash, quantity, sequence,
                               state, reservation_id, token_hash, created_at, hold_granted_at, terminal_at
                        FROM entries
                        WHERE queue_id = ? AND state = ?
                        ORDER BY sequence
                        LIMIT ?
                        FOR UPDATE SKIP LOCKED
                        """,
                this::mapEntry,
                queueId,
                EntryState.WAITING.name(),
                queue.maxBatchSize()
        );

        for (EntryRow entry : entries) {
            Instant expiresAt = clock.instant().plusSeconds(queue.holdDurationSeconds());
            HoldDecision decision = reservationPort.tryCreateHold(
                    queue.resourceId(),
                    entry.id(),
                    entry.quantity(),
                    expiresAt
            );

            if (decision.result() == HoldDecision.Result.HELD) {
                jdbcTemplate.update("""
                                UPDATE entries
                                SET state = ?, reservation_id = ?, hold_granted_at = ?
                                WHERE id = ? AND state = ?
                                """,
                        EntryState.HOLD_GRANTED.name(),
                        decision.reservation().id(),
                        timestamp(clock.instant()),
                        entry.id(),
                        EntryState.WAITING.name()
                );
            } else if (decision.result() == HoldDecision.Result.WAIT_FOR_RECOVERABLE_CAPACITY) {
                break;
            } else {
                markTerminal(entry.id(), EntryState.UNFULFILLABLE);
            }
        }
        domainMetrics.workerCycle("success");
    }

    @Transactional
    public void applyReservationTerminalEvent(ReservationTerminalEventView event) {
        if ("CONFIRMED".equals(event.state())) {
            markReservationTerminalFromEvent(event.entryId(), event.reservationId(), EntryState.CONFIRMED);
        } else if ("CANCELLED".equals(event.state())) {
            markReservationTerminalFromEvent(event.entryId(), event.reservationId(), EntryState.CANCELLED);
        } else if ("EXPIRED".equals(event.state())) {
            markReservationTerminalFromEvent(event.entryId(), event.reservationId(), EntryState.EXPIRED);
        }
    }

    @Transactional
    public void cancelWaitingEntriesForClosedQueue(UUID queueId) {
        int updated = jdbcTemplate.update("""
                        UPDATE entries
                        SET state = ?, terminal_at = ?
                        WHERE queue_id = ? AND state = ?
                        """,
                EntryState.CANCELLED.name(),
                timestamp(clock.instant()),
                queueId,
                EntryState.WAITING.name()
        );
        if (updated > 0) {
            for (int i = 0; i < updated; i++) {
                domainMetrics.entryCompleted(EntryState.CANCELLED.name());
            }
        }
    }

    @Transactional
    public void markReservationConfirmed(UUID entryId) {
        int updated = jdbcTemplate.update("""
                        UPDATE entries
                        SET state = ?, terminal_at = ?
                        WHERE id = ? AND state = ?
                        """,
                EntryState.CONFIRMED.name(),
                timestamp(clock.instant()),
                entryId,
                EntryState.HOLD_GRANTED.name()
        );
        if (updated > 0) {
            domainMetrics.entryCompleted(EntryState.CONFIRMED.name());
        }
    }

    @Transactional
    public void markReservationCancelled(UUID entryId) {
        int updated = jdbcTemplate.update("""
                        UPDATE entries
                        SET state = ?, terminal_at = ?
                        WHERE id = ? AND state = ?
                        """,
                EntryState.CANCELLED.name(),
                timestamp(clock.instant()),
                entryId,
                EntryState.HOLD_GRANTED.name()
        );
        if (updated > 0) {
            domainMetrics.entryCompleted(EntryState.CANCELLED.name());
        }
    }

    @Transactional
    public void markReservationExpired(UUID entryId) {
        int updated = jdbcTemplate.update("""
                        UPDATE entries
                        SET state = ?, terminal_at = ?
                        WHERE id = ? AND state = ?
                        """,
                EntryState.EXPIRED.name(),
                timestamp(clock.instant()),
                entryId,
                EntryState.HOLD_GRANTED.name()
        );
        if (updated > 0) {
            domainMetrics.entryCompleted(EntryState.EXPIRED.name());
        }
    }

    private void markReservationTerminalFromEvent(UUID entryId, UUID reservationId, EntryState terminalState) {
        int updated = jdbcTemplate.update("""
                        UPDATE entries
                        SET state = ?, reservation_id = COALESCE(reservation_id, ?), terminal_at = ?
                        WHERE id = ? AND state IN (?, ?)
                        """,
                terminalState.name(),
                reservationId,
                timestamp(clock.instant()),
                entryId,
                EntryState.WAITING.name(),
                EntryState.HOLD_GRANTED.name()
        );
        if (updated > 0) {
            domainMetrics.entryCompleted(terminalState.name());
        }
    }

    private EntryRow authorizeReservationAccess(
            UUID reservationId,
            String authorizationHeader,
            boolean managerAllowed
    ) {
        EntryRow row = jdbcTemplate.query("""
                        SELECT id, queue_id, resource_id, participant_hash, quantity, sequence,
                               state, reservation_id, token_hash, created_at, hold_granted_at, terminal_at
                        FROM entries
                        WHERE reservation_id = ?
                        """,
                this::mapEntry,
                reservationId
        ).stream().findFirst().orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "RESERVATION_NOT_FOUND", "Reservation not found."));
        authorizeEntry(row, authorizationHeader, managerAllowed);
        return row;
    }

    private EntryRow authorizeEntry(UUID entryId, String authorizationHeader, boolean managerAllowed) {
        EntryRow row = getEntryRow(entryId);
        authorizeEntry(row, authorizationHeader, managerAllowed);
        return row;
    }

    private void authorizeEntry(EntryRow row, String authorizationHeader, boolean managerAllowed) {
        if (managerAllowed) {
            Optional<ManagerIdentity> manager = identityPort.tryManager(authorizationHeader);
            if (manager.isPresent()
                    && queueManagementPort.getQueueInternal(row.queueId()).managerId().equals(manager.get().id())) {
                return;
            }
        }

        String token = bearerTokens.require(authorizationHeader);
        if (!hashingService.matches(token, row.tokenHash())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ENTRY_TOKEN_FORBIDDEN",
                    "Entry token cannot access this resource.");
        }
    }

    private EntryView toView(EntryRow row) {
        Long position = EntryState.WAITING.name().equals(row.state()) ? calculatePosition(row) : null;
        Instant holdExpiresAt = null;
        if (row.reservationId() != null) {
            holdExpiresAt = reservationPort.getReservation(row.reservationId()).expiresAt();
        }

        return new EntryView(
                row.id(),
                row.queueId(),
                row.reservationId(),
                row.state(),
                row.quantity(),
                position,
                row.sequence(),
                row.createdAt(),
                holdExpiresAt
        );
    }

    private Long calculatePosition(EntryRow row) {
        return jdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                        FROM entries
                        WHERE queue_id = ? AND state = ? AND sequence < ?
                        """,
                Long.class,
                row.queueId(),
                EntryState.WAITING.name(),
                row.sequence()
        );
    }

    private void markTerminal(UUID entryId, EntryState terminalState) {
        int updated = jdbcTemplate.update("""
                        UPDATE entries
                        SET state = ?, terminal_at = ?
                        WHERE id = ?
                        """,
                terminalState.name(),
                timestamp(clock.instant()),
                entryId
        );
        if (updated > 0) {
            domainMetrics.entryCompleted(terminalState.name());
        }
    }

    private EntryRow lockEntry(UUID entryId) {
        return jdbcTemplate.query("""
                        SELECT id, queue_id, resource_id, participant_hash, quantity, sequence,
                               state, reservation_id, token_hash, created_at, hold_granted_at, terminal_at
                        FROM entries
                        WHERE id = ?
                        FOR UPDATE
                        """,
                this::mapEntry,
                entryId
        ).stream().findFirst().orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "ENTRY_NOT_FOUND", "Entry not found."));
    }

    private EntryRow getEntryRow(UUID entryId) {
        return jdbcTemplate.query("""
                        SELECT id, queue_id, resource_id, participant_hash, quantity, sequence,
                               state, reservation_id, token_hash, created_at, hold_granted_at, terminal_at
                        FROM entries
                        WHERE id = ?
                        """,
                this::mapEntry,
                entryId
        ).stream().findFirst().orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "ENTRY_NOT_FOUND", "Entry not found."));
    }

    private EntryRow mapEntry(ResultSet rs, int rowNum) throws SQLException {
        return new EntryRow(
                rs.getObject("id", UUID.class),
                rs.getObject("queue_id", UUID.class),
                rs.getObject("resource_id", UUID.class),
                rs.getString("participant_hash"),
                rs.getInt("quantity"),
                rs.getLong("sequence"),
                rs.getString("state"),
                rs.getObject("reservation_id", UUID.class),
                rs.getString("token_hash"),
                rs.getTimestamp("created_at").toInstant(),
                timestampOrNull(rs, "hold_granted_at"),
                timestampOrNull(rs, "terminal_at")
        );
    }

    private Instant timestampOrNull(ResultSet rs, String column) throws SQLException {
        var timestamp = rs.getTimestamp(column);
        return timestamp == null ? null : timestamp.toInstant();
    }

    private record JoinEntryCommand(UUID queueId, String participantHash, int quantity) {
    }

    private record ConfirmReservationCommand(UUID reservationId) {
    }

    private record EntryRow(
            UUID id,
            UUID queueId,
            UUID resourceId,
            String participantHash,
            int quantity,
            long sequence,
            String state,
            UUID reservationId,
            String tokenHash,
            Instant createdAt,
            Instant holdGrantedAt,
            Instant terminalAt
    ) {
    }
}
