package br.com.toponesystem.thirdsector.municipality.domain.model;

import lombok.Getter;

import java.time.Instant;

@Getter
public class Municipality {

    private Long id;
    private String name;
    private String cnpj;
    private String subdomain;
    private Plan plan;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public Municipality(String name, String cnpj, String subdomain, Plan plan) {
        this.name = name;
        this.cnpj = cnpj;
        this.subdomain = subdomain;
        this.plan = plan;
        this.active = true;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Municipality(Long id, String name, String cnpj, String subdomain, Plan plan,
                        boolean active, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.cnpj = cnpj;
        this.subdomain = subdomain;
        this.plan = plan;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = Instant.now();
    }
}
