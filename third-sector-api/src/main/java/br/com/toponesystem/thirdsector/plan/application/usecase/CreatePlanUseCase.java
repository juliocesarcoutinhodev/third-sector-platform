package br.com.toponesystem.thirdsector.plan.application.usecase;

import br.com.toponesystem.thirdsector.plan.application.dto.PlanView;
import br.com.toponesystem.thirdsector.plan.domain.exception.PlanNotFoundException;
import br.com.toponesystem.thirdsector.plan.domain.model.Plan;
import br.com.toponesystem.thirdsector.plan.domain.port.out.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CreatePlanUseCase {

    private final PlanRepository repository;

    @Transactional
    public PlanView execute(CreatePlanCommand command) {
        var plan = new Plan(command.name(), command.maxOrganizations());
        var saved = repository.save(plan);
        return PlanView.from(saved);
    }
}
