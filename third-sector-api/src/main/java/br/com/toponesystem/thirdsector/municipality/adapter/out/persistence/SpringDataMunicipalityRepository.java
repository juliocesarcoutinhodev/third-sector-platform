package br.com.toponesystem.thirdsector.municipality.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface SpringDataMunicipalityRepository extends JpaRepository<MunicipalityEntity, Long> {

    Optional<MunicipalityEntity> findBySubdomain(String subdomain);

    boolean existsBySubdomain(String subdomain);

    List<MunicipalityEntity> findAllByActiveTrue();
}
