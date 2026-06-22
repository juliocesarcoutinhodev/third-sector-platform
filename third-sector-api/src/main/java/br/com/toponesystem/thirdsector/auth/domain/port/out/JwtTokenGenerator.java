package br.com.toponesystem.thirdsector.auth.domain.port.out;

public interface JwtTokenGenerator {

    String generate(Long userId, String role, String tenantId, Long organizationId);
}
