package tcc.bes.api_monolito.reservation.application;

import java.time.Instant;
import java.util.UUID;

public record ReservationTerminalEventView(
        UUID id,
        UUID reservationId,
        UUID entryId,
        String state,
        Instant occurredAt
) {
}
