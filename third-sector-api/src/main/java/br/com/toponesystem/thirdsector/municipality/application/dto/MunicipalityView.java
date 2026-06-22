package br.com.toponesystem.thirdsector.municipality.application.dto;

import br.com.toponesystem.thirdsector.municipality.domain.model.Municipality;
import br.com.toponesystem.thirdsector.municipality.domain.model.Plan;

import java.time.Instant;

public record MunicipalityView(
        Long id,
        String name,
        String cnpj,
        String subdomain,
        Plan plan,
        String logo,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {

    public static MunicipalityView from(Municipality m) {
        return new MunicipalityView(
                m.getId(), m.getName(), m.getCnpj(), m.getSubdomain(),
                m.getPlan(), m.getLogo(), m.isActive(), m.getCreatedAt(), m.getUpdatedAt()
        );
    }
}
