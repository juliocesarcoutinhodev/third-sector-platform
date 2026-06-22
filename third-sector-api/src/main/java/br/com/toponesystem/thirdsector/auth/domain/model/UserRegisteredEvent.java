package br.com.toponesystem.thirdsector.auth.domain.model;

import lombok.Getter;

import java.time.Instant;

@Getter
public class UserRegisteredEvent {

    private final String userName;
    private final String userEmail;
    private final String tenantId;
    private final Instant occurredAt;

    public UserRegisteredEvent(String userName, String userEmail, String tenantId) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.tenantId = tenantId;
        this.occurredAt = Instant.now();
    }
}
