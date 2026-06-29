package br.com.toponesystem.thirdsector.tenant.adapter.in.async;

import br.com.toponesystem.thirdsector.shared.domain.TenantContext;
import br.com.toponesystem.thirdsector.shared.domain.exception.TenantContextNotSetException;
import org.springframework.core.task.TaskDecorator;
import org.springframework.stereotype.Component;

@Component
class TenantContextPropagationTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        var tenantId = currentTenantOrNull();
        return () -> {
            if (tenantId != null) {
                TenantContext.setCurrentTenant(tenantId);
            }
            try {
                runnable.run();
            } finally {
                TenantContext.clear();
            }
        };
    }

    private static String currentTenantOrNull() {
        try {
            return TenantContext.getCurrentTenant();
        } catch (TenantContextNotSetException e) {
            return null;
        }
    }
}
