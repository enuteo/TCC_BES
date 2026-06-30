package tcc.bes.api_monolito.waitingroom.application;

import java.time.Instant;
import java.util.UUID;

public record EntryJoinResponse(
        UUID id,
        UUID queueId,
        String state,
        int quantity,
        Long position,
        long sequence,
        Instant createdAt,
        String entryToken
) {
}
