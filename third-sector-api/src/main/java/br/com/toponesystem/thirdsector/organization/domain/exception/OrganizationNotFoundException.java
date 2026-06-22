package br.com.toponesystem.thirdsector.organization.domain.exception;

import br.com.toponesystem.thirdsector.shared.domain.exception.ResourceNotFoundException;

import java.util.UUID;

public class OrganizationNotFoundException extends ResourceNotFoundException {

    public OrganizationNotFoundException(UUID id) {
        super("Organização com id " + id + " não encontrada.");
    }
}
