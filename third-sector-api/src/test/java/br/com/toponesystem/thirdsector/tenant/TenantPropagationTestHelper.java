package br.com.toponesystem.thirdsector.tenant;

import br.com.toponesystem.thirdsector.tenant.domain.TenantContext;
import br.com.toponesystem.thirdsector.tenant.domain.exception.TenantContextNotSetException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Test-only helper for verifying tenant context propagation across async boundaries.
 *
 * <p>The {@code @EventListener @Async} listener exercises the same executor and
 * {@code TaskDecorator} path used by {@code @ApplicationModuleListener} handlers —
 * both dispatch via the {@code @Async} AOP proxy to the configured
 * {@link org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor}.
 */
@Component
@Profile("test")
class TenantPropagationTestHelper {

    private final ApplicationEventPublisher eventPublisher;
    private final BlockingQueue<String> listenerCaptures = new LinkedBlockingQueue<>();

    TenantPropagationTestHelper(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Async
    public CompletableFuture<String> captureCurrentTenantAsync() {
        return CompletableFuture.completedFuture(TenantContext.getCurrentTenant());
    }

    @Async
    public CompletableFuture<Boolean> hasTenantContextAsync() {
        try {
            TenantContext.getCurrentTenant();
            return CompletableFuture.completedFuture(true);
        } catch (TenantContextNotSetException e) {
            return CompletableFuture.completedFuture(false);
        }
    }

    public void publishTenantAwareEvent() {
        eventPublisher.publishEvent(new TenantAwareTestEvent());
    }

    @EventListener
    @Async
    public void onTenantAwareTestEvent(TenantAwareTestEvent event) {
        try {
            listenerCaptures.add(TenantContext.getCurrentTenant());
        } catch (TenantContextNotSetException e) {
            listenerCaptures.add("__NO_TENANT__");
        }
    }

    public String awaitListenerCapture(long timeout, TimeUnit unit) throws InterruptedException {
        return listenerCaptures.poll(timeout, unit);
    }
}
