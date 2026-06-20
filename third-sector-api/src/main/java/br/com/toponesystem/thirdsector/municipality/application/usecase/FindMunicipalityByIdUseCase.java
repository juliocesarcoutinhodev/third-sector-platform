package br.com.toponesystem.thirdsector.municipality.application.usecase;

import br.com.toponesystem.thirdsector.municipality.application.MunicipalityView;
import br.com.toponesystem.thirdsector.municipality.domain.exception.MunicipalityNotFoundException;
import br.com.toponesystem.thirdsector.municipality.domain.port.out.MunicipalityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindMunicipalityByIdUseCase {

    private final MunicipalityRepository repository;

    public MunicipalityView execute(Long id) {
        return repository.findById(id)
                .map(MunicipalityView::from)
                .orElseThrow(() -> new MunicipalityNotFoundException(id));
    }
}
