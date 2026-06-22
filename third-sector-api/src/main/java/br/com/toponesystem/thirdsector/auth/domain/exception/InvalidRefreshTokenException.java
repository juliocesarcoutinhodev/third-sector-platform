package br.com.toponesystem.thirdsector.auth.domain.exception;

import br.com.toponesystem.thirdsector.shared.domain.exception.BusinessException;

public class InvalidRefreshTokenException extends BusinessException {

    public InvalidRefreshTokenException() {
        super("Token de acesso inválido ou expirado.");
    }
}
