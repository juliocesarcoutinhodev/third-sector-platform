package br.com.toponesystem.thirdsector.auth.application.usecase;

public record ConfirmPasswordResetCommand(
        String token,
        String newPassword
) {}
