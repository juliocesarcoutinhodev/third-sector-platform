package br.com.toponesystem.thirdsector.auth.adapter.out.security;

import br.com.toponesystem.thirdsector.auth.domain.port.out.JwtTokenGenerator;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
class JjwtTokenGenerator implements JwtTokenGenerator {

    private final JwtProperties properties;
    private final SecretKey signingKey;

    JjwtTokenGenerator(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = new SecretKeySpec(
                properties.secret().getBytes(StandardCharsets.UTF_8),
                "HmacSHA256");
    }

    @Override
    public String generate(UUID userId, String role, String tenantId, UUID organizationId) {
        var now = Instant.now();

        var builder = Jwts.builder()
                .subject(userId.toString())
                .claim("role", role)
                .claim("tenantId", tenantId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(properties.expiration())));

        if (organizationId != null) {
            builder.claim("organizationId", organizationId.toString());
        }

        return builder.signWith(signingKey).compact();
    }
}
