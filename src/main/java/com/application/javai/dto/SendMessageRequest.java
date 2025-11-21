package com.application.javai.dto;

public record SendMessageRequest(
        String content,
        Long favoritePlaceId
) {}
