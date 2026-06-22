package br.com.toponesystem.thirdsector.auth.application.dto;

import br.com.toponesystem.thirdsector.auth.domain.model.Role;
import br.com.toponesystem.thirdsector.auth.domain.model.User;

import java.time.Instant;
import java.util.UUID;

public record UserView(
        UUID id,
        String name,
        String email,
        Role role,
        UUID organizationId,
        boolean active,
        Instant createdAt
) {

    public static UserView from(User user) {
        return new UserView(
                user.getId(), user.getName(), user.getEmail(), user.getRole(),
                user.getOrganizationId(), user.isActive(), user.getCreatedAt()
        );
    }
}
