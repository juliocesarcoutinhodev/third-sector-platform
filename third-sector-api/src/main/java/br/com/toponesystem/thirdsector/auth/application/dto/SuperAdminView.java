package br.com.toponesystem.thirdsector.auth.application.dto;

import br.com.toponesystem.thirdsector.auth.domain.model.SuperAdmin;

import java.time.Instant;
import java.util.UUID;

public record SuperAdminView(
        UUID id,
        String name,
        String email,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {

    public static SuperAdminView from(SuperAdmin sa) {
        return new SuperAdminView(
                sa.getId(), sa.getName(), sa.getEmail(),
                sa.isActive(), sa.getCreatedAt(), sa.getUpdatedAt()
        );
    }
}
