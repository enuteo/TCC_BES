package tcc.bes.api_monolito.identity.interfaces;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tcc.bes.api_monolito.identity.application.IdentityPort;
import tcc.bes.api_monolito.identity.application.LoginResult;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@ConditionalOnProperty(name = "app.public-api.enabled", havingValue = "true", matchIfMissing = true)
public class AuthController {

    private final IdentityPort identityPort;

    public AuthController(IdentityPort identityPort) {
        this.identityPort = identityPort;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = identityPort.login(request.username(), request.password());
        return ResponseEntity.ok(new LoginResponse(
                result.accessToken(),
                result.tokenType(),
                result.expiresAt(),
                new ManagerResponse(result.manager().id(), result.manager().username(), result.manager().displayName())
        ));
    }

    public record LoginRequest(
            @NotBlank(message = "Username cannot be empty")
            @Size(max = 100, message = "Username must have at most 100 characters")
            String username,
            @NotBlank(message = "Password cannot be empty")
            @Size(max = 255, message = "Password must have at most 255 characters")
            String password
    ) {
    }

    public record LoginResponse(
            String accessToken,
            String tokenType,
            Instant expiresAt,
            ManagerResponse manager
    ) {
    }

    public record ManagerResponse(UUID id, String username, String displayName) {
    }
}
