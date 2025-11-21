package com.application.javai.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.application.javai.model.FavoritePlace;
import com.application.javai.model.User;

public interface FavoritePlaceRepository extends JpaRepository<FavoritePlace, Long> {

    boolean existsByUserAndExternalId(User user, String externalId);

    List<FavoritePlace> findByUserOrderByNameAsc(User user);

    Optional<FavoritePlace> findByIdAndUser(Long id, User user);
}
