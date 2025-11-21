package com.application.javai;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query("""
        select u 
        from User u
        where lower(u.nome) like lower(concat('%', :term, '%'))
           or lower(u.email) like lower(concat('%', :term, '%'))
        order by u.nome asc
        """)
    List<User> searchByNameOrEmail(@Param("term") String term);
}
