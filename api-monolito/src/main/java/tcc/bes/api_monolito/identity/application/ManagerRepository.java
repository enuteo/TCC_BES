package tcc.bes.api_monolito.identity.application;

import tcc.bes.api_monolito.identity.domain.ManagerAccount;

import java.util.Optional;
import java.util.UUID;

public interface ManagerRepository {

    Optional<ManagerAccount> findByUsername(String username);

    Optional<ManagerAccount> findById(UUID id);

    void create(ManagerAccount managerAccount);
}
