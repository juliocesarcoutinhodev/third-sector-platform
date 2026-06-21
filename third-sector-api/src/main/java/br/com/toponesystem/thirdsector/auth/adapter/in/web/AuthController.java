package br.com.toponesystem.thirdsector.auth.adapter.in.web;

import br.com.toponesystem.thirdsector.auth.adapter.out.security.JwtProperties;
import br.com.toponesystem.thirdsector.auth.application.usecase.LoginUseCase;
import br.com.toponesystem.thirdsector.auth.application.usecase.LogoutUseCase;
import br.com.toponesystem.thirdsector.auth.application.usecase.RefreshTokenProperties;
import br.com.toponesystem.thirdsector.auth.application.usecase.TokenService;
import br.com.toponesystem.thirdsector.auth.domain.exception.InvalidRefreshTokenException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
class AuthController {

    private final LoginUseCase loginUseCase;
    private final TokenService tokenService;
    private final LogoutUseCase logoutUseCase;
    private final LoginRequestMapper requestMapper;
    private final JwtProperties jwtProperties;
    private final RefreshTokenProperties refreshTokenProperties;

    @PostMapping("/login")
    ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        var command = requestMapper.toCommand(request);
        var result = loginUseCase.execute(command);

        var tokenPair = tokenService.createTokenPair(result);

        var accessCookie = buildAccessCookie(tokenPair.accessToken());
        var refreshCookie = buildRefreshCookie(tokenPair.refreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(LoginResponse.from(result));
    }

    @PostMapping("/refresh")
    ResponseEntity<LoginResponse> refresh(HttpServletRequest request) {
        var refreshTokenValue = extractCookie(request, "refresh_token");

        var tokenPair = tokenService.rotateRefreshToken(refreshTokenValue);

        var accessCookie = buildAccessCookie(tokenPair.accessToken());
        var refreshCookie = buildRefreshCookie(tokenPair.refreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(LoginResponse.empty());
    }

    @PostMapping("/logout")
    ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        var rawToken = extractCookieOrNull(request, "refresh_token");
        logoutUseCase.execute(rawToken);

        var clearedAccess = clearCookie("access_token", "/");
        var clearedRefresh = clearCookie("refresh_token", "/api/auth/refresh");

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearedAccess.toString())
                .header(HttpHeaders.SET_COOKIE, clearedRefresh.toString())
                .body(Map.of("message", "Logged out successfully"));
    }

    private ResponseCookie buildAccessCookie(String token) {
        return ResponseCookie.from("access_token", token)
                .httpOnly(true)
                .secure(jwtProperties.cookieSecure())
                .sameSite("Lax")
                .path("/")
                .maxAge(jwtProperties.expiration() / 1000)
                .build();
    }

    private ResponseCookie buildRefreshCookie(String token) {
        return ResponseCookie.from("refresh_token", token)
                .httpOnly(true)
                .secure(jwtProperties.cookieSecure())
                .sameSite("Lax")
                .path("/api/auth/refresh")
                .maxAge(refreshTokenProperties.expiration() / 1000)
                .build();
    }

    private ResponseCookie clearCookie(String name, String path) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(jwtProperties.cookieSecure())
                .sameSite("Lax")
                .path(path)
                .maxAge(0)
                .build();
    }

    private String extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            throw new InvalidRefreshTokenException();
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(InvalidRefreshTokenException::new);
    }

    private String extractCookieOrNull(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return null;
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }
}
