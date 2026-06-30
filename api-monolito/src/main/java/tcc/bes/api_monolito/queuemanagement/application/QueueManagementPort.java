package tcc.bes.api_monolito.queuemanagement.application;

import tcc.bes.api_monolito.identity.application.ManagerIdentity;
import tcc.bes.api_monolito.shared.idempotency.IdempotentResult;

import java.util.List;
import java.util.UUID;

public interface QueueManagementPort {

    IdempotentResult<QueueView> createQueue(
            ManagerIdentity manager,
            String name,
            UUID resourceId,
            int maxQuantityPerParticipant,
            int holdDurationSeconds,
            int workerIntervalMs,
            int maxBatchSize,
            String idempotencyKey
    );

    List<QueueView> listQueues(ManagerIdentity manager);

    QueueView getQueue(ManagerIdentity manager, UUID queueId);

    QueueView getQueueInternal(UUID queueId);

    QueueView open(ManagerIdentity manager, UUID queueId);

    QueueView pause(ManagerIdentity manager, UUID queueId);

    QueueView resume(ManagerIdentity manager, UUID queueId);

    QueueView close(ManagerIdentity manager, UUID queueId);

    QueueJoinSlot allocateSequenceForJoin(UUID queueId, int quantity);

    List<QueueView> listOpenQueues();
}
