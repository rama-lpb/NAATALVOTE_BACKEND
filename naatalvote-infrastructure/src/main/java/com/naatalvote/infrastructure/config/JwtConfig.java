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
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JwtConfig.class);

  /**
   * Clé de signature des jetons. Si NAATALVOTE_JWT_PRIVATE_KEY et NAATALVOTE_JWT_PUBLIC_KEY sont fournies
   * (PEM PKCS#8 / X.509), elles sont utilisées : les jetons restent valides après un redéploiement.
   * Sans ces clés, seul le mode démo/test explicitement configuré autorise une paire éphémère.
   */
  @Bean
  public KeyPair rsaKeyPair(
      @org.springframework.beans.factory.annotation.Value("${naatalvote.jwt.private-key:}") String privatePem,
      @org.springframework.beans.factory.annotation.Value("${naatalvote.jwt.public-key:}") String publicPem,
      @org.springframework.beans.factory.annotation.Value("${naatalvote.jwt.allow-ephemeral:false}") boolean allowEphemeral) {
    try {
      if (!privatePem.isBlank() && !publicPem.isBlank()) {
        java.security.KeyFactory kf = java.security.KeyFactory.getInstance("RSA");
        java.security.PrivateKey priv = kf.generatePrivate(new java.security.spec.PKCS8EncodedKeySpec(pemBody(privatePem)));
        java.security.PublicKey pub = kf.generatePublic(new java.security.spec.X509EncodedKeySpec(pemBody(publicPem)));
        log.info("JWT : clé RSA chargée depuis la configuration");
        return new KeyPair(pub, priv);
      }
      if (!allowEphemeral) throw new IllegalStateException("Clés JWT persistantes requises hors démonstration et tests");
      log.warn("JWT : démonstration/test avec paire RSA éphémère (jetons invalidés au redémarrage)");
      KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
      gen.initialize(2048);
      return gen.generateKeyPair();
    } catch (Exception e) {
      throw new IllegalStateException("Impossible de préparer la clé RSA de signature des jetons", e);
    }
  }

  private static byte[] pemBody(String pem) {
    String b64 = pem.replaceAll("-----[A-Z ]+-----", "").replaceAll("\\s", "");
    return java.util.Base64.getDecoder().decode(b64);
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
  public JwtDecoder jwtDecoder(KeyPair rsaKeyPair,
      com.naatalvote.application.fraud.ports.SuspensionRepositoryPort suspensions) {
    NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey((RSAPublicKey) rsaKeyPair.getPublic()).build();
    org.springframework.security.oauth2.core.OAuth2TokenValidator<org.springframework.security.oauth2.jwt.Jwt> activeUser = jwt -> {
      try {
        if (!suspensions.isApprovedForCitizen(UUID.fromString(jwt.getSubject()))) {
          return org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.success();
        }
      } catch (IllegalArgumentException ignored) {
        // A malformed subject is never an authenticated citizen.
      }
      return org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.failure(
          new org.springframework.security.oauth2.core.OAuth2Error("invalid_token", "Compte suspendu ou identité invalide", null));
    };
    decoder.setJwtValidator(new org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator<>(
        org.springframework.security.oauth2.jwt.JwtValidators.createDefault(), activeUser));
    return decoder;
  }

  @Bean
  public TokenIssuerPort tokenIssuerPort(JwtEncoder encoder) {
    return new JwtTokenIssuer(encoder);
  }
}

