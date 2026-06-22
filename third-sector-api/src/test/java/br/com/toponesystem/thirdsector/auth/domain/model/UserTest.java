package br.com.toponesystem.thirdsector.auth.domain.model;

import br.com.toponesystem.thirdsector.auth.domain.exception.InvalidUserRoleAssignmentException;
import org.junit.jupiter.api.Test;

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
        var user = User.create("Org Mgr", "orgmgr@example.com", "hash", Role.ORGANIZATION_MANAGER, 10L);

        assertThat(user.getRole()).isEqualTo(Role.ORGANIZATION_MANAGER);
        assertThat(user.getOrganizationId()).isEqualTo(10L);
    }

    @Test
    void createsOperatorWithOrganization() {
        var user = User.create("Operator", "op@example.com", "hash", Role.OPERATOR, 20L);

        assertThat(user.getRole()).isEqualTo(Role.OPERATOR);
        assertThat(user.getOrganizationId()).isEqualTo(20L);
    }

    @Test
    void rejectsSuperAdminWithOrganization() {
        assertThatThrownBy(() ->
                User.create("Admin", "admin@example.com", "hash", Role.SUPER_ADMIN, 5L)
        ).isInstanceOf(InvalidUserRoleAssignmentException.class)
                .hasMessageContaining("SUPER_ADMIN")
                .hasMessageContaining("não permite vínculo com organização.");
    }

    @Test
    void rejectsMunicipalityAdmWithOrganization() {
        assertThatThrownBy(() ->
                User.create("Mun Adm", "munadm@example.com", "hash", Role.MUNICIPALITY_ADM, 5L)
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
        var hydrated = new User(1L, "Name", "email@example.com", "hash", Role.MUNICIPALITY_ADM,
                null, true, java.time.Instant.EPOCH, java.time.Instant.EPOCH);

        assertThat(hydrated.getId()).isEqualTo(1L);
        assertThat(hydrated.getEmail()).isEqualTo("email@example.com");
        assertThat(hydrated.getRole()).isEqualTo(Role.MUNICIPALITY_ADM);
        assertThat(hydrated.getOrganizationId()).isNull();
    }

    @Test
    void factoryMethodDoesNotValidateOnHydrationConstructor() {
        assertThatCode(() ->
                new User(1L, "Name", "email@example.com", "hash", Role.SUPER_ADMIN,
                        5L, true, java.time.Instant.EPOCH, java.time.Instant.EPOCH)
        ).doesNotThrowAnyException();
    }
}
