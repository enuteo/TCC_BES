package tcc.bes.api_monolito.repository.impl;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import tcc.bes.api_monolito.model.User;
import tcc.bes.api_monolito.repository.UserRepository;
import tcc.bes.api_monolito.repository.mapper.UserRowMapper;

import java.util.Collections;
import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {
    
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public UserRepositoryImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT id, username, password, email, first_name, last_name FROM usuarios WHERE username = :username";
        try {
            User user = namedParameterJdbcTemplate.queryForObject(
                    sql,
                    Collections.singletonMap("username", username),
                    new UserRowMapper()
            );
            return Optional.of(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT id, username, password, email, first_name, last_name FROM usuarios WHERE id = :id";
        try {
            User user = namedParameterJdbcTemplate.queryForObject(
                    sql,
                    Collections.singletonMap("id", id),
                    new UserRowMapper()
            );
            return Optional.of(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
