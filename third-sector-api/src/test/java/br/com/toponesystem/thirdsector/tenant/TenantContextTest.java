package br.com.toponesystem.thirdsector.tenant;

import br.com.toponesystem.thirdsector.tenant.domain.TenantContext;
import br.com.toponesystem.thirdsector.tenant.domain.exception.TenantContextNotSetException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TenantContextTest {

    @AfterEach
    void cleanup() {
        TenantContext.clear();
    }

    @Test
    void doesNotLeakTenantBetweenSequentialCallsOnSameThread() {
        TenantContext.setCurrentTenant("tenant-a");
        assertThat(TenantContext.getCurrentTenant()).isEqualTo("tenant-a");

        TenantContext.clear();

        TenantContext.setCurrentTenant("tenant-b");
        assertThat(TenantContext.getCurrentTenant()).isEqualTo("tenant-b");

        TenantContext.clear();

        assertThatThrownBy(TenantContext::getCurrentTenant)
                .isInstanceOf(TenantContextNotSetException.class);
    }

    @Test
    void throwsWhenCalledWithoutPriorSet() {
        assertThatThrownBy(TenantContext::getCurrentTenant)
                .isInstanceOf(TenantContextNotSetException.class)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tenant");
    }
}
