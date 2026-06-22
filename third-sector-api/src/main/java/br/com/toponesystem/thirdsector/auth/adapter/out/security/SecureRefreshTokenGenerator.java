package br.com.toponesystem.thirdsector.auth.adapter.out.security;

import br.com.toponesystem.thirdsector.auth.domain.port.out.RefreshTokenGenerator;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.HexFormat;

@Component
class SecureRefreshTokenGenerator implements RefreshTokenGenerator {

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generate() {
        var bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
