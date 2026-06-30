package tcc.bes.api_monolito.queuemanagement.application;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tcc.bes.api_monolito.identity.application.ManagerIdentity;
import tcc.bes.api_monolito.queuemanagement.domain.QueueState;
import tcc.bes.api_monolito.reservation.application.ReservationPort;
import tcc.bes.api_monolito.reservation.application.ResourceView;
import tcc.bes.api_monolito.shared.error.ApiException;
import tcc.bes.api_monolito.shared.idempotency.IdempotencyService;
import tcc.bes.api_monolito.shared.idempotency.IdempotentResult;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static tcc.bes.api_monolito.shared.persistence.JdbcTimestamps.timestamp;

@Service
@ConditionalOnProperty(name = "app.module.queue-management.enabled", havingValue = "true", matchIfMissing = true)
public class QueueApplicationService {

    private final JdbcTemplate jdbcTemplate;
    private final ReservationPort reservationPort;
    private final IdempotencyService idempotencyService;
    private final Clock clock;

    public QueueApplicationService(
            JdbcTemplate jdbcTemplate,
            ReservationPort reservationPort,
            IdempotencyService idempotencyService,
            Clock clock
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.reservationPort = reservationPort;
        this.idempotencyService = idempotencyService;
        this.clock = clock;
    }

