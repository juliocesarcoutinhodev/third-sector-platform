package br.com.toponesystem.thirdsector.auth.application.usecase;

public record LoginCommand(
        String email,
        String password
) {}
