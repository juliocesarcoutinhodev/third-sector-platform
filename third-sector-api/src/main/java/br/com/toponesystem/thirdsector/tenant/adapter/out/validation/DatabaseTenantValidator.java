package br.com.toponesystem.thirdsector.tenant.adapter.out.validation;

import br.com.toponesystem.thirdsector.tenant.domain.TenantValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class DatabaseTenantValidator implements TenantValidator {

    private static final String SQL =
            "SELECT EXISTS(SELECT 1 FROM master.municipality WHERE subdomain = ? AND active = true)";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public boolean isActive(String tenantId) {
        var result = jdbcTemplate.queryForObject(SQL, Boolean.class, tenantId);
        return Boolean.TRUE.equals(result);
    }
}
