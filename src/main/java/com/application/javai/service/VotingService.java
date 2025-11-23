package com.application.javai.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.application.javai.dto.FavoritePlaceDTO;
import com.application.javai.dto.UserSummaryDTO;
import com.application.javai.dto.VotingOptionDTO;
import com.application.javai.dto.VotingSessionDTO;
import com.application.javai.model.ChatRoom;
import com.application.javai.model.ChatRoomType;
import com.application.javai.model.FavoritePlace;
import com.application.javai.model.RankingVote;
import com.application.javai.model.User;
import com.application.javai.model.VotingOption;
import com.application.javai.model.VotingSession;
import com.application.javai.model.VotingStatus;
import com.application.javai.repository.ChatRoomRepository;
import com.application.javai.repository.FavoritePlaceRepository;
import com.application.javai.repository.RankingVoteRepository;
import com.application.javai.repository.UserRepository;
import com.application.javai.repository.VotingOptionRepository;
import com.application.javai.repository.VotingSessionRepository;

@Service
public class VotingService {

    private final VotingSessionRepository votingSessionRepository;
    private final VotingOptionRepository votingOptionRepository;
    private final RankingVoteRepository rankingVoteRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final FavoritePlaceRepository favoritePlaceRepository;
    private final UserRepository userRepository;
    private final RankedChoiceVotingService rankedChoiceVotingService;

    public VotingService(VotingSessionRepository votingSessionRepository,
                         VotingOptionRepository votingOptionRepository,
                         RankingVoteRepository rankingVoteRepository,
                         ChatRoomRepository chatRoomRepository,
                         FavoritePlaceRepository favoritePlaceRepository,
                         UserRepository userRepository,
                         RankedChoiceVotingService rankedChoiceVotingService) {
        this.votingSessionRepository = votingSessionRepository;
        this.votingOptionRepository = votingOptionRepository;
        this.rankingVoteRepository = rankingVoteRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.favoritePlaceRepository = favoritePlaceRepository;
        this.userRepository = userRepository;
        this.rankedChoiceVotingService = rankedChoiceVotingService;
    }

    @Transactional
    public VotingSessionDTO abrirSessao(Long roomId, List<Long> favoritePlaceIds) {
        User currentUser = getAuthenticatedUser();
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new NoSuchElementException("Sala não encontrada."));

        if (room.getType() != ChatRoomType.GROUP) {
            throw new IllegalArgumentException("Apenas grupos suportam votação.");
        }

        ensureUserIsAdmin(room, currentUser);

        votingSessionRepository.findFirstByRoomAndStatus(room, VotingStatus.OPEN)
                .ifPresent(session -> {
                    throw new IllegalStateException("Já existe uma votação aberta para este grupo.");
                });

        if (favoritePlaceIds == null || favoritePlaceIds.size() < 2) {
            throw new IllegalArgumentException("Selecione ao menos dois lugares para votação.");
        }

        VotingSession session = new VotingSession();
        session.setRoom(room);
        session.setCreatedBy(currentUser);
        session.setStatus(VotingStatus.OPEN);

        VotingSession savedSession = votingSessionRepository.save(session);

        List<FavoritePlace> places = favoritePlaceRepository
                .findAllById(favoritePlaceIds);

        if (places.size() != favoritePlaceIds.size()) {
            throw new NoSuchElementException("Algum lugar favorito não existe mais.");
        }

        int index = 0;
        for (FavoritePlace place : places) {
            VotingOption option = new VotingOption();
            option.setSession(savedSession);
            option.setFavoritePlace(place);
            option.setOrderIndex(index++);
            votingOptionRepository.save(option);
        }

        savedSession.setOptions(new LinkedHashSet<>(votingOptionRepository.findBySessionOrderByOrderIndexAsc(savedSession)));

