package br.com.toponesystem.thirdsector.auth.domain.port.out;

import br.com.toponesystem.thirdsector.auth.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    User save(User user);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findById(UUID id);
}
