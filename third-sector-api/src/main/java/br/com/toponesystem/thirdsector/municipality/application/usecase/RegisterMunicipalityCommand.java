package br.com.toponesystem.thirdsector.municipality.application.usecase;

import br.com.toponesystem.thirdsector.municipality.domain.model.Plan;

public record RegisterMunicipalityCommand(
        String name,
        String cnpj,
        String subdomain,
        Plan plan,
        String logo
) {}
