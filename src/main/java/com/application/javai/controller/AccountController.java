package com.application.javai.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // ajuste o pacote da sua entidade User
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.application.javai.User;
import com.application.javai.UserRepository;
import com.application.javai.dto.UserDTO;

@RestController
public class AccountController {

    private final UserRepository userRepository;

    public AccountController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/api/me")
    public ResponseEntity<UserDTO> me(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = userDetails.getUsername(); // você está usando email como username

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        UserDTO dto = new UserDTO(
            user.getId(),
            user.getNome(),
            user.getEmail()
        );

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/api/me/avatar")
    public ResponseEntity<Void> uploadAvatar(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("avatar") MultipartFile avatarFile) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 1. descobrir o usuário autenticado (email = userDetails.getUsername())
        // 2. salvar o arquivo (filesystem, S3, banco, etc.)
        // 3. guardar o caminho/URL na entidade User

        return ResponseEntity.ok().build();
    }
}
