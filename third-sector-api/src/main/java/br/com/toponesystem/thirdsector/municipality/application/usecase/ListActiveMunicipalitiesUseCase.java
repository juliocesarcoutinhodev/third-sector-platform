package br.com.toponesystem.thirdsector.municipality.application.usecase;

import br.com.toponesystem.thirdsector.municipality.application.MunicipalityView;

import br.com.toponesystem.thirdsector.municipality.domain.port.out.MunicipalityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListActiveMunicipalitiesUseCase {

    private final MunicipalityRepository repository;

    public List<MunicipalityView> execute() {
        return repository.findAllActive().stream().map(MunicipalityView::from).toList();
    }
}
