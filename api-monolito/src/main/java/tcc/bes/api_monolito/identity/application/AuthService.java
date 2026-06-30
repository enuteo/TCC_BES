package tcc.bes.api_monolito.identity.application;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tcc.bes.api_monolito.identity.infrastructure.JwtService;
import tcc.bes.api_monolito.identity.domain.ManagerAccount;
import tcc.bes.api_monolito.shared.error.ApiException;
import tcc.bes.api_monolito.shared.security.BearerTokens;

import java.util.Optional;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "app.module.identity.enabled", havingValue = "true", matchIfMissing = true)
public class AuthService {

    private final ManagerRepository managerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final BearerTokens bearerTokens;

    public AuthService(
            ManagerRepository managerRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            BearerTokens bearerTokens
    ) {
        this.managerRepository = managerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.bearerTokens = bearerTokens;
    }

    public LoginResult login(String username, String password) {
        ManagerAccount account = managerRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS",
                        "Invalid username or password."));

        if (!passwordEncoder.matches(password, account.passwordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS",
                    "Invalid username or password.");
        }

        ManagerIdentity identity = toIdentity(account);
        JwtService.IssuedToken issuedToken = jwtService.issue(identity);
        return new LoginResult(issuedToken.token(), "Bearer", issuedToken.expiresAt(), identity);
    }

    public ManagerIdentity requireManager(String authorizationHeader) {
        String token = bearerTokens.require(authorizationHeader);
        JwtService.JwtClaims claims = jwtService.parse(token);
        UUID managerId = UUID.fromString(claims.subject());
        return managerRepository.findById(managerId)
                .map(this::toIdentity)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_MANAGER",
                        "Manager credential is invalid."));
    }

    public Optional<ManagerIdentity> tryManager(String authorizationHeader) {
        try {
            return Optional.of(requireManager(authorizationHeader));
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }

    private ManagerIdentity toIdentity(ManagerAccount account) {
        return new ManagerIdentity(account.id(), account.username(), account.displayName());
    }

}
