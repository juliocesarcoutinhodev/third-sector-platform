package br.com.toponesystem.thirdsector.tenant.domain.port.out;

/**
 * Port for validating whether a tenant identifier corresponds to an active municipality.
 * The production implementation (Story 1.9) queries the master schema.
 * A configurable in-memory stub is used in dev and test profiles until then.
 */
public interface TenantValidator {

    boolean isActive(String tenantId);
}
