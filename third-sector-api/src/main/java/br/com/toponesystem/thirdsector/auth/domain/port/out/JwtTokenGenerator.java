package br.com.toponesystem.thirdsector.auth.domain.port.out;

import java.util.UUID;

public interface JwtTokenGenerator {

    String generate(UUID userId, String role, String tenantId, UUID organizationId);
}
