package br.com.toponesystem.thirdsector.plan.application.usecase;

public record CreatePlanCommand(
        String name,
        Integer maxOrganizations
) {}
