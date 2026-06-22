package br.com.toponesystem.thirdsector.municipality.domain.exception;

import br.com.toponesystem.thirdsector.shared.domain.exception.ResourceNotFoundException;

import java.util.UUID;

public class MunicipalityNotFoundException extends ResourceNotFoundException {

    public MunicipalityNotFoundException(String subdomain) {
        super("Município com subdomínio '" + subdomain + "' não encontrado.");
    }

    public MunicipalityNotFoundException(UUID id) {
        super("Município com id " + id + " não encontrado.");
    }
}
