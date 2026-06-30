package tcc.bes.api_monolito.shared.internal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import tcc.bes.api_monolito.shared.error.ApiException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class InternalRequestAuth {

    private final String token;

    public InternalRequestAuth(@Value("${app.internal.token}") String token) {
        this.token = token;
    }

    public String token() {
        return token;
    }

    public void require(String providedToken) {
        if (providedToken == null || providedToken.isBlank() || token == null || token.isBlank()) {
            throw unauthorized();
        }

        byte[] expected = token.getBytes(StandardCharsets.UTF_8);
        byte[] provided = providedToken.getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(expected, provided)) {
            throw unauthorized();
        }
    }

    private ApiException unauthorized() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "INTERNAL_TOKEN_INVALID",
                "Internal service token is invalid.");
    }
}
