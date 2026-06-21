package br.com.toponesystem.thirdsector.auth.adapter.in.web;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
record LoginResponse(
        Long userId,
        String name,
        String email,
        String role,
        Long organizationId
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
