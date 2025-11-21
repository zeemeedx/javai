package com.application.javai.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.application.javai.voting.RankedChoiceVoting;

@Service
public class RankedChoiceVotingService {

    public <T> RankedChoiceVoting.ElectionResult<T> computeWinner(
            List<T> options,
            List<List<T>> ballots) {
        RankedChoiceVoting<T> voting = new RankedChoiceVoting<>();
        return voting.computeWinner(options, ballots);
    }
}
