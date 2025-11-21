package com.application.javai.dto;

import java.util.List;

public record CreateVotingSessionRequest(
        Long roomId,
        List<Long> favoritePlaceIds
) {}
