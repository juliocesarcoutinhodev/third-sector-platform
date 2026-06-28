package br.com.toponesystem.thirdsector.plan.domain.port.out;

import br.com.toponesystem.thirdsector.plan.domain.model.Plan;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlanRepository {

    Plan save(Plan plan);

    Optional<Plan> findById(UUID id);

    Optional<Plan> findByName(String name);

    List<Plan> findAll();

    Optional<Plan> findByTenantId(String tenantId);
}
