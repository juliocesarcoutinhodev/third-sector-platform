package br.com.toponesystem.thirdsector.organization.domain.exception;

import br.com.toponesystem.thirdsector.shared.domain.exception.ResourceNotFoundException;

public class OrganizationNotFoundException extends ResourceNotFoundException {

    public OrganizationNotFoundException(Long id) {
        super("Organization with id " + id + " not found");
    }
}
