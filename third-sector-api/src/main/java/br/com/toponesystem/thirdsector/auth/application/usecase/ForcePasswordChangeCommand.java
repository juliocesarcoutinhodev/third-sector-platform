package br.com.toponesystem.thirdsector.auth.application.usecase;

import java.util.UUID;

public record ForcePasswordChangeCommand(
        UUID userId,
        String tenantId,
        String newPassword
) {}
