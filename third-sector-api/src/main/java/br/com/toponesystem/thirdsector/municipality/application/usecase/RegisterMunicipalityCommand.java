package br.com.toponesystem.thirdsector.municipality.application.usecase;

import java.util.UUID;

public record RegisterMunicipalityCommand(
        String name,
        String cnpj,
        String subdomain,
        UUID planId,
        String logo,
        String adminName,
        String adminEmail
) {}
