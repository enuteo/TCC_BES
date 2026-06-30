package tcc.bes.api_monolito.identity.domain;

import java.time.Instant;
import java.util.UUID;

public record ManagerAccount(
        UUID id,
        String username,
        String passwordHash,
        String displayName,
        Instant createdAt
) {
}
