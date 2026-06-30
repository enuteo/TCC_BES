package tcc.bes.api_monolito.identity.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import tcc.bes.api_monolito.identity.application.ManagerRepository;
import tcc.bes.api_monolito.identity.domain.ManagerAccount;

import java.time.Clock;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "app.module.identity.enabled", havingValue = "true", matchIfMissing = true)
public class ManagerBootstrap implements ApplicationRunner {

    private final boolean enabled;
    private final String username;
    private final String password;
    private final String displayName;
    private final ManagerRepository managerRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public ManagerBootstrap(
            @Value("${app.bootstrap.manager.enabled}") boolean enabled,
            @Value("${app.bootstrap.manager.username}") String username,
            @Value("${app.bootstrap.manager.password}") String password,
            @Value("${app.bootstrap.manager.display-name}") String displayName,
            ManagerRepository managerRepository,
            PasswordEncoder passwordEncoder,
            Clock clock
    ) {
        this.enabled = enabled;
        this.username = username;
        this.password = password;
        this.displayName = displayName;
        this.managerRepository = managerRepository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled || managerRepository.findByUsername(username).isPresent()) {
            return;
        }

        managerRepository.create(new ManagerAccount(
                UUID.randomUUID(),
                username,
                passwordEncoder.encode(password),
                displayName,
                clock.instant()
        ));
    }
}
