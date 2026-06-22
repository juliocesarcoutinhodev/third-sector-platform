package br.com.toponesystem.thirdsector.auth.adapter.in.web;

import br.com.toponesystem.thirdsector.auth.application.usecase.ConfirmPasswordResetCommand;
import br.com.toponesystem.thirdsector.auth.application.usecase.RequestPasswordResetCommand;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
interface PasswordResetMapper {

    RequestPasswordResetCommand toCommand(PasswordResetRequest request);

    ConfirmPasswordResetCommand toCommand(PasswordResetConfirm request);
}