        return toSessionDTO(savedSession);
    }

    @Transactional
    public void registrarVoto(Long sessionId, List<Long> orderedOptionIds) {
        User currentUser = getAuthenticatedUser();
        VotingSession session = votingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NoSuchElementException("Sessão de votação não encontrada."));

        if (session.getStatus() != VotingStatus.OPEN) {
            throw new IllegalStateException("Esta sessão já foi encerrada.");
        }

        ensureRoomParticipant(session.getRoom(), currentUser);

        List<VotingOption> options = votingOptionRepository.findBySessionOrderByOrderIndexAsc(session);
        Map<Long, VotingOption> optionMap = options.stream()
                .collect(Collectors.toMap(VotingOption::getId, Function.identity()));

        if (orderedOptionIds == null || orderedOptionIds.size() != options.size()) {
            throw new IllegalArgumentException("É necessário ranquear todas as opções.");
        }

        Set<Long> uniqueIds = new LinkedHashSet<>(orderedOptionIds);
        if (uniqueIds.size() != options.size() || uniqueIds.size() != orderedOptionIds.size()) {
            throw new IllegalArgumentException("Não repita opções no ranking.");
        }

        List<VotingOption> orderedOptions = new ArrayList<>();
        for (Long optionId : orderedOptionIds) {
            VotingOption option = optionMap.get(optionId);
            if (option == null) {
                throw new NoSuchElementException("Opção inválida.");
            }
            orderedOptions.add(option);
        }

        RankingVote vote = rankingVoteRepository.findBySessionAndVoter(session, currentUser)
                .orElseGet(() -> {
                    RankingVote newVote = new RankingVote();
                    newVote.setSession(session);
                    newVote.setVoter(currentUser);
                    return newVote;
                });

        vote.setRankingJson(orderedOptionIds.toString());
        rankingVoteRepository.save(vote);
    }

    @Transactional
    public VotingSessionDTO encerrarSessao(Long sessionId) {
        User currentUser = getAuthenticatedUser();
        VotingSession session = votingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NoSuchElementException("Sessão de votação não encontrada."));

        if (session.getStatus() == VotingStatus.CLOSED) {
            return toSessionDTO(session);
        }

        ensureRoomParticipant(session.getRoom(), currentUser);
        if (session.getRoom().getAdmin() == null ||
            !session.getRoom().getAdmin().getId().equals(currentUser.getId())) {
            throw new SecurityException("Apenas o admin pode encerrar a votação.");
        }

        List<VotingOption> options = votingOptionRepository.findBySessionOrderByOrderIndexAsc(session);
        List<RankingVote> votes = rankingVoteRepository.findBySession(session);

        List<List<VotingOption>> ballots = votes.stream()
                .map(vote -> parseRankingJson(vote.getRankingJson(), options))
                .toList();

        var result = rankedChoiceVotingService.computeWinner(options, ballots);
        result.getWinner().ifPresent(session::setWinningOption);
        session.setStatus(VotingStatus.CLOSED);

        VotingSession saved = votingSessionRepository.save(session);
        return toSessionDTO(saved);
    }

    @Transactional(readOnly = true)
    public VotingSessionDTO detalhesSessao(Long sessionId) {
        VotingSession session = votingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NoSuchElementException("Sessão não encontrada."));
        ensureRoomParticipant(session.getRoom(), getAuthenticatedUser());
        return toSessionDTO(session);
    }

    @Transactional(readOnly = true)
    public VotingSessionDTO sessaoAbertaDoGrupo(Long roomId) {
        User currentUser = getAuthenticatedUser();
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new NoSuchElementException("Sala não encontrada."));
        ensureRoomParticipant(room, currentUser);
        return votingSessionRepository.findFirstByRoomAndStatus(room, VotingStatus.OPEN)
                .map(this::toSessionDTO)
                .orElse(null);
    }

    private List<VotingOption> parseRankingJson(String rankingJson, List<VotingOption> options) {
        if (rankingJson == null || rankingJson.isBlank()) {
            return List.of();
        }
        String cleaned = rankingJson
                .replace("[", "")
                .replace("]", "")
                .trim();
        if (cleaned.isEmpty()) {
            return List.of();
        }
        String[] parts = cleaned.split(",");
        Map<Long, VotingOption> optionMap = options.stream()
                .collect(Collectors.toMap(VotingOption::getId, Function.identity()));
        List<VotingOption> ordered = new ArrayList<>();
        for (String part : parts) {
            Long optionId = Long.valueOf(part.trim());
            VotingOption option = optionMap.get(optionId);
            if (option != null) {
                ordered.add(option);
            }
        }
        return ordered;
    }

    private List<Long> parseRankingJsonToIds(String rankingJson) {
        if (rankingJson == null || rankingJson.isBlank()) {
            return List.of();
        }
        String cleaned = rankingJson
                .replace("[", "")
                .replace("]", "")
                .trim();
        if (cleaned.isEmpty()) {
            return List.of();
        }
        String[] parts = cleaned.split(",");
        List<Long> ordered = new ArrayList<>();
        for (String part : parts) {
            ordered.add(Long.valueOf(part.trim()));
        }
        return ordered;
    }

    private VotingSessionDTO toSessionDTO(VotingSession session) {
        User currentUser = getAuthenticatedUser();
        List<VotingOption> options = ensureOptionsLoaded(session);
        List<VotingOptionDTO> optionDTOs = options.stream()
                .sorted((o1, o2) -> Integer.compare(o1.getOrderIndex(), o2.getOrderIndex()))
                .map(this::toOptionDTO)
                .toList();

        List<Long> myVote = rankingVoteRepository.findBySessionAndVoter(session, currentUser)
                .map(vote -> parseRankingJsonToIds(vote.getRankingJson()))
                .orElse(null);

        VotingOptionDTO winning = session.getWinningOption() != null
                ? toOptionDTO(session.getWinningOption())
                : null;
        return new VotingSessionDTO(
                session.getId(),
                session.getRoom().getId(),
                session.getStatus(),
                session.getCreatedAt(),
                toUserSummary(session.getCreatedBy()),
                optionDTOs,
                winning,
                myVote
        );
    }

    private List<VotingOption> ensureOptionsLoaded(VotingSession session) {
        if (session.getOptions() == null || session.getOptions().isEmpty()) {
            session.setOptions(new LinkedHashSet<>(votingOptionRepository.findBySessionOrderByOrderIndexAsc(session)));
        }
        return new ArrayList<>(session.getOptions());
    }

    private VotingOptionDTO toOptionDTO(VotingOption option) {
        FavoritePlace place = option.getFavoritePlace();
        FavoritePlaceDTO placeDTO = new FavoritePlaceDTO(
                place.getId(),
                place.getExternalId(),
                place.getName(),
                place.getType(),
                place.getLat(),
                place.getLon(),
                place.getSource()
        );
        return new VotingOptionDTO(
                option.getId(),
                placeDTO,
                option.getOrderIndex()
        );
    }

    private void ensureUserIsAdmin(ChatRoom room, User user) {
        if (room.getAdmin() == null || !room.getAdmin().getId().equals(user.getId())) {
            throw new SecurityException("Apenas o admin pode realizar esta ação.");
        }
    }

    private void ensureRoomParticipant(ChatRoom room, User user) {
        boolean isParticipant = room.getParticipants().stream()
                .anyMatch(participant -> participant.getId().equals(user.getId()));
        if (!isParticipant) {
            throw new SecurityException("Você não participa deste grupo.");
        }
    }

    private UserSummaryDTO toUserSummary(User user) {
        return new UserSummaryDTO(user.getId(), user.getNome(), user.getEmail());
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("Usuário não autenticado.");
        }

        Object principal = authentication.getPrincipal();
        String username;
        if (principal instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        } else if (principal instanceof User user) {
            username = user.getEmail();
        } else if (principal instanceof String str) {
            username = str;
        } else {
            throw new IllegalStateException("Principal de autenticação inválido.");
        }

        return userRepository.findByEmail(username)
                .orElseThrow(() -> new NoSuchElementException("Usuário autenticado não encontrado."));
    }
}
