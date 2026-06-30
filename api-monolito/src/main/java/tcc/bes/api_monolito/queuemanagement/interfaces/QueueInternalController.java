package tcc.bes.api_monolito.queuemanagement.interfaces;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tcc.bes.api_monolito.identity.application.ManagerIdentity;
import tcc.bes.api_monolito.queuemanagement.application.QueueApplicationService;
import tcc.bes.api_monolito.queuemanagement.application.QueueJoinSlot;
import tcc.bes.api_monolito.queuemanagement.application.QueueView;
import tcc.bes.api_monolito.shared.internal.InternalHeaders;
import tcc.bes.api_monolito.shared.internal.InternalRequestAuth;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal/v1/queues")
@ConditionalOnProperty(name = {
        "app.internal-api.enabled",
        "app.module.queue-management.enabled"
}, havingValue = "true")
public class QueueInternalController {

    private final QueueApplicationService queueApplicationService;
    private final InternalRequestAuth internalRequestAuth;

    public QueueInternalController(
            QueueApplicationService queueApplicationService,
            InternalRequestAuth internalRequestAuth
    ) {
        this.queueApplicationService = queueApplicationService;
        this.internalRequestAuth = internalRequestAuth;
    }

    @PostMapping
    public ResponseEntity<QueueView> createQueue(
            @RequestHeader(InternalHeaders.INTERNAL_TOKEN) String internalToken,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody CreateQueueRequest request
    ) {
        internalRequestAuth.require(internalToken);
        var result = queueApplicationService.createQueue(
                manager(request.managerId()),
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
    public List<QueueView> listQueues(
            @RequestHeader(InternalHeaders.INTERNAL_TOKEN) String internalToken,
            @RequestParam UUID managerId
    ) {
        internalRequestAuth.require(internalToken);
        return queueApplicationService.listQueues(manager(managerId));
    }

    @GetMapping("/{queueId}")
    public QueueView getQueue(
            @RequestHeader(InternalHeaders.INTERNAL_TOKEN) String internalToken,
            @RequestParam UUID managerId,
            @PathVariable UUID queueId
    ) {
        internalRequestAuth.require(internalToken);
        return queueApplicationService.getQueue(manager(managerId), queueId);
    }

    @GetMapping("/{queueId}/internal")
    public QueueView getQueueInternal(
            @RequestHeader(InternalHeaders.INTERNAL_TOKEN) String internalToken,
            @PathVariable UUID queueId
    ) {
        internalRequestAuth.require(internalToken);
        return queueApplicationService.getQueueInternal(queueId);
    }

    @PostMapping("/{queueId}/open")
    public QueueView open(
            @RequestHeader(InternalHeaders.INTERNAL_TOKEN) String internalToken,
            @RequestParam UUID managerId,
            @PathVariable UUID queueId
    ) {
        internalRequestAuth.require(internalToken);
        return queueApplicationService.open(manager(managerId), queueId);
    }

    @PostMapping("/{queueId}/pause")
    public QueueView pause(
            @RequestHeader(InternalHeaders.INTERNAL_TOKEN) String internalToken,
            @RequestParam UUID managerId,
            @PathVariable UUID queueId
    ) {
        internalRequestAuth.require(internalToken);
        return queueApplicationService.pause(manager(managerId), queueId);
    }

    @PostMapping("/{queueId}/resume")
    public QueueView resume(
            @RequestHeader(InternalHeaders.INTERNAL_TOKEN) String internalToken,
            @RequestParam UUID managerId,
            @PathVariable UUID queueId
    ) {
        internalRequestAuth.require(internalToken);
        return queueApplicationService.resume(manager(managerId), queueId);
    }

    @PostMapping("/{queueId}/close")
    public QueueView close(
            @RequestHeader(InternalHeaders.INTERNAL_TOKEN) String internalToken,
            @RequestParam UUID managerId,
            @PathVariable UUID queueId
    ) {
        internalRequestAuth.require(internalToken);
        return queueApplicationService.close(manager(managerId), queueId);
    }

    @PostMapping("/{queueId}/join-slots")
    public QueueJoinSlot allocateSequenceForJoin(
            @RequestHeader(InternalHeaders.INTERNAL_TOKEN) String internalToken,
            @PathVariable UUID queueId,
            @RequestBody AllocateJoinSlotRequest request
    ) {
        internalRequestAuth.require(internalToken);
        return queueApplicationService.allocateSequenceForJoin(queueId, request.quantity());
    }

    @GetMapping("/open")
    public List<QueueView> listOpenQueues(
            @RequestHeader(InternalHeaders.INTERNAL_TOKEN) String internalToken
    ) {
        internalRequestAuth.require(internalToken);
        return queueApplicationService.listOpenQueues();
    }

    private ManagerIdentity manager(UUID managerId) {
        return new ManagerIdentity(managerId, "internal", "internal");
    }

    private record CreateQueueRequest(
            UUID managerId,
            String name,
            UUID resourceId,
            int maxQuantityPerParticipant,
            int holdDurationSeconds,
            int workerIntervalMs,
            int maxBatchSize
    ) {
    }

    private record AllocateJoinSlotRequest(int quantity) {
    }
}
