package com.application.javai.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.application.javai.dto.FavoritePlaceDTO;
import com.application.javai.model.ChatMessage;
import com.application.javai.model.FavoritePlace;
import com.application.javai.model.User;
import com.application.javai.repository.ChatMessageRepository;
import com.application.javai.repository.FavoritePlaceRepository;
import com.application.javai.repository.UserRepository;
import com.application.javai.repository.VotingOptionRepository;

@Service
public class FavoriteService {

    private final FavoritePlaceRepository favoritePlaceRepository;
    private final UserRepository userRepository;
    private final VotingOptionRepository votingOptionRepository;
    private final ChatMessageRepository chatMessageRepository;

    public FavoriteService(FavoritePlaceRepository favoritePlaceRepository,
                           UserRepository userRepository,
                           VotingOptionRepository votingOptionRepository,
                           ChatMessageRepository chatMessageRepository) {
        this.favoritePlaceRepository = favoritePlaceRepository;
        this.userRepository = userRepository;
        this.votingOptionRepository = votingOptionRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    @Transactional
    public FavoritePlaceDTO favoritarLugar(FavoritePlaceDTO dto) {
        User user = getAuthenticatedUser();

        String normalizedName = normalizeName(dto.name());

        FavoritePlace favorite = favoritePlaceRepository
                .findByUserAndNameAndCoordinates(user, normalizedName, dto.lat(), dto.lon())
                .orElse(null);

        if (favorite != null) {
            return toDto(favorite);
        }

        favorite = new FavoritePlace();
        favorite.setUser(user);
        favorite.setExternalId(dto.externalId());
        favorite.setName(normalizedName);
        favorite.setType(dto.type());
        favorite.setLat(dto.lat());
        favorite.setLon(dto.lon());
        favorite.setSource(dto.source());

        return toDto(favoritePlaceRepository.save(favorite));
    }

    @Transactional(readOnly = true)
    public List<FavoritePlaceDTO> listarFavoritosDoUsuarioLogado() {
        User user = getAuthenticatedUser();
        return favoritePlaceRepository.findByUserOrderByNameAsc(user)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void removerFavorito(Long favoriteId) {
        User user = getAuthenticatedUser();
        FavoritePlace favorite = favoritePlaceRepository.findByIdAndUser(favoriteId, user)
                .orElseThrow(() -> new NoSuchElementException("Favorito não encontrado."));

        // Check 1: Is it used in a voting session? If so, block deletion.
        if (votingOptionRepository.existsByFavoritePlace(favorite)) {
            throw new IllegalStateException("Este lugar não pode ser removido pois já foi usado em uma votação.");
        }

        // Step 2: Nullify references in ChatMessages
        List<ChatMessage> messagesToUpdate = chatMessageRepository.findByFavoritePlace(favorite);
        if (!messagesToUpdate.isEmpty()) {
            for (ChatMessage message : messagesToUpdate) {
                message.setFavoritePlace(null);
            }
            chatMessageRepository.saveAll(messagesToUpdate);
        }
        
        // Step 3: Delete the favorite place
        favoritePlaceRepository.delete(favorite);
    }

    private FavoritePlaceDTO toDto(FavoritePlace favorite) {
        return new FavoritePlaceDTO(
                favorite.getId(),
                favorite.getExternalId(),
                favorite.getName(),
                favorite.getType(),
                favorite.getLat(),
                favorite.getLon(),
                favorite.getSource()
        );
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

    private String normalizeName(String rawName) {
        if (rawName == null || rawName.trim().isEmpty()) {
            return "(sem nome)";
        }
        return rawName.trim();
    }
}
