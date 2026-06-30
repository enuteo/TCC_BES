package tcc.bes.api_monolito.reservation.application;

import java.time.Instant;
import java.util.UUID;

public record ResourceView(
        UUID id,
        UUID managerId,
        String name,
        int totalQuantity,
        int availableQuantity,
        int heldQuantity,
        int confirmedQuantity,
        Instant createdAt
) {
}
