package br.com.toponesystem.thirdsector.organization.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface SpringDataOrganizationRepository extends JpaRepository<OrganizationEntity, UUID> {

    Optional<OrganizationEntity> findByCnpj(String cnpj);

    boolean existsByCnpj(String cnpj);
}
