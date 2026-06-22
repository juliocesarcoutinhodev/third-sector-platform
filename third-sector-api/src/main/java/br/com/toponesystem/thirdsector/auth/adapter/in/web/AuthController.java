package br.com.toponesystem.thirdsector.auth.adapter.in.web;

import br.com.toponesystem.thirdsector.auth.application.usecase.LoginUseCase;
import br.com.toponesystem.thirdsector.auth.application.usecase.LogoutUseCase;
import br.com.toponesystem.thirdsector.auth.application.usecase.ConfirmPasswordResetUseCase;
import br.com.toponesystem.thirdsector.auth.application.usecase.RequestPasswordResetUseCase;
import br.com.toponesystem.thirdsector.auth.application.usecase.TokenService;
import br.com.toponesystem.thirdsector.shared.adapter.in.web.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
class AuthController {

    private final LoginUseCase loginUseCase;
    private final TokenService tokenService;
    private final LogoutUseCase logoutUseCase;
    private final RequestPasswordResetUseCase requestPasswordResetUseCase;
    private final ConfirmPasswordResetUseCase confirmPasswordResetUseCase;
    private final LoginRequestMapper requestMapper;
    private final PasswordResetMapper passwordResetMapper;
    private final AuthCookieManager cookieManager;

    @PostMapping("/login")
    ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        var command = requestMapper.toCommand(request);
        var result = loginUseCase.execute(command);

        var tokenPair = tokenService.createTokenPair(result);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieManager.buildAccessCookie(tokenPair.accessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, cookieManager.buildRefreshCookie(tokenPair.refreshToken()).toString())
                .body(LoginResponse.from(result));
    }

    @PostMapping("/refresh")
    ResponseEntity<LoginResponse> refresh(HttpServletRequest request) {
        var refreshTokenValue = cookieManager.extractRefreshToken(request);

        var tokenPair = tokenService.rotateRefreshToken(refreshTokenValue);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieManager.buildAccessCookie(tokenPair.accessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, cookieManager.buildRefreshCookie(tokenPair.refreshToken()).toString())
                .body(LoginResponse.empty());
    }

    @PostMapping("/logout")
    ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        var rawToken = cookieManager.extractRefreshTokenOrNull(request);
        logoutUseCase.execute(rawToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieManager.clearAccessCookie().toString())
                .header(HttpHeaders.SET_COOKIE, cookieManager.clearRefreshCookie().toString())
                .body(ApiResponse.success("Logout realizado com sucesso."));
    }

    @PostMapping("/password-reset/request")
    ResponseEntity<ApiResponse<Void>> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequest request) {
        requestPasswordResetUseCase.execute(passwordResetMapper.toCommand(request));
        return ResponseEntity.ok(
                ApiResponse.success("Se o e-mail estiver cadastrado, um link de redefinição foi enviado."));
    }

    @PostMapping("/password-reset/confirm")
    ResponseEntity<ApiResponse<Void>> confirmPasswordReset(
            @Valid @RequestBody PasswordResetConfirm request) {
        confirmPasswordResetUseCase.execute(passwordResetMapper.toCommand(request));
        return ResponseEntity.ok(ApiResponse.success("Senha redefinida com sucesso."));
    }
}
