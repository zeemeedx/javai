package com.application.javai.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.application.javai.dto.FavoritePlaceDTO;
import com.application.javai.model.FavoritePlace;
import com.application.javai.model.User;
import com.application.javai.repository.FavoritePlaceRepository;
import com.application.javai.repository.UserRepository;

@Service
public class FavoriteService {

    private final FavoritePlaceRepository favoritePlaceRepository;
    private final UserRepository userRepository;

    public FavoriteService(FavoritePlaceRepository favoritePlaceRepository,
                           UserRepository userRepository) {
        this.favoritePlaceRepository = favoritePlaceRepository;
        this.userRepository = userRepository;
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
