package tcc.bes.api_monolito.identity.application;

import java.time.Instant;

public record LoginResult(
        String accessToken,
        String tokenType,
        Instant expiresAt,
        ManagerIdentity manager
) {
}
