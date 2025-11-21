package com.application.javai.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.application.javai.dto.FavoritePlaceDTO;
import com.application.javai.service.FavoriteService;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public List<FavoritePlaceDTO> listarFavoritos() {
        return favoriteService.listarFavoritosDoUsuarioLogado();
    }

    @PostMapping
    public ResponseEntity<Void> favoritar(@RequestBody FavoritePlaceDTO dto) {
        favoriteService.favoritarLugar(dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        favoriteService.removerFavorito(id);
        return ResponseEntity.noContent().build();
    }
}
