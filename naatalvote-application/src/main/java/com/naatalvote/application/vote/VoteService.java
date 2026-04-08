package com.naatalvote.application.vote;

import com.naatalvote.application.common.TimeProvider;
import com.naatalvote.application.vote.ports.TraceVoteRepositoryPort;
import com.naatalvote.application.vote.ports.VoteRepositoryPort;
import com.naatalvote.domain.common.DomainException;
import com.naatalvote.domain.common.Ids;
import com.naatalvote.domain.vote.TraceVote;
import com.naatalvote.domain.vote.Vote;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class VoteService {
  private final VoteRepositoryPort votes;
  private final TraceVoteRepositoryPort traces;
  private final TimeProvider time;

  public VoteService(VoteRepositoryPort votes, TraceVoteRepositoryPort traces, TimeProvider time) {
    this.votes = Objects.requireNonNull(votes, "votes");
    this.traces = Objects.requireNonNull(traces, "traces");
    this.time = Objects.requireNonNull(time, "time");
  }

  public CastVoteResult cast(CastVoteCommand cmd) {
    TraceVote existing = traces.findByCitoyenIdAndElectionId(cmd.citoyenId(), cmd.electionId()).orElse(null);
    if (existing != null && existing.aVote()) {
      throw new DomainException("Vote multiple interdit");
    }
    UUID token = cmd.tokenAnonyme() == null ? Ids.newUuid() : cmd.tokenAnonyme();
    Instant now = time.now();
    Vote v = new Vote(Ids.newUuid(), token, cmd.electionId(), cmd.candidatId(), now);
    votes.save(v);
    traces.save(new TraceVote(Ids.newUuid(), cmd.citoyenId(), cmd.electionId(), true, now));
    return new CastVoteResult(true, token, now);
  }

  public Vote verifyToken(UUID token) {
    Vote v = votes.findByToken(token).orElseThrow(() -> new DomainException("Token non trouvé"));
    return v;
  }

  public VerifyResult verify(UUID token) {
    Vote v = verifyToken(token);
    return new VerifyResult(v.electionId(), v.candidatId(), v.horodatage());
  }

  public Results results(UUID electionId) {
    List<Vote> electionVotes = votes.findByElectionId(electionId);
    int total = electionVotes.size();
    Map<UUID, Integer> countByCandidate = new HashMap<>();
    for (Vote v : electionVotes) {
      countByCandidate.put(v.candidatId(), countByCandidate.getOrDefault(v.candidatId(), 0) + 1);
    }
    List<ResultLine> lines = countByCandidate.entrySet().stream()
        .map(e -> new ResultLine(e.getKey(), e.getValue(), total == 0 ? 0.0 : (e.getValue() * 100.0 / total)))
        .toList();
    return new Results(electionId, total, lines);
  }

  public record CastVoteCommand(UUID electionId, UUID candidatId, UUID citoyenId, UUID tokenAnonyme) {}
  public record CastVoteResult(boolean success, UUID tokenAnonyme, Instant horodatage) {}
  public record ResultLine(UUID candidatId, int votes, double percent) {}
  public record Results(UUID electionId, int totalVotes, List<ResultLine> results) {}
  public record VerifyResult(UUID electionId, UUID candidatId, Instant horodatage) {}
}

