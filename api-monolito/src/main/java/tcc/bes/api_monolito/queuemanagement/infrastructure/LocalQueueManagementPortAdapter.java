package tcc.bes.api_monolito.queuemanagement.infrastructure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import tcc.bes.api_monolito.identity.application.ManagerIdentity;
import tcc.bes.api_monolito.queuemanagement.application.QueueApplicationService;
import tcc.bes.api_monolito.queuemanagement.application.QueueJoinSlot;
import tcc.bes.api_monolito.queuemanagement.application.QueueManagementPort;
import tcc.bes.api_monolito.queuemanagement.application.QueueView;
import tcc.bes.api_monolito.shared.idempotency.IdempotentResult;

import java.util.List;
import java.util.UUID;

@Component
@ConditionalOnExpression("'${app.module.queue-management.enabled:true}' == 'true' && '${app.adapters.queue-management.mode:local}' == 'local'")
public class LocalQueueManagementPortAdapter implements QueueManagementPort {

    private final QueueApplicationService queueApplicationService;

    public LocalQueueManagementPortAdapter(QueueApplicationService queueApplicationService) {
        this.queueApplicationService = queueApplicationService;
    }

    @Override
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
        return queueApplicationService.createQueue(
                manager,
                name,
                resourceId,
                maxQuantityPerParticipant,
                holdDurationSeconds,
                workerIntervalMs,
                maxBatchSize,
                idempotencyKey
        );
    }

    @Override
    public List<QueueView> listQueues(ManagerIdentity manager) {
        return queueApplicationService.listQueues(manager);
    }

    @Override
    public QueueView getQueue(ManagerIdentity manager, UUID queueId) {
        return queueApplicationService.getQueue(manager, queueId);
    }

    @Override
    public QueueView getQueueInternal(UUID queueId) {
        return queueApplicationService.getQueueInternal(queueId);
    }

    @Override
    public QueueView open(ManagerIdentity manager, UUID queueId) {
        return queueApplicationService.open(manager, queueId);
    }

    @Override
    public QueueView pause(ManagerIdentity manager, UUID queueId) {
        return queueApplicationService.pause(manager, queueId);
    }

    @Override
    public QueueView resume(ManagerIdentity manager, UUID queueId) {
        return queueApplicationService.resume(manager, queueId);
    }

    @Override
    public QueueView close(ManagerIdentity manager, UUID queueId) {
        return queueApplicationService.close(manager, queueId);
    }

    @Override
    public QueueJoinSlot allocateSequenceForJoin(UUID queueId, int quantity) {
        return queueApplicationService.allocateSequenceForJoin(queueId, quantity);
    }

    @Override
    public List<QueueView> listOpenQueues() {
        return queueApplicationService.listOpenQueues();
    }
}
