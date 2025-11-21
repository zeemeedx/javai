package com.application.javai.dto;

import java.util.List;

import com.application.javai.model.ChatRoomType;

public record ChatRoomDTO(
        Long id,
        String name,
        String displayName,
        ChatRoomType type,
        Long adminId,
        List<UserSummaryDTO> participants
) {}
