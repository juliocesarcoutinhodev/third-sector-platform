package br.com.toponesystem.thirdsector.auth.adapter.in.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

record LoginRequest(
        @Email @NotBlank String email,
        @NotBlank String password
) {}
