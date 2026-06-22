package br.com.toponesystem.thirdsector.auth.domain.model;

import lombok.Getter;

import java.time.Instant;

@Getter
public class PasswordResetRequestedEvent {

    private final String userName;
    private final String userEmail;
    private final String resetToken;
    private final String tenantId;
    private final Instant occurredAt;

    public PasswordResetRequestedEvent(String userName, String userEmail, String resetToken,
                                        String tenantId) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.resetToken = resetToken;
        this.tenantId = tenantId;
        this.occurredAt = Instant.now();
    }
}
