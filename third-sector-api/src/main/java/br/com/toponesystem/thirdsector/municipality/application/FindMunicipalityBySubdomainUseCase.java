package br.com.toponesystem.thirdsector.municipality.application;

import br.com.toponesystem.thirdsector.municipality.domain.MunicipalityNotFoundException;
import br.com.toponesystem.thirdsector.municipality.domain.port.out.MunicipalityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindMunicipalityBySubdomainUseCase {

    private final MunicipalityRepository repository;

    public MunicipalityView execute(String subdomain) {
        return repository.findBySubdomain(subdomain)
                .map(MunicipalityView::from)
                .orElseThrow(() -> new MunicipalityNotFoundException(subdomain));
    }
}
