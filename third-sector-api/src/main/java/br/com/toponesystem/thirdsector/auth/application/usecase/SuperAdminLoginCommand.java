package br.com.toponesystem.thirdsector.auth.application.usecase;

public record SuperAdminLoginCommand(
        String email,
        String password
) {}
