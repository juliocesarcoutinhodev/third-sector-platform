package br.com.toponesystem.thirdsector.auth.application.dto;

public record LoginResult(
        Long userId,
        String name,
        String email,
        String role,
        String tenantId,
        Long organizationId
) {}
