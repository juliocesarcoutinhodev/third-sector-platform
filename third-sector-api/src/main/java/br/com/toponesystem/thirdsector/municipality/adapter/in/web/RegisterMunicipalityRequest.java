package br.com.toponesystem.thirdsector.municipality.adapter.in.web;

import br.com.toponesystem.thirdsector.municipality.domain.model.Plan;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.br.CNPJ;

record RegisterMunicipalityRequest(
        @NotBlank String name,
        @CNPJ @NotBlank String cnpj,
        @NotBlank
        @Pattern(regexp = "^[a-z0-9]([a-z0-9-]*[a-z0-9])?$",
                message = "Subdomain must contain only lowercase letters, numbers, and hyphens")
        String subdomain,
        @NotNull Plan plan
) {}
