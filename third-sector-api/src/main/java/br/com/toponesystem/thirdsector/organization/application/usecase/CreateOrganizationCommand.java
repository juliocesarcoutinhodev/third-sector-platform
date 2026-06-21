package br.com.toponesystem.thirdsector.organization.application.usecase;

public record CreateOrganizationCommand(
        String name,
        String cnpj
) {}
