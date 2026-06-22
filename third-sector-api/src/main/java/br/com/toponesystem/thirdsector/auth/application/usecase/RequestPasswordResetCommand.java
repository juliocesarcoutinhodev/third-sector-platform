package br.com.toponesystem.thirdsector.auth.application.usecase;

public record RequestPasswordResetCommand(
        String email
) {}
