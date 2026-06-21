package br.com.toponesystem.thirdsector.auth.adapter.in.web;

import br.com.toponesystem.thirdsector.auth.application.usecase.LoginCommand;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
interface LoginRequestMapper {

    LoginCommand toCommand(LoginRequest request);
}
