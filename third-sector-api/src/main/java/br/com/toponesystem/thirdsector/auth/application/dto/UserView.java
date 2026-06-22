package br.com.toponesystem.thirdsector.auth.application.dto;

import br.com.toponesystem.thirdsector.auth.domain.model.Role;
import br.com.toponesystem.thirdsector.auth.domain.model.User;

import java.time.Instant;

public record UserView(
        Long id,
        String name,
        String email,
        Role role,
        Long organizationId,
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
