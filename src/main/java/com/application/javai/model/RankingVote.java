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
@Table(name = "ranking_votes")
public class RankingVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id")
    private VotingSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "voter_id")
    private User voter;

    @Column(name = "ranking_json", nullable = false, columnDefinition = "TEXT")
    private String rankingJson;

    public Long getId() {
        return id;
    }

    public VotingSession getSession() {
        return session;
    }

    public void setSession(VotingSession session) {
        this.session = session;
    }

    public User getVoter() {
        return voter;
    }

    public void setVoter(User voter) {
        this.voter = voter;
    }

    public String getRankingJson() {
        return rankingJson;
    }

    public void setRankingJson(String rankingJson) {
        this.rankingJson = rankingJson;
    }
}
