package br.com.toponesystem.thirdsector.organization.application.dto;

import br.com.toponesystem.thirdsector.organization.domain.model.Organization;
import br.com.toponesystem.thirdsector.organization.domain.model.OrganizationStatus;

import java.time.Instant;

public record OrganizationView(
        Long id,
        String name,
        String cnpj,
        OrganizationStatus status,
        Instant createdAt,
        Instant updatedAt
) {

    public static OrganizationView from(Organization org) {
        return new OrganizationView(
                org.getId(), org.getName(), org.getCnpj(),
                org.getStatus(), org.getCreatedAt(), org.getUpdatedAt()
        );
    }
}
