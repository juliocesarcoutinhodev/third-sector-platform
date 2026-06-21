package br.com.toponesystem.thirdsector.auth.adapter.in.web;

import br.com.toponesystem.thirdsector.auth.adapter.out.security.JwtProperties;
import br.com.toponesystem.thirdsector.auth.application.usecase.LoginUseCase;
import br.com.toponesystem.thirdsector.auth.domain.port.out.JwtTokenGenerator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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
    private final JwtTokenGenerator tokenGenerator;
    private final LoginRequestMapper requestMapper;
    private final JwtProperties jwtProperties;

    @PostMapping("/login")
    ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        var command = requestMapper.toCommand(request);
        var result = loginUseCase.execute(command);

        var token = tokenGenerator.generate(
                result.userId(), result.role(), result.tenantId(), result.organizationId());

        var cookie = ResponseCookie.from("access_token", token)
                .httpOnly(true)
                .secure(jwtProperties.cookieSecure())
                .sameSite("Lax")
                .path("/")
                .maxAge(jwtProperties.expiration() / 1000)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(LoginResponse.from(result));
    }
}
