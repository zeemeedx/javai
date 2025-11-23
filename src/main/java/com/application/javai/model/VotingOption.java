package com.application.javai.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "voting_options")
public class VotingOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id")
    private VotingSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "favorite_place_id", nullable = true)
    private FavoritePlace favoritePlace;

    @Column(nullable = false)
    private int orderIndex;

    public Long getId() {
        return id;
    }

    public VotingSession getSession() {
        return session;
    }

    public void setSession(VotingSession session) {
        this.session = session;
    }

    public FavoritePlace getFavoritePlace() {
        return favoritePlace;
    }

    public void setFavoritePlace(FavoritePlace favoritePlace) {
        this.favoritePlace = favoritePlace;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
}
