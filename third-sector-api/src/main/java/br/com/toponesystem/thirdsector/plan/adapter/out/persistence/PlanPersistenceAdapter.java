package br.com.toponesystem.thirdsector.plan.adapter.out.persistence;

import br.com.toponesystem.thirdsector.plan.domain.model.Plan;
import br.com.toponesystem.thirdsector.plan.domain.port.out.PlanRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class PlanPersistenceAdapter implements PlanRepository {

    private final SpringDataPlanRepository jpaRepo;
    private final EntityManager entityManager;

    @Override
    public Plan save(Plan domain) {
        var entity = toEntity(domain);
        var saved = jpaRepo.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Plan> findById(UUID id) {
        return jpaRepo.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Plan> findByName(String name) {
        return jpaRepo.findByName(name).map(this::toDomain);
    }

    @Override
    public List<Plan> findAll() {
        return jpaRepo.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Plan> findByTenantId(String tenantId) {
        try {
            var planId = (UUID) entityManager
                    .createNativeQuery(
                            "SELECT plan_id FROM master.municipality WHERE subdomain = :subdomain")
                    .setParameter("subdomain", tenantId)
                    .getSingleResult();
            return jpaRepo.findById(planId).map(this::toDomain);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    private PlanEntity toEntity(Plan domain) {
        return PlanEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .maxOrganizations(domain.getMaxOrganizations())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    private Plan toDomain(PlanEntity entity) {
        return new Plan(
                entity.getId(), entity.getName(), entity.getMaxOrganizations(),
                entity.getCreatedAt(), entity.getUpdatedAt()
        );
    }
}
