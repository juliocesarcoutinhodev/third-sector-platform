package br.com.toponesystem.thirdsector.auth.domain.exception;

import br.com.toponesystem.thirdsector.shared.domain.exception.BusinessException;

public class AuthenticationFailedException extends BusinessException {

    public AuthenticationFailedException() {
        super("Invalid email or password");
    }
}
