package br.com.toponesystem.thirdsector.municipality.domain.model;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class Municipality {

    private UUID id;
    private String name;
    private String cnpj;
    private String subdomain;
    private Plan plan;
    private String logo;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public Municipality(String name, String cnpj, String subdomain, Plan plan, String logo) {
        this.name = name;
        this.cnpj = cnpj;
        this.subdomain = subdomain;
        this.plan = plan;
        this.logo = logo;
        this.active = true;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Municipality(UUID id, String name, String cnpj, String subdomain, Plan plan,
                        String logo, boolean active, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.cnpj = cnpj;
        this.subdomain = subdomain;
        this.plan = plan;
        this.logo = logo;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = Instant.now();
    }
}
