package br.com.toponesystem.thirdsector.auth.adapter.in.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

record PasswordResetRequest(
        @Email @NotBlank String email
) {}
