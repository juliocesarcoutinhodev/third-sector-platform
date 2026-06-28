package br.com.toponesystem.thirdsector.auth.application.usecase;

public record CreateSuperAdminCommand(
        String name,
        String email
) {}
