package com.application.javai.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.application.javai.model.FavoritePlace;
import com.application.javai.model.User;

public interface FavoritePlaceRepository extends JpaRepository<FavoritePlace, Long> {

    List<FavoritePlace> findByUserOrderByNameAsc(User user);

    Optional<FavoritePlace> findByIdAndUser(Long id, User user);

    @Query("""
        select fp
        from FavoritePlace fp
        where fp.user = :user
          and lower(fp.name) = lower(:name)
          and fp.lat = :lat
          and fp.lon = :lon
        """)
    Optional<FavoritePlace> findByUserAndNameAndCoordinates(@Param("user") User user,
                                                            @Param("name") String name,
                                                            @Param("lat") double lat,
                                                            @Param("lon") double lon);
}
