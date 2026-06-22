package br.com.toponesystem.thirdsector.auth.domain.exception;

import br.com.toponesystem.thirdsector.shared.domain.exception.BusinessException;

public class InvalidPasswordResetTokenException extends BusinessException {

    public InvalidPasswordResetTokenException() {
        super("Token de redefinição inválido ou expirado.");
    }
}
