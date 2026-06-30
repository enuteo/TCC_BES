package tcc.bes.api_monolito.queuemanagement.interfaces;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tcc.bes.api_monolito.identity.application.IdentityPort;
import tcc.bes.api_monolito.identity.application.ManagerIdentity;
import tcc.bes.api_monolito.queuemanagement.application.QueueManagementPort;
import tcc.bes.api_monolito.queuemanagement.application.QueueView;
import tcc.bes.api_monolito.waitingroom.application.WaitingRoomPort;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/queues")
@ConditionalOnProperty(name = "app.public-api.enabled", havingValue = "true", matchIfMissing = true)
public class QueueController {

    private final IdentityPort identityPort;
    private final QueueManagementPort queueManagementPort;
    private final WaitingRoomPort waitingRoomPort;

    public QueueController(
            IdentityPort identityPort,
            QueueManagementPort queueManagementPort,
            WaitingRoomPort waitingRoomPort
    ) {
        this.identityPort = identityPort;
        this.queueManagementPort = queueManagementPort;
        this.waitingRoomPort = waitingRoomPort;
    }

    @PostMapping
    public ResponseEntity<QueueView> createQueue(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreateQueueRequest request
    ) {
        ManagerIdentity manager = identityPort.requireManager(authorization);
        var result = queueManagementPort.createQueue(
                manager,
                request.name(),
                request.resourceId(),
                request.maxQuantityPerParticipant(),
                request.holdDurationSeconds(),
                request.workerIntervalMs(),
                request.maxBatchSize(),
                idempotencyKey
        );
        return ResponseEntity.status(result.statusCode()).body(result.body());
    }

    @GetMapping
    public List<QueueView> listQueues(@RequestHeader("Authorization") String authorization) {
        return queueManagementPort.listQueues(identityPort.requireManager(authorization));
    }

    @GetMapping("/{queueId}")
    public QueueView getQueue(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID queueId
    ) {
        return queueManagementPort.getQueue(identityPort.requireManager(authorization), queueId);
    }

    @PostMapping("/{queueId}/open")
    public QueueView open(@RequestHeader("Authorization") String authorization, @PathVariable UUID queueId) {
        return queueManagementPort.open(identityPort.requireManager(authorization), queueId);
    }

    @PostMapping("/{queueId}/pause")
    public QueueView pause(@RequestHeader("Authorization") String authorization, @PathVariable UUID queueId) {
        return queueManagementPort.pause(identityPort.requireManager(authorization), queueId);
    }

    @PostMapping("/{queueId}/resume")
    public QueueView resume(@RequestHeader("Authorization") String authorization, @PathVariable UUID queueId) {
        return queueManagementPort.resume(identityPort.requireManager(authorization), queueId);
    }

    @PostMapping("/{queueId}/close")
    public ResponseEntity<QueueView> close(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID queueId
    ) {
        QueueView queue = queueManagementPort.close(identityPort.requireManager(authorization), queueId);
        waitingRoomPort.cancelWaitingEntriesForClosedQueue(queueId);
        return ResponseEntity.accepted().body(queue);
    }

    public record CreateQueueRequest(
            @NotBlank(message = "Name cannot be empty")
            @Size(max = 160, message = "Name must have at most 160 characters")
            String name,
            @NotNull(message = "Resource id is required")
            UUID resourceId,
            @Min(value = 1, message = "Max quantity per participant must be positive")
            int maxQuantityPerParticipant,
            @Min(value = 1, message = "Hold duration must be positive")
            int holdDurationSeconds,
            @Min(value = 1, message = "Worker interval must be positive")
            int workerIntervalMs,
            @Min(value = 1, message = "Max batch size must be positive")
            int maxBatchSize
    ) {
    }
}
