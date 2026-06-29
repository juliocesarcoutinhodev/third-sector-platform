package br.com.toponesystem.thirdsector.municipality.domain.model;

import lombok.Getter;

import java.time.Instant;

@Getter
public class MunicipalityAdminCreatedEvent {

    private final String adminName;
    private final String adminEmail;
    private final String temporaryPassword;
    private final String municipalityName;
    private final String tenantId;
    private final Instant occurredAt;

    public MunicipalityAdminCreatedEvent(String adminName, String adminEmail,
                                          String temporaryPassword, String municipalityName,
                                          String tenantId) {
        this.adminName = adminName;
        this.adminEmail = adminEmail;
        this.temporaryPassword = temporaryPassword;
        this.municipalityName = municipalityName;
        this.tenantId = tenantId;
        this.occurredAt = Instant.now();
    }
}
