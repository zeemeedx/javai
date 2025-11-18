package com.application.javai;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {
    private final UserRepository userRepository;
    private final UserService userService;

    public UserController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GetMapping("/users")
    public List<User> getUsuarios() {
        return userRepository.findAll();
    }

    @GetMapping("/user/{uid}")
    public User getUsuario(@PathVariable long uid) {
        return userRepository.findById(uid)
                .orElse(null);
    }

    @DeleteMapping("/user/{uid}")
    public void deleteUsuario(@PathVariable long uid) {
        userService.removeUser(uid);
    }
}
