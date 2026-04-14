package com.naatalvote.infrastructure.config;

import com.naatalvote.application.admin.AdminService;
import com.naatalvote.application.audit.ports.ActionLogRepositoryPort;
import com.naatalvote.application.auth.AuthService;
import com.naatalvote.application.auth.OtpGenerator;
import com.naatalvote.application.auth.SecureRandomOtpGenerator;
import com.naatalvote.application.auth.ports.CitizenRegistryPort;
import com.naatalvote.application.auth.ports.OtpSenderPort;
import com.naatalvote.application.auth.ports.OtpStorePort;
import com.naatalvote.application.auth.ports.TokenIssuerPort;
import com.naatalvote.application.auth.ports.UserRepositoryPort;
import com.naatalvote.application.common.TimeProvider;
import com.naatalvote.application.election.ElectionService;
import com.naatalvote.application.election.ports.CandidateRepositoryPort;
import com.naatalvote.application.election.ports.ElectionRepositoryPort;
import com.naatalvote.application.fraud.FraudService;
import com.naatalvote.application.fraud.ports.FraudAlertRepositoryPort;
import com.naatalvote.application.fraud.ports.SuspensionRepositoryPort;
import com.naatalvote.application.superadmin.SuperAdminService;
import com.naatalvote.application.vote.VoteService;
import com.naatalvote.application.vote.ports.TraceVoteRepositoryPort;
import com.naatalvote.application.vote.ports.VoteRepositoryPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationServicesConfig {
  @Bean
  public OtpGenerator otpGenerator() {
    return new SecureRandomOtpGenerator();
  }

  @Bean
  public AuthService authService(
      UserRepositoryPort users,
      CitizenRegistryPort citizenRegistry,
      OtpStorePort otpStore,
      OtpSenderPort otpSender,
      TokenIssuerPort tokenIssuer,
      ActionLogRepositoryPort actionLogs,
      OtpGenerator otpGenerator,
      @Value("${naatalvote.auth.debug-return-otp:false}") boolean debugReturnOtp
  ) {
    return new AuthService(users, citizenRegistry, otpStore, otpSender, tokenIssuer, actionLogs, otpGenerator, debugReturnOtp);
  }

  @Bean
  public ElectionService electionService(ElectionRepositoryPort elections, CandidateRepositoryPort candidates, TimeProvider timeProvider) {
    return new ElectionService(elections, candidates, timeProvider);
  }

  @Bean
  public VoteService voteService(VoteRepositoryPort votes, TraceVoteRepositoryPort traces, TimeProvider timeProvider) {
    return new VoteService(votes, traces, timeProvider);
  }

  @Bean
  public FraudService fraudService(FraudAlertRepositoryPort alerts, SuspensionRepositoryPort suspensions) {
    return new FraudService(alerts, suspensions);
  }

  @Bean
  public AdminService adminService(ElectionRepositoryPort elections, VoteRepositoryPort votes, CandidateRepositoryPort candidates) {
    return new AdminService(elections, votes, candidates);
  }

  @Bean
  public SuperAdminService superAdminService(UserRepositoryPort users, ActionLogRepositoryPort logs, SuspensionRepositoryPort suspensions) {
    return new SuperAdminService(users, logs, suspensions);
  }
}
