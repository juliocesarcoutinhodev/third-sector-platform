package br.com.toponesystem.thirdsector.auth.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface SpringDataSuperAdminRepository extends JpaRepository<SuperAdminEntity, UUID> {

    Optional<SuperAdminEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
