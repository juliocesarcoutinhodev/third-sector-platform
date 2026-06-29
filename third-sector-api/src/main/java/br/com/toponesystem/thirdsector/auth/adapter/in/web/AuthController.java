package br.com.toponesystem.thirdsector.auth.adapter.in.web;

import br.com.toponesystem.thirdsector.auth.application.usecase.ConfirmPasswordResetUseCase;
import br.com.toponesystem.thirdsector.auth.application.usecase.ForcePasswordChangeCommand;
import br.com.toponesystem.thirdsector.auth.application.usecase.ForcePasswordChangeUseCase;
import br.com.toponesystem.thirdsector.auth.application.usecase.LoginUseCase;
import br.com.toponesystem.thirdsector.auth.application.usecase.LogoutUseCase;
import br.com.toponesystem.thirdsector.auth.application.usecase.RequestPasswordResetUseCase;
import br.com.toponesystem.thirdsector.auth.application.usecase.TokenService;
import br.com.toponesystem.thirdsector.shared.adapter.in.web.ApiResponse;
import br.com.toponesystem.thirdsector.shared.adapter.in.web.TenantAuthenticationToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
    private final ForcePasswordChangeUseCase forcePasswordChangeUseCase;
    private final LoginRequestMapper requestMapper;
    private final PasswordResetMapper passwordResetMapper;
    private final AuthCookieManager cookieManager;

    @PostMapping("/login")
    ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        var result = loginUseCase.execute(requestMapper.toCommand(request));
        var tokenPair = tokenService.createTokenPair(result);
        var builder = ResponseEntity.ok();
        cookieManager.buildTokenCookieHeaders(tokenPair)
                .forEach(cookie -> builder.header(HttpHeaders.SET_COOKIE, cookie));
        return builder.body(ApiResponse.success("Login realizado com sucesso.", LoginResponse.from(result)));
    }

    @PostMapping("/refresh")
    ResponseEntity<ApiResponse<LoginResponse>> refresh(HttpServletRequest request) {
        var refreshTokenValue = cookieManager.extractRefreshToken(request);
        var tokenPair = tokenService.rotateRefreshToken(refreshTokenValue);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieManager.buildAccessCookie(tokenPair.accessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, cookieManager.buildRefreshCookie(tokenPair.refreshToken()).toString())
                .body(ApiResponse.success("Sessão renovada com sucesso.", LoginResponse.empty()));
    }

    @PostMapping("/logout")
    ResponseEntity<Void> logout(HttpServletRequest request) {
        var rawToken = cookieManager.extractRefreshTokenOrNull(request);
        logoutUseCase.execute(rawToken);

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookieManager.clearAccessCookie().toString())
                .header(HttpHeaders.SET_COOKIE, cookieManager.clearRefreshCookie().toString())
                .build();
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

    @PostMapping("/force-password-change")
    @PreAuthorize("hasRole('FORCE_PASSWORD_CHANGE')")
    ResponseEntity<ApiResponse<LoginResponse>> forcePasswordChange(
            @Valid @RequestBody ForcePasswordChangeRequest request,
            Authentication authentication) {
        var auth = (TenantAuthenticationToken) authentication;
        var command = new ForcePasswordChangeCommand(auth.getUserId(), auth.getTenantId(), request.newPassword());
        var result = forcePasswordChangeUseCase.execute(command);
        var tokenPair = tokenService.createTokenPair(result);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieManager.buildAccessCookie(tokenPair.accessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, cookieManager.buildRefreshCookie(tokenPair.refreshToken()).toString())
                .body(ApiResponse.success("Senha alterada com sucesso. Acesso completo liberado.",
                        LoginResponse.from(result)));
    }
}
