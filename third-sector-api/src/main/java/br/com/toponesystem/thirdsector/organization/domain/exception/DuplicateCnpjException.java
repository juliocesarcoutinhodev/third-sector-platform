package br.com.toponesystem.thirdsector.organization.domain.exception;

import br.com.toponesystem.thirdsector.shared.domain.exception.ConflictException;

public class DuplicateCnpjException extends ConflictException {

    public DuplicateCnpjException(String cnpj) {
        super("An organization with CNPJ '" + cnpj + "' already exists");
    }
}
