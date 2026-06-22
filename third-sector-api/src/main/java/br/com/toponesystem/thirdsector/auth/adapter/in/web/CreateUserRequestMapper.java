package br.com.toponesystem.thirdsector.auth.adapter.in.web;

import br.com.toponesystem.thirdsector.auth.application.usecase.CreateUserCommand;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
interface CreateUserRequestMapper {

    CreateUserCommand toCommand(CreateUserRequest request);
}
