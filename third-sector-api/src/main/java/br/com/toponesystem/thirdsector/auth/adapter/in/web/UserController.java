package br.com.toponesystem.thirdsector.auth.adapter.in.web;

import br.com.toponesystem.thirdsector.auth.application.dto.UserView;
import br.com.toponesystem.thirdsector.auth.application.usecase.CreateUserUseCase;
import br.com.toponesystem.thirdsector.shared.adapter.in.web.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final CreateUserRequestMapper requestMapper;

    @PostMapping
    ResponseEntity<ApiResponse<UserView>> create(@Valid @RequestBody CreateUserRequest request) {
        var view = createUserUseCase.execute(requestMapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Usuário cadastrado com sucesso.", view));
    }
}
