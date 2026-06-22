package br.com.toponesystem.thirdsector.auth.application.usecase;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.password-reset")
public record PasswordResetProperties(
        long expiration
) {}
