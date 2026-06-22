package br.com.toponesystem.thirdsector.municipality.domain.port.out;

import br.com.toponesystem.thirdsector.municipality.domain.model.Municipality;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MunicipalityRepository {

    Municipality save(Municipality municipality);

    Optional<Municipality> findBySubdomain(String subdomain);

    boolean existsBySubdomain(String subdomain);

    Optional<Municipality> findById(UUID id);

    List<Municipality> findAllActive();
}
