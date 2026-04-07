package com.naatalvote.infrastructure.config;

import com.naatalvote.application.audit.ports.ActionLogRepositoryPort;
import com.naatalvote.application.auth.ports.OtpSenderPort;
import com.naatalvote.application.auth.ports.OtpStorePort;
import com.naatalvote.application.auth.ports.TokenIssuerPort;
import com.naatalvote.application.auth.ports.UserRepositoryPort;
import com.naatalvote.application.common.SystemTimeProvider;
import com.naatalvote.application.common.TimeProvider;
import com.naatalvote.application.election.ports.CandidateRepositoryPort;
import com.naatalvote.application.election.ports.ElectionRepositoryPort;
import com.naatalvote.application.fraud.ports.FraudAlertRepositoryPort;
import com.naatalvote.application.fraud.ports.SuspensionRepositoryPort;
import com.naatalvote.application.vote.ports.TraceVoteRepositoryPort;
import com.naatalvote.application.vote.ports.VoteRepositoryPort;
import com.naatalvote.infrastructure.persistence.jpa.adapter.ActionLogJpaAdapter;
import com.naatalvote.infrastructure.persistence.jpa.adapter.CandidateJpaAdapter;
import com.naatalvote.infrastructure.persistence.jpa.adapter.ElectionJpaAdapter;
import com.naatalvote.infrastructure.persistence.jpa.adapter.FraudAlertJpaAdapter;
import com.naatalvote.infrastructure.persistence.jpa.adapter.SuspensionJpaAdapter;
import com.naatalvote.infrastructure.persistence.jpa.adapter.TraceVoteJpaAdapter;
import com.naatalvote.infrastructure.persistence.jpa.adapter.UserJpaAdapter;
import com.naatalvote.infrastructure.persistence.jpa.adapter.VoteJpaAdapter;
import com.naatalvote.infrastructure.persistence.jpa.repository.ActionLogJpaRepository;
import com.naatalvote.infrastructure.persistence.jpa.repository.CandidateJpaRepository;
import com.naatalvote.infrastructure.persistence.jpa.repository.ElectionJpaRepository;
import com.naatalvote.infrastructure.persistence.jpa.repository.FraudAlertJpaRepository;
import com.naatalvote.infrastructure.persistence.jpa.repository.SuspensionJpaRepository;
import com.naatalvote.infrastructure.persistence.jpa.repository.TraceVoteJpaRepository;
import com.naatalvote.infrastructure.persistence.jpa.repository.UserJpaRepository;
import com.naatalvote.infrastructure.persistence.jpa.repository.VoteJpaRepository;
import com.naatalvote.infrastructure.security.ConsoleOtpSender;
import com.naatalvote.infrastructure.security.RedisOtpStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@Profile("postgres")
public class PostgresAdaptersConfig {
  @Bean
  public TimeProvider timeProvider() {
    return new SystemTimeProvider();
  }

  @Bean
  public ElectionRepositoryPort electionRepositoryPort(ElectionJpaRepository repo) {
    return new ElectionJpaAdapter(repo);
  }

  @Bean
  public CandidateRepositoryPort candidateRepositoryPort(CandidateJpaRepository repo) {
    return new CandidateJpaAdapter(repo);
  }

  @Bean
  public VoteRepositoryPort voteRepositoryPort(VoteJpaRepository repo) {
    return new VoteJpaAdapter(repo);
  }

  @Bean
  public TraceVoteRepositoryPort traceVoteRepositoryPort(TraceVoteJpaRepository repo) {
    return new TraceVoteJpaAdapter(repo);
  }

  @Bean
  public UserRepositoryPort userRepositoryPort(UserJpaRepository repo) {
    return new UserJpaAdapter(repo);
  }

  @Bean
  public FraudAlertRepositoryPort fraudAlertRepositoryPort(FraudAlertJpaRepository repo) {
    return new FraudAlertJpaAdapter(repo);
  }

  @Bean
  public SuspensionRepositoryPort suspensionRepositoryPort(SuspensionJpaRepository repo) {
    return new SuspensionJpaAdapter(repo);
  }

  @Bean
  public ActionLogRepositoryPort actionLogRepositoryPort(ActionLogJpaRepository repo) {
    return new ActionLogJpaAdapter(repo);
  }

  @Bean
  public OtpStorePort otpStorePort(StringRedisTemplate redis) {
    return new RedisOtpStore(redis);
  }

  @Bean
  public OtpSenderPort otpSenderPort() {
    return new ConsoleOtpSender();
  }

  // TokenIssuerPort fourni par JwtConfig (même profile)
}
