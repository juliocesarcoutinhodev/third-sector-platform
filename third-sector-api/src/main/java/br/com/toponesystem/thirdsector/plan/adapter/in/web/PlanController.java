package br.com.toponesystem.thirdsector.plan.adapter.in.web;

import br.com.toponesystem.thirdsector.plan.application.dto.PlanView;
import br.com.toponesystem.thirdsector.plan.application.usecase.CreatePlanUseCase;
import br.com.toponesystem.thirdsector.plan.application.usecase.ListPlansUseCase;
import br.com.toponesystem.thirdsector.plan.application.usecase.UpdatePlanUseCase;
import br.com.toponesystem.thirdsector.shared.adapter.in.web.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
class PlanController {

    private final ListPlansUseCase listPlansUseCase;
    private final UpdatePlanUseCase updatePlanUseCase;
    private final CreatePlanUseCase createPlanUseCase;
    private final PlanRequestMapper mapper;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    ResponseEntity<ApiResponse<List<PlanView>>> list() {
        var views = listPlansUseCase.execute();
        return ResponseEntity.ok(ApiResponse.success("Planos listados com sucesso.", views));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    ResponseEntity<ApiResponse<PlanView>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePlanRequest request) {
        var command = mapper.toCommand(id, request);
        var view = updatePlanUseCase.execute(command);
        return ResponseEntity.ok(ApiResponse.success("Plano atualizado com sucesso.", view));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    ResponseEntity<ApiResponse<PlanView>> create(@Valid @RequestBody CreatePlanRequest request) {
        var command = mapper.toCommand(request);
        var view = createPlanUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Plano criado com sucesso.", view));
    }
}
