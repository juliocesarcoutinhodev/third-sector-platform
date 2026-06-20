package br.com.toponesystem.thirdsector.tenant.domain;

public class TenantContextNotSetException extends IllegalStateException {

    public TenantContextNotSetException() {
        super("No tenant set for the current thread. Call TenantContext.setCurrentTenant() before entering tenant-aware code.");
    }
}
