package br.com.toponesystem.thirdsector.tenant;

import br.com.toponesystem.thirdsector.AbstractIntegrationTest;
import br.com.toponesystem.thirdsector.shared.domain.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class TenantContextPropagationTest extends AbstractIntegrationTest {

    @Autowired
    private TenantPropagationTestHelper helper;

    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }

    @Test
    void asyncMethodPropagatesTenantContextToWorkerThread() throws Exception {
        TenantContext.setCurrentTenant("maringa");

        String captured = helper.captureCurrentTenantAsync().get(5, TimeUnit.SECONDS);

        assertThat(captured).isEqualTo("maringa");
    }

    @Test
    void asyncEventListenerPropagatesTenantContextFromPublishingThread() throws Exception {
        TenantContext.setCurrentTenant("londrina");

        helper.publishTenantAwareEvent();

        String captured = helper.awaitListenerCapture(5, TimeUnit.SECONDS);
        assertThat(captured).isEqualTo("londrina");
    }

    @Test
    void workerThreadDoesNotLeakTenantContextAfterTaskCompletion() throws Exception {
        TenantContext.setCurrentTenant("maringa");
        helper.captureCurrentTenantAsync().get(5, TimeUnit.SECONDS);
        TenantContext.clear();

        boolean hasContext = helper.hasTenantContextAsync().get(5, TimeUnit.SECONDS);

        assertThat(hasContext).isFalse();
    }
}
