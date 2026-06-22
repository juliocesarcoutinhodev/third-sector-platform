package br.com.toponesystem.thirdsector.auth.application.usecase;

import br.com.toponesystem.thirdsector.auth.domain.model.Role;

import java.util.UUID;

public record CreateUserCommand(
        String name,
        String email,
        String password,
        Role role,
        UUID organizationId
) {}
