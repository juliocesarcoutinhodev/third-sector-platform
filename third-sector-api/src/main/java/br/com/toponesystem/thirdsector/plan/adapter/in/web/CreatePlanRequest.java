package br.com.toponesystem.thirdsector.plan.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record CreatePlanRequest(
        @NotBlank @Size(max = 50) String name,
        Integer maxOrganizations
) {}
