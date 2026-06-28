package br.com.toponesystem.thirdsector.plan.adapter.in.web;

import br.com.toponesystem.thirdsector.plan.application.usecase.CreatePlanCommand;
import br.com.toponesystem.thirdsector.plan.application.usecase.UpdatePlanCommand;
import org.mapstruct.Mapper;

import java.util.UUID;

@Mapper(componentModel = "spring")
interface PlanRequestMapper {

    CreatePlanCommand toCommand(CreatePlanRequest request);

    default UpdatePlanCommand toCommand(UUID id, UpdatePlanRequest request) {
        return new UpdatePlanCommand(id, request.maxOrganizations());
    }
}
