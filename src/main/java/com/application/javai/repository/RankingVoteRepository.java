package com.application.javai.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.application.javai.model.RankingVote;
import com.application.javai.model.User;
import com.application.javai.model.VotingSession;

public interface RankingVoteRepository extends JpaRepository<RankingVote, Long> {

    Optional<RankingVote> findBySessionAndVoter(VotingSession session, User voter);

    List<RankingVote> findBySession(VotingSession session);
}
