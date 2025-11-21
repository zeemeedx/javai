package com.application.javai.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.application.javai.model.Friendship;
import com.application.javai.model.User;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    boolean existsByUserAndFriend(User user, User friend);

    void deleteByUserIdAndFriendId(Long userId, Long friendId);
}
