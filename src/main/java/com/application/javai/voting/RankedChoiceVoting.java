package com.application.javai.voting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Implementação genérica de Ranked Choice Voting (Instant-Runoff Voting).
 *
 * T representa o tipo de candidato (ex.: Long com o ID do lugar, PlaceDTO, etc).
 *
 * Requisitos:
 * - T deve implementar equals/hashCode de forma consistente (por exemplo, baseado no ID).
 */
public class RankedChoiceVoting<T> {

    /**
     * Resultado de um único round da votação.
     */
    public static class RoundResult<T> {
        private final Map<T, Integer> tallyPerCandidate;
        private final Set<T> eliminatedThisRound;
        private final int totalActiveVotes;

        public RoundResult(Map<T, Integer> tallyPerCandidate,
                           Set<T> eliminatedThisRound,
                           int totalActiveVotes) {
            this.tallyPerCandidate = Collections.unmodifiableMap(new LinkedHashMap<>(tallyPerCandidate));
            this.eliminatedThisRound = Collections.unmodifiableSet(new LinkedHashSet<>(eliminatedThisRound));
            this.totalActiveVotes = totalActiveVotes;
        }

        public Map<T, Integer> getTallyPerCandidate() {
            return tallyPerCandidate;
        }

        public Set<T> getEliminatedThisRound() {
            return eliminatedThisRound;
        }

        public int getTotalActiveVotes() {
            return totalActiveVotes;
        }
    }

    /**
     * Resultado completo da eleição.
     */
    public static class ElectionResult<T> {
        private final T winner;                 // pode ser null em caso de empate sem vencedor único
        private final boolean tie;             // true se não houve vencedor único
        private final List<RoundResult<T>> rounds;

        public ElectionResult(T winner, boolean tie, List<RoundResult<T>> rounds) {
            this.winner = winner;
            this.tie = tie;
            this.rounds = Collections.unmodifiableList(new ArrayList<>(rounds));
        }

        public Optional<T> getWinner() {
            return Optional.ofNullable(winner);
        }

        public boolean isTie() {
            return tie;
        }

        public List<RoundResult<T>> getRounds() {
            return rounds;
        }
    }

    /**
     * Executa o algoritmo de RCV.
     *
     * @param allCandidates conjunto de todos os candidatos possíveis
     * @param ballots       lista de votos; cada voto é uma lista ordenada de preferências
     * @return ElectionResult com vencedor (se houver) e detalhes dos rounds
     */
    public ElectionResult<T> computeWinner(Collection<T> allCandidates,
                                           List<List<T>> ballots) {

        // Conjunto mutável de candidatos que ainda estão na disputa
        Set<T> activeCandidates = new LinkedHashSet<>(allCandidates);

        // Normaliza os votos: remove duplicados e candidatos inexistentes
        List<List<T>> normalizedBallots = normalizeBallots(ballots, activeCandidates);

        List<RoundResult<T>> rounds = new ArrayList<>();

        while (!activeCandidates.isEmpty()) {
            // 1) Contar primeira preferência entre candidatos ativos
            Map<T, Integer> tally = new LinkedHashMap<>();
            for (T candidate : activeCandidates) {
                tally.put(candidate, 0);
            }

            int totalActiveVotes = 0;

            for (List<T> ballot : normalizedBallots) {
                // Para cada voto, encontra o primeiro candidato ainda ativo
                T top = firstActivePreference(ballot, activeCandidates);
                if (top != null) {
                    tally.put(top, tally.get(top) + 1);
                    totalActiveVotes++;
                }
            }

            if (totalActiveVotes == 0) {
                // Todos os votos se esgotaram (ninguém com preferência restante)
                rounds.add(new RoundResult<>(tally, Collections.emptySet(), totalActiveVotes));
                return new ElectionResult<>(null, true, rounds);
            }

            // 2) Verifica se algum candidato tem > 50% dos votos válidos
            T majorityCandidate = null;
            for (Map.Entry<T, Integer> entry : tally.entrySet()) {
                int votes = entry.getValue();
                if (votes * 2 > totalActiveVotes) { // > 50%
                    majorityCandidate = entry.getKey();
                    break;
                }
            }

            if (majorityCandidate != null) {
                // Registramos o round final e retornamos vencedor
                rounds.add(new RoundResult<>(tally, Collections.emptySet(), totalActiveVotes));
                return new ElectionResult<>(majorityCandidate, false, rounds);
            }

            // 3) Ninguém tem maioria → eliminar o(s) candidato(s) com menos votos
            int minVotes = Integer.MAX_VALUE;
            for (Map.Entry<T, Integer> entry : tally.entrySet()) {
                minVotes = Math.min(minVotes, entry.getValue());
            }

            // Candidatos com o menor número de votos
            Set<T> toEliminate = new LinkedHashSet<>();
            for (Map.Entry<T, Integer> entry : tally.entrySet()) {
                if (entry.getValue() == minVotes) {
                    toEliminate.add(entry.getKey());
                }
            }

            // Se todos os ativos têm o mesmo número de votos → empate
            if (toEliminate.size() == activeCandidates.size()) {
                rounds.add(new RoundResult<>(tally, Collections.emptySet(), totalActiveVotes));
                return new ElectionResult<>(null, true, rounds);
            }

            // Registra o round com os eliminados
            rounds.add(new RoundResult<>(tally, toEliminate, totalActiveVotes));

            // Remove eliminados e passa para o próximo round
            activeCandidates.removeAll(toEliminate);
        }

        // Se saiu do loop sem vencedor, é empate
        return new ElectionResult<>(null, true, rounds);
    }

    /**
     * Normaliza os votos:
     * - Remove valores null
     * - Remove candidatos que não estão em allCandidates
     * - Remove duplicatas mantendo a primeira ocorrência
     */
    private List<List<T>> normalizeBallots(List<List<T>> ballots, Set<T> allCandidates) {
        List<List<T>> result = new ArrayList<>();
        if (ballots == null) return result;

        for (List<T> ballot : ballots) {
            if (ballot == null || ballot.isEmpty()) continue;

            List<T> cleaned = new ArrayList<>();
            Set<T> seen = new HashSet<>();

            for (T candidate : ballot) {
                if (candidate == null) continue;
                if (!allCandidates.contains(candidate)) continue;
                if (seen.contains(candidate)) continue;

                seen.add(candidate);
                cleaned.add(candidate);
            }

            if (!cleaned.isEmpty()) {
                result.add(cleaned);
            }
        }
        return result;
    }

    /**
     * Retorna a primeira preferência de um voto que ainda esteja entre os candidatos ativos.
     */
    private T firstActivePreference(List<T> ballot, Set<T> activeCandidates) {
        for (T candidate : ballot) {
            if (activeCandidates.contains(candidate)) {
                return candidate;
            }
        }
        return null;
    }
}
