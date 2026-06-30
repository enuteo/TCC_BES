package tcc.bes.api_monolito.identity.application;

import java.util.Optional;

public interface IdentityPort {

    LoginResult login(String username, String password);

    ManagerIdentity requireManager(String authorizationHeader);

    Optional<ManagerIdentity> tryManager(String authorizationHeader);
}
