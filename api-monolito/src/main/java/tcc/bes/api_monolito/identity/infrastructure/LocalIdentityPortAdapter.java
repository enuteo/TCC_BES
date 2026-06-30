package tcc.bes.api_monolito.identity.infrastructure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import tcc.bes.api_monolito.identity.application.AuthService;
import tcc.bes.api_monolito.identity.application.IdentityPort;
import tcc.bes.api_monolito.identity.application.LoginResult;
import tcc.bes.api_monolito.identity.application.ManagerIdentity;

import java.util.Optional;

@Component
@ConditionalOnExpression("'${app.module.identity.enabled:true}' == 'true' && '${app.adapters.identity.mode:local}' == 'local'")
public class LocalIdentityPortAdapter implements IdentityPort {

    private final AuthService authService;

    public LocalIdentityPortAdapter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public LoginResult login(String username, String password) {
        return authService.login(username, password);
    }

    @Override
    public ManagerIdentity requireManager(String authorizationHeader) {
        return authService.requireManager(authorizationHeader);
    }

    @Override
    public Optional<ManagerIdentity> tryManager(String authorizationHeader) {
        return authService.tryManager(authorizationHeader);
    }
}
