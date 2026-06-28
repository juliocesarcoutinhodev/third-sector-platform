package br.com.toponesystem.thirdsector.plan.application.dto;

import br.com.toponesystem.thirdsector.plan.domain.model.Plan;

import java.time.Instant;
import java.util.UUID;

public record PlanView(
        UUID id,
        String name,
        Integer maxOrganizations,
        Instant createdAt,
        Instant updatedAt
) {

    public static PlanView from(Plan plan) {
        return new PlanView(
                plan.getId(), plan.getName(), plan.getMaxOrganizations(),
                plan.getCreatedAt(), plan.getUpdatedAt()
        );
    }
}
