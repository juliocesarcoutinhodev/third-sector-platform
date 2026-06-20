package br.com.toponesystem.thirdsector.municipality.domain;

import br.com.toponesystem.thirdsector.shared.domain.exception.ConflictException;

public class DuplicateSubdomainException extends ConflictException {

    public DuplicateSubdomainException(String subdomain) {
        super("A municipality with subdomain '" + subdomain + "' already exists");
    }
}
