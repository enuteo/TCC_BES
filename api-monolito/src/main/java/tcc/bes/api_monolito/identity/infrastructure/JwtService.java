package tcc.bes.api_monolito.identity.infrastructure;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import tcc.bes.api_monolito.identity.application.ManagerIdentity;
import tcc.bes.api_monolito.shared.error.ApiException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Component
public class JwtService {

    private final String secret;
    private final long expirationSeconds;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public JwtService(
            @Value("${app.security.jwt-secret}") String secret,
            @Value("${app.security.jwt-expiration-seconds}") long expirationSeconds,
            ObjectMapper objectMapper,
            Clock clock
    ) {
        this.secret = secret;
        this.expirationSeconds = expirationSeconds;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    public IssuedToken issue(ManagerIdentity manager) {
        try {
            Instant expiresAt = clock.instant().plusSeconds(expirationSeconds);
            String header = encodeJson(Map.of("alg", "HS256", "typ", "JWT"));
            String payload = encodeJson(Map.of(
                    "sub", manager.id().toString(),
                    "username", manager.username(),
                    "name", manager.displayName(),
                    "exp", expiresAt.getEpochSecond()
            ));
            String signingInput = header + "." + payload;
            String signature = sign(signingInput);
            return new IssuedToken(signingInput + "." + signature, expiresAt);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not issue JWT", ex);
        }
    }

    public JwtClaims parse(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw invalid();
            }

            String signingInput = parts[0] + "." + parts[1];
            if (!MessageDigest.isEqual(sign(signingInput).getBytes(StandardCharsets.UTF_8),
                    parts[2].getBytes(StandardCharsets.UTF_8))) {
                throw invalid();
            }

            Map<String, Object> payload = objectMapper.readValue(
                    Base64.getUrlDecoder().decode(parts[1]),
                    new TypeReference<>() {
                    }
            );

            long exp = ((Number) payload.get("exp")).longValue();
            if (clock.instant().isAfter(Instant.ofEpochSecond(exp))) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", "Manager credential has expired.");
            }

            return new JwtClaims((String) payload.get("sub"), (String) payload.get("username"));
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw invalid();
        }
    }

    private ApiException invalid() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "Manager credential is invalid.");
    }

    private String encodeJson(Object value) throws Exception {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(objectMapper.writeValueAsBytes(value));
    }

    private String sign(String signingInput) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8)));
    }

    public record IssuedToken(String token, Instant expiresAt) {
    }

    public record JwtClaims(String subject, String username) {
    }
}
