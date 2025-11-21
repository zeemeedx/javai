package com.application.javai.dto;

public record FriendRequestDTO(
        Long id,
        UserSummaryDTO requester,
        UserSummaryDTO receiver,
        String status
) {}
