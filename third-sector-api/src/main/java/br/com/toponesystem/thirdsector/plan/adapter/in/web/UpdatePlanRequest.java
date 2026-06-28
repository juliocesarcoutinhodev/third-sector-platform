package br.com.toponesystem.thirdsector.plan.adapter.in.web;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

record UpdatePlanRequest(
        @NotNull Integer maxOrganizations
) {}
