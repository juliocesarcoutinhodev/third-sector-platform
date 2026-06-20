package br.com.toponesystem.thirdsector.tenant.domain;

import br.com.toponesystem.thirdsector.tenant.domain.exception.TenantContextNotSetException;

/**
 * Thread-safe holder for the current tenant identifier during request processing.
 *
 * <p>Usage contract:
 * <ul>
 *   <li>{@link #setCurrentTenant(String)} must be called once at the start of each request,
 *       before any tenant-aware code executes.
 *   <li>{@link #getCurrentTenant()} may be called from any code on the same thread
 *       after the tenant has been set.
 *   <li>{@link #clear()} must be called in a {@code finally} block at the end of the request
 *       to prevent context leaking to the next request on the same pooled thread.
 * </ul>
 */
public final class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setCurrentTenant(String tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static String getCurrentTenant() {
        String tenantId = CURRENT_TENANT.get();
        if (tenantId == null) {
            throw new TenantContextNotSetException();
        }
        return tenantId;
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
