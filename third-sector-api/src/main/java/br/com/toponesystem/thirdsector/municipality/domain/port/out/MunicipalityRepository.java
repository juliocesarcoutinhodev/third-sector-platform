package br.com.toponesystem.thirdsector.municipality.domain.port.out;

import br.com.toponesystem.thirdsector.municipality.domain.model.Municipality;

import java.util.List;
import java.util.Optional;

public interface MunicipalityRepository {

    Municipality save(Municipality municipality);

    Optional<Municipality> findBySubdomain(String subdomain);

    boolean existsBySubdomain(String subdomain);

    Optional<Municipality> findById(Long id);

    List<Municipality> findAllActive();
}
