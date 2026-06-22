package br.com.toponesystem.thirdsector.municipality.application.api;

import java.util.Optional;

public interface MunicipalityDataProvider {

    Optional<MunicipalityData> findBySubdomain(String subdomain);
}
