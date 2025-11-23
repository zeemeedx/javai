package com.application.javai.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.application.javai.model.FavoritePlace;
import com.application.javai.model.VotingOption;
import com.application.javai.model.VotingSession;

public interface VotingOptionRepository extends JpaRepository<VotingOption, Long> {

    List<VotingOption> findBySessionOrderByOrderIndexAsc(VotingSession session);

    boolean existsByFavoritePlace(FavoritePlace favoritePlace);
}
