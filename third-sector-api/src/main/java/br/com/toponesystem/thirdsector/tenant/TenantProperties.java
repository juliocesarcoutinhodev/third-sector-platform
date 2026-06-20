package br.com.toponesystem.thirdsector.tenant;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "tenant")
public class TenantProperties {

    private String baseDomain;
    private boolean headerFallbackEnabled;
    private String fallbackHeader = "X-Tenant-ID";
    private List<String> knownTenants = new ArrayList<>();
}
