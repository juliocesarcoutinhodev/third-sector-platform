package br.com.toponesystem.thirdsector.municipality.domain.exception;

import br.com.toponesystem.thirdsector.shared.domain.exception.ConflictException;

public class DuplicateSubdomainException extends ConflictException {

    public DuplicateSubdomainException(String subdomain) {
        super("Já existe um município com o subdomínio '" + subdomain + "'.");
    }
}
