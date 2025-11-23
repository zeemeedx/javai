package com.application.javai.dto;

import java.time.Instant;
import java.util.List;

import com.application.javai.model.VotingStatus;

public record VotingSessionDTO(
        Long id,
        Long roomId,
        VotingStatus status,
        Instant createdAt,
        UserSummaryDTO createdBy,
        List<VotingOptionDTO> options,
        VotingOptionDTO winningOption,
        List<Long> myVote
) {}
