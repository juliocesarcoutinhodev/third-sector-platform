package br.com.toponesystem.thirdsector.auth.adapter.out.security;

import br.com.toponesystem.thirdsector.auth.domain.port.out.JwtTokenGenerator;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

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
    public String generate(Long userId, String role, String tenantId, Long organizationId) {
        var now = Instant.now();

        var builder = Jwts.builder()
                .subject(userId.toString())
                .claim("role", role)
                .claim("tenantId", tenantId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(properties.expiration())));

        if (organizationId != null) {
            builder.claim("organizationId", organizationId);
        }

        return builder.signWith(signingKey).compact();
    }
}
