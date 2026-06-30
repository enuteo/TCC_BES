package tcc.bes.api_monolito.queuemanagement.application;

import java.time.Duration;
import java.util.UUID;

public record QueueJoinSlot(
        UUID queueId,
        UUID managerId,
        UUID resourceId,
        int maxQuantityPerParticipant,
        Duration holdDuration,
        int maxBatchSize,
        long sequence
) {
}
