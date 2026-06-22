package br.com.toponesystem.thirdsector.auth.domain.model;

import lombok.Getter;

import java.time.Instant;

@Getter
public class PasswordResetToken {

    private Long id;
    private Long userId;
    private String tokenHash;
    private Instant expiresAt;
    private boolean used;
    private Instant createdAt;

    public PasswordResetToken(Long userId, String tokenHash, Instant expiresAt) {
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.used = false;
        this.createdAt = Instant.now();
    }

    public PasswordResetToken(Long id, Long userId, String tokenHash, Instant expiresAt,
                               boolean used, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.used = used;
        this.createdAt = createdAt;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
