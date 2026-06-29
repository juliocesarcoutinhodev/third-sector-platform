package br.com.toponesystem.thirdsector.auth.application.dto;

import java.util.UUID;

public record LoginResult(
        UUID userId,
        String name,
        String email,
        String role,
        String tenantId,
        UUID organizationId,
        boolean mustChangePassword
) {}
