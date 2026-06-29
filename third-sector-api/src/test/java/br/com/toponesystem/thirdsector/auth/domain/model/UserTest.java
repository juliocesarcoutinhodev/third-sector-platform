package br.com.toponesystem.thirdsector.auth.domain.model;

import br.com.toponesystem.thirdsector.auth.domain.exception.InvalidUserRoleAssignmentException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    @Test
    void createsSuperAdminWithoutOrganization() {
        var user = User.create("Admin", "admin@example.com", "hash", Role.SUPER_ADMIN, null);

        assertThat(user.getRole()).isEqualTo(Role.SUPER_ADMIN);
        assertThat(user.getOrganizationId()).isNull();
        assertThat(user.isActive()).isTrue();
        assertThat(user.getCreatedAt()).isNotNull();
    }

    @Test
    void createsMunicipalityAdmWithoutOrganization() {
        var user = User.create("Mun Adm", "munadm@example.com", "hash", Role.MUNICIPALITY_ADM, null);

        assertThat(user.getRole()).isEqualTo(Role.MUNICIPALITY_ADM);
        assertThat(user.getOrganizationId()).isNull();
    }

    @Test
    void createsOrganizationManagerWithOrganization() {
        var user = User.create("Org Mgr", "orgmgr@example.com", "hash", Role.ORGANIZATION_MANAGER, UUID.fromString("00000000-0000-0000-0000-00000000000a"));

        assertThat(user.getRole()).isEqualTo(Role.ORGANIZATION_MANAGER);
        assertThat(user.getOrganizationId()).isEqualTo(UUID.fromString("00000000-0000-0000-0000-00000000000a"));
    }

    @Test
    void createsOperatorWithOrganization() {
        var user = User.create("Operator", "op@example.com", "hash", Role.OPERATOR, UUID.fromString("00000000-0000-0000-0000-000000000014"));

        assertThat(user.getRole()).isEqualTo(Role.OPERATOR);
        assertThat(user.getOrganizationId()).isEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000014"));
    }

    @Test
    void rejectsSuperAdminWithOrganization() {
        assertThatThrownBy(() ->
                User.create("Admin", "admin@example.com", "hash", Role.SUPER_ADMIN, UUID.fromString("00000000-0000-0000-0000-000000000005"))
        ).isInstanceOf(InvalidUserRoleAssignmentException.class)
                .hasMessageContaining("SUPER_ADMIN")
                .hasMessageContaining("não permite vínculo com organização.");
    }

    @Test
    void rejectsMunicipalityAdmWithOrganization() {
        assertThatThrownBy(() ->
                User.create("Mun Adm", "munadm@example.com", "hash", Role.MUNICIPALITY_ADM, UUID.fromString("00000000-0000-0000-0000-000000000005"))
        ).isInstanceOf(InvalidUserRoleAssignmentException.class)
                .hasMessageContaining("MUNICIPALITY_ADM")
                .hasMessageContaining("não permite vínculo com organização.");
    }

    @Test
    void rejectsOrganizationManagerWithoutOrganization() {
        assertThatThrownBy(() ->
                User.create("Org Mgr", "orgmgr@example.com", "hash", Role.ORGANIZATION_MANAGER, null)
        ).isInstanceOf(InvalidUserRoleAssignmentException.class)
                .hasMessageContaining("ORGANIZATION_MANAGER")
                .hasMessageContaining("exige vínculo com uma organização.");
    }

    @Test
    void rejectsOperatorWithoutOrganization() {
        assertThatThrownBy(() ->
                User.create("Operator", "op@example.com", "hash", Role.OPERATOR, null)
        ).isInstanceOf(InvalidUserRoleAssignmentException.class)
                .hasMessageContaining("OPERATOR")
                .hasMessageContaining("exige vínculo com uma organização.");
    }

    @Test
    void hydrationConstructorRebuildsFromPersistence() {
        var hydrated = new User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Name", "email@example.com", "hash", Role.MUNICIPALITY_ADM,
                null, true, java.time.Instant.EPOCH, java.time.Instant.EPOCH, false);

        assertThat(hydrated.getId()).isEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        assertThat(hydrated.getEmail()).isEqualTo("email@example.com");
        assertThat(hydrated.getRole()).isEqualTo(Role.MUNICIPALITY_ADM);
        assertThat(hydrated.getOrganizationId()).isNull();
    }

    @Test
    void factoryMethodDoesNotValidateOnHydrationConstructor() {
        assertThatCode(() ->
                new User(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Name", "email@example.com", "hash", Role.SUPER_ADMIN,
                        UUID.fromString("00000000-0000-0000-0000-000000000005"), true, java.time.Instant.EPOCH, java.time.Instant.EPOCH, false)
        ).doesNotThrowAnyException();
    }
}
