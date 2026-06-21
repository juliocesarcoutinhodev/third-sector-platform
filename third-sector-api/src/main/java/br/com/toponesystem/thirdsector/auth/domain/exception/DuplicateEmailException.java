package br.com.toponesystem.thirdsector.auth.domain.exception;

import br.com.toponesystem.thirdsector.shared.domain.exception.ConflictException;

public class DuplicateEmailException extends ConflictException {

    public DuplicateEmailException(String email) {
        super("A user with email '" + email + "' already exists");
    }
}
