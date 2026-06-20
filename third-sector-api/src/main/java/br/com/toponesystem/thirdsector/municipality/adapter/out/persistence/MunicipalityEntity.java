package br.com.toponesystem.thirdsector.municipality.adapter.out.persistence;

import br.com.toponesystem.thirdsector.municipality.domain.Plan;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.br.CNPJ;

import java.time.Instant;

@Entity
@Table(name = "municipality", schema = "master")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
class MunicipalityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String name;

    @CNPJ
    @NotBlank
    @Column(length = 14, nullable = false, unique = true)
    private String cnpj;

    @NotBlank
    @Size(max = 63)
    @Pattern(
            regexp = "^[a-z0-9]([a-z0-9-]*[a-z0-9])?$",
            message = "Subdomain must contain only lowercase letters, numbers, and hyphens"
    )
    @Column(length = 63, nullable = false, unique = true)
    private String subdomain;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private Plan plan;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
