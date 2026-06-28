package br.com.toponesystem.thirdsector.auth.adapter.out.persistence;

import br.com.toponesystem.thirdsector.auth.domain.model.SuperAdmin;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
interface SuperAdminMapper {

    SuperAdminEntity toEntity(SuperAdmin domain);

    SuperAdmin toDomain(SuperAdminEntity entity);
}
