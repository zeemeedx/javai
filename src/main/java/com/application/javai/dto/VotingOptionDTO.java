package com.application.javai.dto;

public record VotingOptionDTO(
        Long id,
        FavoritePlaceDTO place,
        int orderIndex
) {}
