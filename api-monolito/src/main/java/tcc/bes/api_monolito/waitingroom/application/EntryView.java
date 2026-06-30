package tcc.bes.api_monolito.waitingroom.application;

import java.time.Instant;
import java.util.UUID;

public record EntryView(
        UUID id,
        UUID queueId,
        UUID reservationId,
        String state,
        int quantity,
        Long position,
        long sequence,
        Instant createdAt,
        Instant holdExpiresAt
) {
}
