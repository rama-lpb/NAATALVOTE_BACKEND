package com.naatalvote.infrastructure.config;

import com.naatalvote.application.auth.ports.TokenIssuerPort;
import com.naatalvote.domain.auth.UserRole;
import com.naatalvote.infrastructure.security.JwtTokenIssuer;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;

@Configuration
@Profile("postgres")
public class JwtConfig {
  @Bean
  public KeyPair rsaKeyPair() {
    try {
      KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
      gen.initialize(2048);
      return gen.generateKeyPair();
    } catch (Exception e) {
      throw new IllegalStateException("Impossible de générer une keypair RSA", e);
    }
  }

  @Bean
  public JwtEncoder jwtEncoder(KeyPair rsaKeyPair) {
    RSAKey rsaKey = new RSAKey.Builder((RSAPublicKey) rsaKeyPair.getPublic())
        .privateKey((RSAPrivateKey) rsaKeyPair.getPrivate())
        .keyID("naatalvote-dev")
        .build();
    return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(rsaKey)));
  }

  @Bean
  public JwtDecoder jwtDecoder(KeyPair rsaKeyPair) {
    return NimbusJwtDecoder.withPublicKey((RSAPublicKey) rsaKeyPair.getPublic()).build();
  }

  @Bean
  public TokenIssuerPort tokenIssuerPort(JwtEncoder encoder) {
    return new JwtTokenIssuer(encoder);
  }
}

