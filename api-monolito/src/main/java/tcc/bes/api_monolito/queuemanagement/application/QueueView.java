package tcc.bes.api_monolito.queuemanagement.application;

import java.time.Instant;
import java.util.UUID;

public record QueueView(
        UUID id,
        UUID managerId,
        UUID resourceId,
        String name,
        int maxQuantityPerParticipant,
        int holdDurationSeconds,
        int workerIntervalMs,
        int maxBatchSize,
        String state,
        long nextSequence,
        Instant createdAt,
        Instant updatedAt
) {
}
