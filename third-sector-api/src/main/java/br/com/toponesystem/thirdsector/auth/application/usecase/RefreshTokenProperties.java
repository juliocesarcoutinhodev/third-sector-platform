package br.com.toponesystem.thirdsector.auth.application.usecase;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.refresh-token")
public record RefreshTokenProperties(
        long expiration
) {}
