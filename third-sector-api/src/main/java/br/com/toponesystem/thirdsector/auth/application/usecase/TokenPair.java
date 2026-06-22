package br.com.toponesystem.thirdsector.auth.application.usecase;

public record TokenPair(
        String accessToken,
        String refreshToken
) {}
