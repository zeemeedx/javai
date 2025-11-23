package com.application.javai.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.application.javai.model.ChatMessage;
import com.application.javai.model.ChatRoom;
import com.application.javai.model.FavoritePlace;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByRoomOrderByCreatedAtAsc(ChatRoom room);

    List<ChatMessage> findByFavoritePlace(FavoritePlace favoritePlace);

    void deleteByRoom(ChatRoom room);
}
