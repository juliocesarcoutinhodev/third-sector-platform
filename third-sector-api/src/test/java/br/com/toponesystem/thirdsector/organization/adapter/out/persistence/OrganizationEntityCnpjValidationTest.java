package br.com.toponesystem.thirdsector.organization.adapter.out.persistence;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class OrganizationEntityCnpjValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void validCnpjPassesValidation() {
        var entity = new OrganizationEntity(null, "ACME ONG", "12345678000195",
                br.com.toponesystem.thirdsector.organization.domain.model.OrganizationStatus.PENDING,
                Instant.now(), Instant.now());

        var violations = validator.validate(entity);
        assertThat(violations).isEmpty();
    }

    @Test
    void invalidCnpjFailsValidation() {
        var entity = new OrganizationEntity(null, "ACME ONG", "00000000000000",
                br.com.toponesystem.thirdsector.organization.domain.model.OrganizationStatus.PENDING,
                Instant.now(), Instant.now());

        var violations = validator.validate(entity);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("cnpj"));
    }

    @Test
    void cnpjWithWrongCheckDigitsFailsValidation() {
        var entity = new OrganizationEntity(null, "ACME ONG", "12345678000100",
                br.com.toponesystem.thirdsector.organization.domain.model.OrganizationStatus.PENDING,
                Instant.now(), Instant.now());

        var violations = validator.validate(entity);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("cnpj"));
    }

    @Test
    void maskedCnpjPassesValidation() {
        var entity = new OrganizationEntity(null, "ACME ONG", "12.345.678/0001-95",
                br.com.toponesystem.thirdsector.organization.domain.model.OrganizationStatus.PENDING,
                Instant.now(), Instant.now());

        var violations = validator.validate(entity);
        assertThat(violations).isEmpty();
    }

    @Test
    void blankCnpjFailsValidation() {
        var entity = new OrganizationEntity(null, "ACME ONG", "",
                br.com.toponesystem.thirdsector.organization.domain.model.OrganizationStatus.PENDING,
                Instant.now(), Instant.now());

        var violations = validator.validate(entity);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("cnpj"));
    }
}
