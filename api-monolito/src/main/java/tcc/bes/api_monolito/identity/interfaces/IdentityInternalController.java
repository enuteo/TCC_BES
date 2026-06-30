package tcc.bes.api_monolito.identity.interfaces;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tcc.bes.api_monolito.identity.application.AuthService;
import tcc.bes.api_monolito.identity.application.LoginResult;
import tcc.bes.api_monolito.identity.application.ManagerIdentity;
import tcc.bes.api_monolito.shared.internal.InternalHeaders;
import tcc.bes.api_monolito.shared.internal.InternalRequestAuth;

@RestController
@RequestMapping("/internal/v1/auth")
@ConditionalOnProperty(name = {
        "app.internal-api.enabled",
        "app.module.identity.enabled"
}, havingValue = "true")
public class IdentityInternalController {

    private final AuthService authService;
    private final InternalRequestAuth internalRequestAuth;

    public IdentityInternalController(
            AuthService authService,
            InternalRequestAuth internalRequestAuth
    ) {
        this.authService = authService;
        this.internalRequestAuth = internalRequestAuth;
    }

    @PostMapping("/login")
    public LoginResult login(
            @RequestHeader(InternalHeaders.INTERNAL_TOKEN) String internalToken,
            @RequestBody LoginRequest request
    ) {
        internalRequestAuth.require(internalToken);
        return authService.login(request.username(), request.password());
    }

    @PostMapping("/manager-resolution")
    public ManagerIdentity requireManager(
            @RequestHeader(InternalHeaders.INTERNAL_TOKEN) String internalToken,
            @RequestBody ResolveManagerRequest request
    ) {
        internalRequestAuth.require(internalToken);
        return authService.requireManager(request.authorizationHeader());
    }

    private record LoginRequest(String username, String password) {
    }

    private record ResolveManagerRequest(String authorizationHeader) {
    }
}
