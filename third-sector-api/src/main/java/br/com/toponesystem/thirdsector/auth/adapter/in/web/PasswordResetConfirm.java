package br.com.toponesystem.thirdsector.auth.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

record PasswordResetConfirm(
        @NotBlank String token,
        @NotBlank @Size(min = 8)
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).+$",
                message = "must contain at least one letter and one number")
        String newPassword
) {}
