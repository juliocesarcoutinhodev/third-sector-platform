package br.com.toponesystem.thirdsector.auth.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

record ForcePasswordChangeRequest(
        @NotBlank
        @Size(min = 8)
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).+$",
                message = "A senha deve conter letras e números")
        String newPassword
) {}
