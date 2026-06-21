package br.com.toponesystem.thirdsector.auth.domain.exception;

import br.com.toponesystem.thirdsector.shared.domain.exception.BusinessException;

public class InvalidUserRoleAssignmentException extends BusinessException {

    public InvalidUserRoleAssignmentException(String message) {
        super(message);
    }
}
