package br.com.toponesystem.thirdsector.organization.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrganizationTest {

    @Test
    void createsOrganizationWithPendingStatus() {
        var org = Organization.create("ACME ONG", "12345678000195");

        assertThat(org.getName()).isEqualTo("ACME ONG");
        assertThat(org.getCnpj()).isEqualTo("12345678000195");
        assertThat(org.getStatus()).isEqualTo(OrganizationStatus.PENDING);
        assertThat(org.getCreatedAt()).isNotNull();
        assertThat(org.getUpdatedAt()).isNotNull();
    }

    @Test
    void hydrationConstructorRebuildsFromPersistence() {
        var hydrated = new Organization(1L, "ACME ONG", "12345678000195",
                OrganizationStatus.ACTIVE, java.time.Instant.EPOCH, java.time.Instant.EPOCH);

        assertThat(hydrated.getId()).isEqualTo(1L);
        assertThat(hydrated.getName()).isEqualTo("ACME ONG");
        assertThat(hydrated.getCnpj()).isEqualTo("12345678000195");
        assertThat(hydrated.getStatus()).isEqualTo(OrganizationStatus.ACTIVE);
    }
}
