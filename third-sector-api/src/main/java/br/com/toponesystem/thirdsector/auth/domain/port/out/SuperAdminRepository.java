package br.com.toponesystem.thirdsector.auth.domain.port.out;

import br.com.toponesystem.thirdsector.auth.domain.model.SuperAdmin;

import java.util.Optional;
import java.util.UUID;

public interface SuperAdminRepository {

    SuperAdmin save(SuperAdmin superAdmin);

    Optional<SuperAdmin> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<SuperAdmin> findById(UUID id);
}
