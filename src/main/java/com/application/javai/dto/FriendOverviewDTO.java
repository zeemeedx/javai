package com.application.javai.dto;

import java.util.List;

public record FriendOverviewDTO(
        List<UserSummaryDTO> friends,
        List<FriendRequestDTO> incomingRequests,
        List<FriendRequestDTO> outgoingRequests
) {}
