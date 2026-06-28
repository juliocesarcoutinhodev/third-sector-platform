package br.com.toponesystem.thirdsector.auth.adapter.in.web;

import br.com.toponesystem.thirdsector.auth.application.usecase.SuperAdminLoginCommand;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
interface SuperAdminLoginRequestMapper {

    SuperAdminLoginCommand toCommand(SuperAdminLoginRequest request);
}
