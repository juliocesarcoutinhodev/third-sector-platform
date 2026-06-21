package br.com.toponesystem.thirdsector.auth.domain.model;

import lombok.Getter;

import java.time.Instant;

@Getter
public class RefreshToken {

    private Long id;
    private Long userId;
    private String tokenHash;
    private Instant expiresAt;
    private boolean revoked;
    private Instant createdAt;
    private String familyId;

    public RefreshToken(Long userId, String tokenHash, Instant expiresAt, String familyId) {
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.revoked = false;
        this.createdAt = Instant.now();
        this.familyId = familyId;
    }

    public RefreshToken(Long id, Long userId, String tokenHash, Instant expiresAt,
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
