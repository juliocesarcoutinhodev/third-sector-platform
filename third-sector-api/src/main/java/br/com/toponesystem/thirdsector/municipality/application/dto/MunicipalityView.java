package br.com.toponesystem.thirdsector.municipality.application.dto;

import br.com.toponesystem.thirdsector.municipality.domain.model.Municipality;

import java.time.Instant;
import java.util.UUID;

public record MunicipalityView(
        UUID id,
        String name,
        String cnpj,
        String subdomain,
        UUID planId,
        String logo,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {

    public static MunicipalityView from(Municipality m) {
        return new MunicipalityView(
                m.getId(), m.getName(), m.getCnpj(), m.getSubdomain(),
                m.getPlanId(), m.getLogo(), m.isActive(), m.getCreatedAt(), m.getUpdatedAt()
        );
    }
}
