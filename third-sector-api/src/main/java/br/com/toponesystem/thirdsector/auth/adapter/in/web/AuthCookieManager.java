package br.com.toponesystem.thirdsector.auth.adapter.in.web;

import br.com.toponesystem.thirdsector.auth.adapter.out.security.JwtProperties;
import br.com.toponesystem.thirdsector.auth.application.usecase.RefreshTokenProperties;
import br.com.toponesystem.thirdsector.auth.domain.exception.InvalidRefreshTokenException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
class AuthCookieManager {

    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";

    private final JwtProperties jwtProperties;
    private final RefreshTokenProperties refreshTokenProperties;

    AuthCookieManager(JwtProperties jwtProperties, RefreshTokenProperties refreshTokenProperties) {
        this.jwtProperties = jwtProperties;
        this.refreshTokenProperties = refreshTokenProperties;
    }

    ResponseCookie buildAccessCookie(String token) {
        return ResponseCookie.from(ACCESS_TOKEN, token)
                .httpOnly(true)
                .secure(jwtProperties.cookieSecure())
                .sameSite("Lax")
                .path("/")
                .maxAge(jwtProperties.expiration() / 1000)
                .build();
    }

    ResponseCookie buildRefreshCookie(String token) {
        return ResponseCookie.from(REFRESH_TOKEN, token)
                .httpOnly(true)
                .secure(jwtProperties.cookieSecure())
                .sameSite("Lax")
                .path("/api/auth/refresh")
                .maxAge(refreshTokenProperties.expiration() / 1000)
                .build();
    }

    ResponseCookie clearAccessCookie() {
        return ResponseCookie.from(ACCESS_TOKEN, "")
                .httpOnly(true)
                .secure(jwtProperties.cookieSecure())
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
    }

    ResponseCookie clearRefreshCookie() {
        return ResponseCookie.from(REFRESH_TOKEN, "")
                .httpOnly(true)
                .secure(jwtProperties.cookieSecure())
                .sameSite("Lax")
                .path("/api/auth/refresh")
                .maxAge(0)
                .build();
    }

    String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            throw new InvalidRefreshTokenException();
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> REFRESH_TOKEN.equals(c.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(InvalidRefreshTokenException::new);
    }

    String extractRefreshTokenOrNull(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> REFRESH_TOKEN.equals(c.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }
}
