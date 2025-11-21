package com.application.javai.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.application.javai.model.ChatRoom;
import com.application.javai.model.VotingSession;
import com.application.javai.model.VotingStatus;

public interface VotingSessionRepository extends JpaRepository<VotingSession, Long> {

    Optional<VotingSession> findFirstByRoomAndStatus(ChatRoom room, VotingStatus status);
}
