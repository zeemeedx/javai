package com.application.javai.dto;

public record FavoritePlaceDTO(
        Long id,
        String externalId,
        String name,
        String type,
        double lat,
        double lon,
        String source
) {}
