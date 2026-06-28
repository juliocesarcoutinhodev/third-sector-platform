package br.com.toponesystem.thirdsector.auth.adapter.in.web;

import br.com.toponesystem.thirdsector.auth.application.usecase.SuperAdminLoginUseCase;
import br.com.toponesystem.thirdsector.auth.domain.port.out.JwtTokenGenerator;
import br.com.toponesystem.thirdsector.shared.adapter.in.web.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/super-admin")
@RequiredArgsConstructor
class SuperAdminAuthController {

    private final SuperAdminLoginUseCase loginUseCase;
    private final JwtTokenGenerator jwtTokenGenerator;
    private final AuthCookieManager cookieManager;
    private final SuperAdminLoginRequestMapper requestMapper;

    @PostMapping("/login")
    ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody SuperAdminLoginRequest request) {
        var command = requestMapper.toCommand(request);
        var result = loginUseCase.execute(command);

        var accessToken = jwtTokenGenerator.generate(
                result.userId(), result.role(),
                result.tenantId(), result.organizationId());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieManager.buildAccessCookie(accessToken).toString())
                .body(ApiResponse.success("Login realizado com sucesso.", LoginResponse.from(result)));
    }
}
