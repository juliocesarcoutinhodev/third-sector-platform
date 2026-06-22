package br.com.toponesystem.thirdsector.auth.domain.model;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class RefreshToken {

    private UUID id;
    private UUID userId;
    private String tokenHash;
    private Instant expiresAt;
    private boolean revoked;
    private Instant createdAt;
    private String familyId;

    public RefreshToken(UUID userId, String tokenHash, Instant expiresAt, String familyId) {
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.revoked = false;
        this.createdAt = Instant.now();
        this.familyId = familyId;
    }

    public RefreshToken(UUID id, UUID userId, String tokenHash, Instant expiresAt,
                        boolean revoked, Instant createdAt, String familyId) {
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
        this.createdAt = createdAt;
        this.familyId = familyId;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
