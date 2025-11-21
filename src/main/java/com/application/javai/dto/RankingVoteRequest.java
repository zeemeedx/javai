package com.application.javai.dto;

import java.util.List;

public record RankingVoteRequest(
        List<Long> orderedOptionIds
) {}
