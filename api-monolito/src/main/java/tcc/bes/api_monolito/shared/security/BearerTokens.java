package tcc.bes.api_monolito.shared.security;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import tcc.bes.api_monolito.shared.error.ApiException;

@Component
public class BearerTokens {

    public String require(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTHORIZATION_REQUIRED", "Bearer credential is required.");
        }

        String normalized = authorizationHeader.trim();
        if (!normalized.regionMatches(true, 0, "Bearer ", 0, 7)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_AUTHORIZATION", "Bearer credential is invalid.");
        }

        String token = normalized.substring(7).trim();
        if (token.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_AUTHORIZATION", "Bearer credential is invalid.");
        }

        return token;
    }
}
