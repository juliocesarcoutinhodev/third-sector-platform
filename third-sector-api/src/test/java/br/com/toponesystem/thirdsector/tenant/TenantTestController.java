package br.com.toponesystem.thirdsector.tenant;

import br.com.toponesystem.thirdsector.tenant.domain.TenantContext;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant-test")
@Profile("test")
class TenantTestController {

    @GetMapping
    ResponseEntity<String> current() {
        return ResponseEntity.ok(TenantContext.getCurrentTenant());
    }
}
