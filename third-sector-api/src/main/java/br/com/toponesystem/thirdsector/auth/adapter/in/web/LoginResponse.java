package br.com.toponesystem.thirdsector.auth.adapter.in.web;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
record LoginResponse(
        UUID userId,
        String name,
        String email,
        String role,
        UUID organizationId
) {

    static LoginResponse from(br.com.toponesystem.thirdsector.auth.application.dto.LoginResult result) {
        return new LoginResponse(
                result.userId(), result.name(), result.email(),
                result.role(), result.organizationId());
    }

    static LoginResponse empty() {
        return new LoginResponse(null, null, null, null, null);
    }
}
