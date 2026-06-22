package br.com.toponesystem.thirdsector.auth.domain.model;

import br.com.toponesystem.thirdsector.auth.domain.exception.InvalidUserRoleAssignmentException;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class User {

    private UUID id;
    private String name;
    private String email;
    private String passwordHash;
    private Role role;
    private UUID organizationId;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public static User create(String name, String email, String passwordHash,
                               Role role, UUID organizationId) {
        validateRoleOrganizationBinding(role, organizationId);
        return new User(name, email, passwordHash, role, organizationId);
    }

    public User(UUID id, String name, String email, String passwordHash, Role role,
                UUID organizationId, boolean active, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.organizationId = organizationId;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    private User(String name, String email, String passwordHash, Role role, UUID organizationId) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.organizationId = organizationId;
        this.active = true;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    private static void validateRoleOrganizationBinding(Role role, UUID organizationId) {
        switch (role) {
            case SUPER_ADMIN, MUNICIPALITY_ADM -> {
                if (organizationId != null) {
                    throw new InvalidUserRoleAssignmentException(
                            "O papel " + role + " não permite vínculo com organização.");
                }
            }
            case ORGANIZATION_MANAGER, OPERATOR -> {
                if (organizationId == null) {
                    throw new InvalidUserRoleAssignmentException(
                            "O papel " + role + " exige vínculo com uma organização.");
                }
            }
        }
    }
}
