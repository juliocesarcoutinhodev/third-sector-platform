package br.com.toponesystem.thirdsector.plan.domain.model;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class Plan {

    private UUID id;
    private String name;
    private Integer maxOrganizations;
    private Instant createdAt;
    private Instant updatedAt;

    public Plan(String name, Integer maxOrganizations) {
        this.name = name;
        this.maxOrganizations = maxOrganizations;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Plan(UUID id, String name, Integer maxOrganizations,
                Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.maxOrganizations = maxOrganizations;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public boolean isUnlimited() {
        return maxOrganizations == null;
    }

    public boolean hasReachedLimit(long currentCount) {
        if (isUnlimited()) {
            return false;
        }
        return currentCount >= maxOrganizations;
    }

    public void updateMaxOrganizations(Integer maxOrganizations) {
        this.maxOrganizations = maxOrganizations;
        this.updatedAt = Instant.now();
    }
}
