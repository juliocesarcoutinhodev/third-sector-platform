package br.com.toponesystem.thirdsector.municipality.domain;

import br.com.toponesystem.thirdsector.shared.domain.exception.ResourceNotFoundException;

public class MunicipalityNotFoundException extends ResourceNotFoundException {

    public MunicipalityNotFoundException(String subdomain) {
        super("Municipality with subdomain '" + subdomain + "' not found");
    }

    public MunicipalityNotFoundException(Long id) {
        super("Municipality with id " + id + " not found");
    }
}
