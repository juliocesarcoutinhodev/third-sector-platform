package br.com.toponesystem.thirdsector.auth.domain.port.out;

public interface TokenHasher {

    String hash(String token);
}
