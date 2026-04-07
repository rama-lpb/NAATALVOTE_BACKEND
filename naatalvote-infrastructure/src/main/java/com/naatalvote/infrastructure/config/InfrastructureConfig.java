package com.naatalvote.infrastructure.config;

import com.naatalvote.application.common.SystemTimeProvider;
import com.naatalvote.application.common.TimeProvider;
import com.naatalvote.application.audit.ports.ActionLogRepositoryPort;
import com.naatalvote.application.auth.ports.OtpSenderPort;
import com.naatalvote.application.auth.ports.OtpStorePort;
import com.naatalvote.application.auth.ports.TokenIssuerPort;
import com.naatalvote.application.auth.ports.UserRepositoryPort;
import com.naatalvote.application.election.ports.CandidateRepositoryPort;
import com.naatalvote.application.election.ports.ElectionRepositoryPort;
import com.naatalvote.application.fraud.ports.FraudAlertRepositoryPort;
import com.naatalvote.application.fraud.ports.SuspensionRepositoryPort;
import com.naatalvote.application.vote.ports.TraceVoteRepositoryPort;
import com.naatalvote.application.vote.ports.VoteRepositoryPort;
import com.naatalvote.infrastructure.security.ConsoleOtpSender;
import com.naatalvote.infrastructure.security.InMemoryOtpStore;
import com.naatalvote.infrastructure.security.MockTokenIssuer;
import com.naatalvote.infrastructure.persistence.memory.InMemoryActionLogRepository;
import com.naatalvote.infrastructure.persistence.memory.InMemoryCandidateRepository;
import com.naatalvote.infrastructure.persistence.memory.InMemoryElectionRepository;
import com.naatalvote.infrastructure.persistence.memory.InMemoryFraudAlertRepository;
import com.naatalvote.infrastructure.persistence.memory.InMemorySuspensionRepository;
import com.naatalvote.infrastructure.persistence.memory.InMemoryTraceVoteRepository;
import com.naatalvote.infrastructure.persistence.memory.InMemoryUserRepository;
import com.naatalvote.infrastructure.persistence.memory.InMemoryVoteRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("inmemory")
public class InfrastructureConfig {
  @Bean
  public TimeProvider timeProvider() {
    return new SystemTimeProvider();
  }

  @Bean
  public ElectionRepositoryPort electionRepositoryPort() {
    return new InMemoryElectionRepository();
  }

  @Bean
  public CandidateRepositoryPort candidateRepositoryPort() {
    return new InMemoryCandidateRepository();
  }

  @Bean
  public VoteRepositoryPort voteRepositoryPort() {
    return new InMemoryVoteRepository();
  }

  @Bean
  public TraceVoteRepositoryPort traceVoteRepositoryPort() {
    return new InMemoryTraceVoteRepository();
  }

  @Bean
  public UserRepositoryPort userRepositoryPort() {
    return new InMemoryUserRepository();
  }

  @Bean
  public FraudAlertRepositoryPort fraudAlertRepositoryPort() {
    return new InMemoryFraudAlertRepository();
  }

  @Bean
  public SuspensionRepositoryPort suspensionRepositoryPort() {
    return new InMemorySuspensionRepository();
  }

  @Bean
  public ActionLogRepositoryPort actionLogRepositoryPort() {
    return new InMemoryActionLogRepository();
  }

  @Bean
  public OtpStorePort otpStorePort() {
    return new InMemoryOtpStore();
  }

  @Bean
  public OtpSenderPort otpSenderPort() {
    return new ConsoleOtpSender();
  }

  @Bean
  public TokenIssuerPort tokenIssuerPort() {
    return new MockTokenIssuer();
  }
}
