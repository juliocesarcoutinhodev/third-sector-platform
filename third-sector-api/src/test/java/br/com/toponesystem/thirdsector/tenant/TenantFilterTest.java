package br.com.toponesystem.thirdsector.tenant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import br.com.toponesystem.thirdsector.AbstractIntegrationTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class TenantFilterTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void resolvesValidSubdomainAndPopulatesTenantContext() throws Exception {
        mockMvc.perform(get("/api/tenant-test")
                        .header("Host", "maringa.thirdsector.com.br"))
                .andExpect(status().isOk())
                .andExpect(content().string("maringa"));
    }

    @Test
    void rejectsRequestWithUnrecognizedSubdomain() throws Exception {
        mockMvc.perform(get("/api/tenant-test")
                        .header("Host", "unknown.thirdsector.com.br"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsRequestWithMissingTenant() throws Exception {
        mockMvc.perform(get("/api/tenant-test")
                        .header("Host", "localhost:8080"))
                .andExpect(status().isNotFound());
    }

    @Test
    void resolvesValidTenantViaHeaderFallback() throws Exception {
        mockMvc.perform(get("/api/tenant-test")
                        .header("X-Tenant-ID", "londrina"))
                .andExpect(status().isOk())
                .andExpect(content().string("londrina"));
    }
}
