package com.application.javai.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.application.javai.model.User;
import com.application.javai.model.FriendRequest;
import com.application.javai.model.FriendRequestStatus;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    @Query("""
        select fr
        from FriendRequest fr
        where (fr.requester = :user or fr.receiver = :user)
          and fr.status = :status
        """)
    List<FriendRequest> findAllByUserAndStatus(@Param("user") User user,
                                               @Param("status") FriendRequestStatus status);

    // Todos os pedidos entre dois usuários (qualquer direção), mais recentes primeiro
    @Query("""
        select fr
        from FriendRequest fr
        where (fr.requester = :u1 and fr.receiver = :u2)
           or (fr.requester = :u2 and fr.receiver = :u1)
        order by fr.createdAt desc
        """)
    List<FriendRequest> findAllBetweenUsers(@Param("u1") User u1,
                                            @Param("u2") User u2);

    // Pedidos pendentes recebidos por um usuário
    List<FriendRequest> findByReceiverAndStatusOrderByCreatedAtDesc(User receiver,
                                                                    FriendRequestStatus status);

    // Pedidos pendentes enviados por um usuário
    List<FriendRequest> findByRequesterAndStatusOrderByCreatedAtDesc(User requester,
                                                                     FriendRequestStatus status);

    @Query("""
        select fr
        from FriendRequest fr
        where fr.receiver.id = :requestedId
          and fr.status = :status
        order by fr.createdAt desc
        """)
    List<FriendRequest> findByRequestedIdAndStatus(@Param("requestedId") Long requestedId,
                                                   @Param("status") FriendRequestStatus status);
}
