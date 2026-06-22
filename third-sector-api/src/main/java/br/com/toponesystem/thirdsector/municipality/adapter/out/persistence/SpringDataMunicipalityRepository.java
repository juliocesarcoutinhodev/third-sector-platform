package br.com.toponesystem.thirdsector.municipality.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface SpringDataMunicipalityRepository extends JpaRepository<MunicipalityEntity, UUID> {

    Optional<MunicipalityEntity> findBySubdomain(String subdomain);

    boolean existsBySubdomain(String subdomain);

    List<MunicipalityEntity> findAllByActiveTrue();
}
