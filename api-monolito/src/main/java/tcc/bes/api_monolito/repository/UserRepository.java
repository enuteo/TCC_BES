package tcc.bes.api_monolito.repository;

import tcc.bes.api_monolito.model.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByUsername(String username);
    Optional<User> findById(Long id);
}
