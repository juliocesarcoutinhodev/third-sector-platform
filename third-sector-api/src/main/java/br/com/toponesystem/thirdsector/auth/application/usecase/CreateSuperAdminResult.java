package br.com.toponesystem.thirdsector.auth.application.usecase;

import br.com.toponesystem.thirdsector.auth.application.dto.SuperAdminView;

public record CreateSuperAdminResult(SuperAdminView superAdmin, String temporaryPassword) {}
