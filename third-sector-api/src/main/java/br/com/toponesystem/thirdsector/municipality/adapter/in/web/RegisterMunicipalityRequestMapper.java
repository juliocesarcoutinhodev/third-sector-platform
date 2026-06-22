package br.com.toponesystem.thirdsector.municipality.adapter.in.web;

import br.com.toponesystem.thirdsector.municipality.application.usecase.RegisterMunicipalityCommand;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
interface RegisterMunicipalityRequestMapper {

    RegisterMunicipalityCommand toCommand(RegisterMunicipalityRequest request);
}
