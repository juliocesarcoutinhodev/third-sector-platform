package br.com.toponesystem.thirdsector.municipality.adapter.in.web;

import br.com.toponesystem.thirdsector.municipality.application.dto.MunicipalityView;
import br.com.toponesystem.thirdsector.municipality.application.usecase.FindMunicipalityByIdUseCase;
import br.com.toponesystem.thirdsector.municipality.application.usecase.FindMunicipalityBySubdomainUseCase;
import br.com.toponesystem.thirdsector.municipality.application.usecase.ListActiveMunicipalitiesUseCase;
import br.com.toponesystem.thirdsector.municipality.application.usecase.RegisterMunicipalityUseCase;
import br.com.toponesystem.thirdsector.shared.adapter.in.web.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/municipalities")
@RequiredArgsConstructor
class MunicipalityController {

    private final RegisterMunicipalityUseCase registerUseCase;
    private final FindMunicipalityBySubdomainUseCase findBySubdomainUseCase;
    private final FindMunicipalityByIdUseCase findByIdUseCase;
    private final ListActiveMunicipalitiesUseCase listActiveUseCase;
    private final RegisterMunicipalityRequestMapper requestMapper;

    @PostMapping
    ResponseEntity<ApiResponse<MunicipalityView>> register(@Valid @RequestBody RegisterMunicipalityRequest request) {
        var view = registerUseCase.execute(requestMapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Município cadastrado com sucesso.", view));
    }

    @GetMapping("/by-subdomain/{subdomain}")
    ResponseEntity<ApiResponse<MunicipalityView>> findBySubdomain(@PathVariable String subdomain) {
        var view = findBySubdomainUseCase.execute(subdomain);
        return ResponseEntity.ok(ApiResponse.success("Município encontrado.", view));
    }

    @GetMapping("/{id}")
    ResponseEntity<ApiResponse<MunicipalityView>> findById(@PathVariable Long id) {
        var view = findByIdUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.success("Município encontrado.", view));
    }

    @GetMapping
    ResponseEntity<ApiResponse<List<MunicipalityView>>> listActive() {
        var views = listActiveUseCase.execute();
        return ResponseEntity.ok(ApiResponse.success("Municípios ativos listados com sucesso.", views));
    }
}
