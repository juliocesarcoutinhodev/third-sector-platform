package br.com.toponesystem.thirdsector.municipality.domain.port.out;

public interface TenantProvisioningPort {

    void provision(String subdomain);
}