    @Transactional
    public IdempotentResult<QueueView> createQueue(
            ManagerIdentity manager,
            String name,
            UUID resourceId,
            int maxQuantityPerParticipant,
            int holdDurationSeconds,
            int workerIntervalMs,
            int maxBatchSize,
            String idempotencyKey
    ) {
        CreateQueueCommand command = new CreateQueueCommand(
                name,
                resourceId,
                maxQuantityPerParticipant,
                holdDurationSeconds,
                workerIntervalMs,
                maxBatchSize
        );
        String requestHash = idempotencyService.requireRequestHash(command);
        var replay = idempotencyService.findReplay(
                "queue.create",
                manager.id().toString(),
                idempotencyKey,
                requestHash,
                QueueView.class
        );
        if (replay.isPresent()) {
            return new IdempotentResult<>(replay.get().statusCode(), replay.get().body());
        }

        ResourceView resource = reservationPort.getResource(manager, resourceId);
        if (maxQuantityPerParticipant > resource.totalQuantity()) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "QUEUE_LIMIT_EXCEEDS_RESOURCE",
                    "Queue participant limit cannot exceed resource total quantity.");
        }

        UUID queueId = UUID.randomUUID();
        Instant now = clock.instant();
        try {
            jdbcTemplate.update("""
                            INSERT INTO queues
                                (id, manager_id, resource_id, name, max_quantity_per_participant,
                                 hold_duration_seconds, worker_interval_ms, max_batch_size, state,
                                 next_sequence, created_at, updated_at)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 1, ?, ?)
                            """,
                    queueId,
                    manager.id(),
                    resourceId,
                    name,
                    maxQuantityPerParticipant,
                    holdDurationSeconds,
                    workerIntervalMs,
                    maxBatchSize,
                    QueueState.DRAFT.name(),
                    timestamp(now),
                    timestamp(now)
            );
        } catch (DataIntegrityViolationException ex) {
            throw new ApiException(HttpStatus.CONFLICT, "RESOURCE_ALREADY_HAS_ACTIVE_QUEUE",
                    "Resource already has a queue that is not closed.");
        }

        QueueView response = getQueue(manager, queueId);
        idempotencyService.save("queue.create", manager.id().toString(), idempotencyKey,
                requestHash, HttpStatus.CREATED.value(), response);
        return new IdempotentResult<>(HttpStatus.CREATED.value(), response);
    }

    public List<QueueView> listQueues(ManagerIdentity manager) {
        return jdbcTemplate.query("""
                        SELECT id, manager_id, resource_id, name, max_quantity_per_participant,
                               hold_duration_seconds, worker_interval_ms, max_batch_size, state,
                               next_sequence, created_at, updated_at
                        FROM queues
                        WHERE manager_id = ?
                        ORDER BY created_at, id
                        """,
                this::mapQueue,
                manager.id()
        );
    }

    public QueueView getQueue(ManagerIdentity manager, UUID queueId) {
        QueueView queue = getQueueInternal(queueId);
        if (!queue.managerId().equals(manager.id())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "QUEUE_NOT_FOUND", "Queue not found.");
        }
        return queue;
    }

    public QueueView getQueueInternal(UUID queueId) {
        return jdbcTemplate.query("""
                        SELECT id, manager_id, resource_id, name, max_quantity_per_participant,
                               hold_duration_seconds, worker_interval_ms, max_batch_size, state,
                               next_sequence, created_at, updated_at
                        FROM queues
                        WHERE id = ?
                        """,
                this::mapQueue,
                queueId
        ).stream().findFirst().orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "QUEUE_NOT_FOUND", "Queue not found."));
    }

    @Transactional
    public QueueView open(ManagerIdentity manager, UUID queueId) {
        QueueView queue = lockQueueForManager(manager, queueId);
        if (QueueState.OPEN.name().equals(queue.state())) {
            return queue;
        }
        transition(queue, QueueState.DRAFT, QueueState.OPEN, "QUEUE_INVALID_TRANSITION");
        return getQueue(manager, queueId);
    }

    @Transactional
    public QueueView pause(ManagerIdentity manager, UUID queueId) {
        QueueView queue = lockQueueForManager(manager, queueId);
        if (QueueState.PAUSED.name().equals(queue.state())) {
            return queue;
        }
        transition(queue, QueueState.OPEN, QueueState.PAUSED, "QUEUE_INVALID_TRANSITION");
        return getQueue(manager, queueId);
    }

    @Transactional
    public QueueView resume(ManagerIdentity manager, UUID queueId) {
        QueueView queue = lockQueueForManager(manager, queueId);
        if (QueueState.OPEN.name().equals(queue.state())) {
            return queue;
        }
        transition(queue, QueueState.PAUSED, QueueState.OPEN, "QUEUE_INVALID_TRANSITION");
        return getQueue(manager, queueId);
    }

    @Transactional
    public QueueView close(ManagerIdentity manager, UUID queueId) {
        QueueView queue = lockQueueForManager(manager, queueId);
        if (QueueState.CLOSED.name().equals(queue.state())) {
            return queue;
        }
        if (!QueueState.DRAFT.name().equals(queue.state())
                && !QueueState.OPEN.name().equals(queue.state())
                && !QueueState.PAUSED.name().equals(queue.state())) {
            throw new ApiException(HttpStatus.CONFLICT, "QUEUE_INVALID_TRANSITION",
                    "Queue cannot be closed from state " + queue.state() + ".");
        }
        updateState(queue.id(), QueueState.CLOSED);
        return getQueue(manager, queueId);
    }

    @Transactional
    public QueueJoinSlot allocateSequenceForJoin(UUID queueId, int quantity) {
        QueueView queue = lockQueue(queueId);
        if (!QueueState.OPEN.name().equals(queue.state())) {
            throw new ApiException(HttpStatus.CONFLICT, "QUEUE_NOT_OPEN", "Queue is not open.");
        }
        if (quantity < 1 || quantity > queue.maxQuantityPerParticipant()) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "ENTRY_QUANTITY_OUT_OF_RANGE",
                    "Entry quantity must be between 1 and the queue participant limit.");
        }
        ResourceView resource = reservationPort.getResourceForInternalUse(queue.resourceId());
        if (quantity > resource.totalQuantity()) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "ENTRY_QUANTITY_EXCEEDS_RESOURCE",
                    "Entry quantity cannot exceed resource total quantity.");
        }

        long sequence = queue.nextSequence();
        jdbcTemplate.update("""
                        UPDATE queues
                        SET next_sequence = next_sequence + 1, updated_at = ?
                        WHERE id = ?
                        """,
                timestamp(clock.instant()),
                queueId
        );
        return new QueueJoinSlot(
                queue.id(),
                queue.managerId(),
                queue.resourceId(),
                queue.maxQuantityPerParticipant(),
                Duration.ofSeconds(queue.holdDurationSeconds()),
                queue.maxBatchSize(),
                sequence
        );
    }

    public List<QueueView> listOpenQueues() {
        return jdbcTemplate.query("""
                        SELECT id, manager_id, resource_id, name, max_quantity_per_participant,
                               hold_duration_seconds, worker_interval_ms, max_batch_size, state,
                               next_sequence, created_at, updated_at
                        FROM queues
                        WHERE state = ?
                        ORDER BY created_at, id
                        """,
                this::mapQueue,
                QueueState.OPEN.name()
        );
    }

    private QueueView lockQueueForManager(ManagerIdentity manager, UUID queueId) {
        QueueView queue = lockQueue(queueId);
        if (!queue.managerId().equals(manager.id())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "QUEUE_NOT_FOUND", "Queue not found.");
        }
        return queue;
    }

    private QueueView lockQueue(UUID queueId) {
        return jdbcTemplate.query("""
                        SELECT id, manager_id, resource_id, name, max_quantity_per_participant,
                               hold_duration_seconds, worker_interval_ms, max_batch_size, state,
                               next_sequence, created_at, updated_at
                        FROM queues
                        WHERE id = ?
                        FOR UPDATE
                        """,
                this::mapQueue,
                queueId
        ).stream().findFirst().orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "QUEUE_NOT_FOUND", "Queue not found."));
    }

    private void transition(QueueView queue, QueueState from, QueueState to, String code) {
        if (!from.name().equals(queue.state())) {
            throw new ApiException(HttpStatus.CONFLICT, code,
                    "Queue cannot transition from " + queue.state() + " to " + to.name() + ".");
        }
        updateState(queue.id(), to);
    }

    private void updateState(UUID queueId, QueueState state) {
        jdbcTemplate.update("""
                        UPDATE queues
                        SET state = ?, updated_at = ?
                        WHERE id = ?
                        """,
                state.name(),
                timestamp(clock.instant()),
                queueId
        );
    }

    private QueueView mapQueue(ResultSet rs, int rowNum) throws SQLException {
        return new QueueView(
                rs.getObject("id", UUID.class),
                rs.getObject("manager_id", UUID.class),
                rs.getObject("resource_id", UUID.class),
                rs.getString("name"),
                rs.getInt("max_quantity_per_participant"),
                rs.getInt("hold_duration_seconds"),
                rs.getInt("worker_interval_ms"),
                rs.getInt("max_batch_size"),
                rs.getString("state"),
                rs.getLong("next_sequence"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }

    private record CreateQueueCommand(
            String name,
            UUID resourceId,
            int maxQuantityPerParticipant,
            int holdDurationSeconds,
            int workerIntervalMs,
            int maxBatchSize
    ) {
    }
}
