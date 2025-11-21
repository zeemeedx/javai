package com.application.javai.dto;

import java.time.Instant;

public record ChatMessageDTO(
        Long id,
        Long roomId,
        UserSummaryDTO sender,
        String content,
        Instant createdAt,
        FavoritePlaceDTO favoritePlace
) {}
