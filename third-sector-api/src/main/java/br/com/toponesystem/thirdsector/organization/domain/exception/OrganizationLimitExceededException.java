package br.com.toponesystem.thirdsector.organization.domain.exception;

import br.com.toponesystem.thirdsector.shared.domain.exception.BusinessException;

public class OrganizationLimitExceededException extends BusinessException {

    public OrganizationLimitExceededException(int currentCount, int maxAllowed) {
        super("Limite de organizacoes atingido: " + currentCount + " de " + maxAllowed + " cadastradas. "
              + "Fac,a o upgrade do seu plano para cadastrar mais organizac,oes.");
    }
}
