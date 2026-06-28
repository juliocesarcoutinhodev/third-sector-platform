package br.com.toponesystem.thirdsector.plan.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface SpringDataPlanRepository extends JpaRepository<PlanEntity, UUID> {

    Optional<PlanEntity> findByName(String name);
}
