package com.example.demo.service;

import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

@Service
public class AccountService {

    private final UserRepository userRepository;

    public AccountService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ResponseEntity<?> validateAccountCreation(String email, String username, String password,
            String confirmPassword) {

        if (email == null || !email.contains("@") || email.isBlank()) {
            return ResponseEntity.badRequest().body("E-mail inválido ou mal formatado.");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body("E-mail já cadastrado no sistema.");
        }

        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest().body("O nome de usuário não pode estar vazio.");
        }

        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body("O nome de usuário já está em uso.");
        }

        if (password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body("A senha não pode estar vazia.");
        }

        if (password.length() < 8) {
            return ResponseEntity.badRequest().body("A senha deve ter pelo menos 8 caracteres.");
        }

        if (!password.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body("A senha e a confirmação de senha não coincidem.");
        }

        BCryptPasswordEncoder crypt = new BCryptPasswordEncoder();
        String hashPass = crypt.encode(password);
        User newUser = new User(email, username, hashPass);

        return ResponseEntity.ok(userRepository.save(newUser));
    }

    @Scheduled(fixedRate = 180000) // 3 minutos
    public void deleteAllUsers() {
        System.out.println("Banco limpo!");
        userRepository.deleteAll();

    }
}
