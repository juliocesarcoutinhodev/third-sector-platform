package br.com.toponesystem.thirdsector.plan.domain.exception;

import br.com.toponesystem.thirdsector.shared.domain.exception.ResourceNotFoundException;

import java.util.UUID;

public class PlanNotFoundException extends ResourceNotFoundException {

    public PlanNotFoundException(UUID id) {
        super("Plano com id '" + id + "' nao encontrado.");
    }
}
