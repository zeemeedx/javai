package com.application.javai.dto;

import java.util.List;

public record CreateGroupRoomRequest(
        String name,
        List<Long> participantIds
) {}
