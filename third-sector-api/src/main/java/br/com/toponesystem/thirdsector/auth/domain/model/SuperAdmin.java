package br.com.toponesystem.thirdsector.auth.domain.model;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class SuperAdmin {

    private UUID id;
    private String name;
    private String email;
    private String passwordHash;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public static SuperAdmin create(String name, String email, String passwordHash) {
        return new SuperAdmin(name, email, passwordHash);
    }

    public SuperAdmin(UUID id, String name, String email, String passwordHash,
                      boolean active, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    private SuperAdmin(String name, String email, String passwordHash) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.active = true;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
}
