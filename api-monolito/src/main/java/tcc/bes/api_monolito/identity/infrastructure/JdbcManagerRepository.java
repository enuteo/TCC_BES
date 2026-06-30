package tcc.bes.api_monolito.identity.infrastructure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import tcc.bes.api_monolito.identity.application.ManagerRepository;
import tcc.bes.api_monolito.identity.domain.ManagerAccount;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import static tcc.bes.api_monolito.shared.persistence.JdbcTimestamps.timestamp;

@Repository
@ConditionalOnProperty(name = "app.module.identity.enabled", havingValue = "true", matchIfMissing = true)
public class JdbcManagerRepository implements ManagerRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcManagerRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<ManagerAccount> findByUsername(String username) {
        return jdbcTemplate.query("""
                        SELECT id, username, password_hash, display_name, created_at
                        FROM manager_accounts
                        WHERE username = ?
                        """,
                this::map,
                username
        ).stream().findFirst();
    }

    @Override
    public Optional<ManagerAccount> findById(UUID id) {
        return jdbcTemplate.query("""
                        SELECT id, username, password_hash, display_name, created_at
                        FROM manager_accounts
                        WHERE id = ?
                        """,
                this::map,
                id
        ).stream().findFirst();
    }

    @Override
    public void create(ManagerAccount managerAccount) {
        jdbcTemplate.update("""
                        INSERT INTO manager_accounts (id, username, password_hash, display_name, created_at)
                        VALUES (?, ?, ?, ?, ?)
                        """,
                managerAccount.id(),
                managerAccount.username(),
                managerAccount.passwordHash(),
                managerAccount.displayName(),
                timestamp(managerAccount.createdAt())
        );
    }

    private ManagerAccount map(ResultSet rs, int rowNum) throws SQLException {
        return new ManagerAccount(
                rs.getObject("id", UUID.class),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("display_name"),
                rs.getTimestamp("created_at").toInstant()
        );
    }
}
