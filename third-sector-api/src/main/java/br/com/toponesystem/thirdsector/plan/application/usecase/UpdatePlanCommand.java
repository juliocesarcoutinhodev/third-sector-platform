package br.com.toponesystem.thirdsector.plan.application.usecase;

import java.util.UUID;

public record UpdatePlanCommand(
        UUID id,
        Integer maxOrganizations
) {}
