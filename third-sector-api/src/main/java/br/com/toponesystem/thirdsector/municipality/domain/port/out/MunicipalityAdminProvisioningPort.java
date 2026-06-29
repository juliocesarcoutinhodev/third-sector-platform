package br.com.toponesystem.thirdsector.municipality.domain.port.out;

public interface MunicipalityAdminProvisioningPort {

    void provision(String adminName, String adminEmail, String subdomain, String municipalityName);
}
