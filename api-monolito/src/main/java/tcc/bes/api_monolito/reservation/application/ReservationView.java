package tcc.bes.api_monolito.reservation.application;

import java.time.Instant;
import java.util.UUID;

public record ReservationView(
        UUID id,
        UUID resourceId,
        UUID entryId,
        int quantity,
        Instant expiresAt,
        String state,
        Instant createdAt,
        Instant confirmedAt,
        Instant cancelledAt,
        Instant expiredAt
) {
}
