package br.com.toponesystem.thirdsector;

import br.com.toponesystem.thirdsector.plan.domain.port.out.PlanRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PlanFixtures {

    private final PlanRepository planRepository;

    public PlanFixtures(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    public UUID basicPlanId() {
        return planRepository.findByName("BASIC")
                .orElseThrow(() -> new IllegalStateException("BASIC plan not seeded"))
                .getId();
    }

    public UUID intermediatePlanId() {
        return planRepository.findByName("INTERMEDIATE")
                .orElseThrow(() -> new IllegalStateException("INTERMEDIATE plan not seeded"))
                .getId();
    }

    public UUID enterprisePlanId() {
        return planRepository.findByName("ENTERPRISE")
                .orElseThrow(() -> new IllegalStateException("ENTERPRISE plan not seeded"))
                .getId();
    }
}
