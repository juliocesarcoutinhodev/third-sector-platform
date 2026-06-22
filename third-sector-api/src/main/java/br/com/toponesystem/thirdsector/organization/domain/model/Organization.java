package br.com.toponesystem.thirdsector.organization.domain.model;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class Organization {

    private UUID id;
    private String name;
    private String cnpj;
    private OrganizationStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public static Organization create(String name, String cnpj) {
        return new Organization(name, cnpj);
    }

    private Organization(String name, String cnpj) {
        this.name = name;
        this.cnpj = cnpj;
        this.status = OrganizationStatus.PENDING;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Organization(UUID id, String name, String cnpj, OrganizationStatus status,
                        Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.cnpj = cnpj;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
