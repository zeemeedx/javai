package com.application.javai.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.application.javai.model.ChatRoom;
import com.application.javai.model.ChatRoomType;
import com.application.javai.model.User;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("""
        select distinct cr
        from ChatRoom cr
        join cr.participants p
        where p = :user
        order by cr.name asc
        """)
    List<ChatRoom> findAllByParticipant(@Param("user") User user);

    @Query("""
        select distinct cr
        from ChatRoom cr
        join cr.participants p1
        join cr.participants p2
        where cr.type = :type
          and p1 = :user1
          and p2 = :user2
        """)
    List<ChatRoom> findDirectRoomBetween(@Param("user1") User user1,
                                         @Param("user2") User user2,
                                         @Param("type") ChatRoomType type);

    @Query("""
        select cr from ChatRoom cr
        join cr.participants p
        where cr.type = 'GROUP'
          and cr.admin = :admin
          and p = :participant
        """)
    List<ChatRoom> findGroupRoomsWithAdminAndParticipant(@Param("admin") User admin, @Param("participant") User participant);
}
