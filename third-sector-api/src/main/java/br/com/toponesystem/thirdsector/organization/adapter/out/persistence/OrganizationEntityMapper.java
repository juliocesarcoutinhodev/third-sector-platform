package br.com.toponesystem.thirdsector.organization.adapter.out.persistence;

import br.com.toponesystem.thirdsector.organization.domain.model.Organization;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
interface OrganizationEntityMapper {

    OrganizationEntity toEntity(Organization domain);

    Organization toDomain(OrganizationEntity entity);
}
