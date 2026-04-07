package com.naatalvote.application.vote.usecases;

import java.util.UUID;

public interface CastVoteUseCase {
  CastVoteResponse cast(CastVoteRequest request);

  record CastVoteRequest(UUID electionId, UUID candidatId, UUID citoyenId) {}

  record CastVoteResponse(boolean success, UUID tokenAnonyme, String message) {}
}

