package br.com.toponesystem.thirdsector.tenant.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.hibernate.cfg.MultiTenancySettings;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
class TenantJpaConfiguration implements HibernatePropertiesCustomizer {

    private final TenantConnectionProvider connectionProvider;
    private final TenantIdentifierResolver identifierResolver;

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(MultiTenancySettings.MULTI_TENANT_CONNECTION_PROVIDER, connectionProvider);
        hibernateProperties.put(MultiTenancySettings.MULTI_TENANT_IDENTIFIER_RESOLVER, identifierResolver);
    }
}
