package br.com.toponesystem.thirdsector.auth.domain.model;

import lombok.Getter;

import java.time.Instant;

@Getter
public class SuspiciousTokenReuseDetectedEvent {

    private final String userName;
    private final String userEmail;
    private final String tenantId;
    private final String familyId;
    private final Instant occurredAt;

    public SuspiciousTokenReuseDetectedEvent(String userName, String userEmail,
                                              String tenantId, String familyId) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.tenantId = tenantId;
        this.familyId = familyId;
        this.occurredAt = Instant.now();
    }
}
