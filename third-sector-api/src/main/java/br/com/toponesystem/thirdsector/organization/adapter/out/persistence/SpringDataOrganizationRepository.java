package br.com.toponesystem.thirdsector.organization.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface SpringDataOrganizationRepository extends JpaRepository<OrganizationEntity, Long> {

    Optional<OrganizationEntity> findByCnpj(String cnpj);

    boolean existsByCnpj(String cnpj);
}
