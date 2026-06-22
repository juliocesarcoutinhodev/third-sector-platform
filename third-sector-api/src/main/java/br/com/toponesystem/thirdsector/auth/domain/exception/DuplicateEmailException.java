package br.com.toponesystem.thirdsector.auth.domain.exception;

import br.com.toponesystem.thirdsector.shared.domain.exception.ConflictException;

public class DuplicateEmailException extends ConflictException {

    public DuplicateEmailException(String email) {
        super("Já existe um usuário com o e-mail '" + email + "'.");
    }
}
