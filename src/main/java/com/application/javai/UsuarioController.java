package com.application.javai;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UsuarioController {
    private final UserRepository userRepository;

    public UsuarioController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public List<Usuario> getUsuarios() {
        return userRepository.findAll();
    }

    @GetMapping("/user/{uid}")
    public Usuario getUsuario(@PathVariable long uid) {
        return userRepository.findById(uid)
                .orElse(null);
    }
}
