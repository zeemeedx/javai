package com.application.javai;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void removeUser(Long userId) {
        userRepository.deleteById(userId);
    }
}
