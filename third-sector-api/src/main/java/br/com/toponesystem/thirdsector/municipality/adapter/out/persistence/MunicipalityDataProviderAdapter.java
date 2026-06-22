package br.com.toponesystem.thirdsector.municipality.adapter.out.persistence;

import br.com.toponesystem.thirdsector.municipality.application.api.MunicipalityData;
import br.com.toponesystem.thirdsector.municipality.application.api.MunicipalityDataProvider;
import br.com.toponesystem.thirdsector.municipality.domain.port.out.MunicipalityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class MunicipalityDataProviderAdapter implements MunicipalityDataProvider {

    private final MunicipalityRepository repository;

    @Override
    public java.util.Optional<MunicipalityData> findBySubdomain(String subdomain) {
        return repository.findBySubdomain(subdomain)
                .map(m -> new MunicipalityData(m.getSubdomain(), m.getName(), m.getLogo()));
    }
}
