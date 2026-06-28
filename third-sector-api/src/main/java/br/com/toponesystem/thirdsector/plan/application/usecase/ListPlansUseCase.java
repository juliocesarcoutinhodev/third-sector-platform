package br.com.toponesystem.thirdsector.plan.application.usecase;

import br.com.toponesystem.thirdsector.plan.application.dto.PlanView;
import br.com.toponesystem.thirdsector.plan.domain.port.out.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ListPlansUseCase {

    private final PlanRepository repository;

    public List<PlanView> execute() {
        return repository.findAll().stream()
                .map(PlanView::from)
                .toList();
    }
}
