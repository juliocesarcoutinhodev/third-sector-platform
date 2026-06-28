package br.com.toponesystem.thirdsector.auth.domain.exception;

import br.com.toponesystem.thirdsector.shared.domain.exception.ConflictException;

public class DuplicateSuperAdminEmailException extends ConflictException {

    public DuplicateSuperAdminEmailException(String email) {
        super("Já existe um Super Admin com o e-mail '" + email + "'.");
    }
}
