package com.example.demo.controller;

import java.io.UnsupportedEncodingException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.AccountService;

import jakarta.mail.MessagingException;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api")
public class AccountController {

    private final AccountService accountService;

    public record CadastroRequest(
            String email,
            String username,
            String password,
            String confirmPassword) {
    }

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/createAccount")
    public ResponseEntity<?> createAccount(@RequestBody CadastroRequest request)
            throws UnsupportedEncodingException, MessagingException {

        ResponseEntity<?> createdUser = accountService.validateAccountCreation(request.email(), request.username(),
                request.password(), request.confirmPassword());

        return createdUser;

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody CadastroRequest request) {
        ResponseEntity<?> loggedUser = accountService.login(request.username(), request.password());
        return loggedUser;
    }

    @GetMapping("/verify")
    public String verifyEmail(@RequestParam("token") String token) {
        boolean isEnabled = accountService.verify(token);
        return isEnabled ? "Conta verificada com sucesso!" : "Token inválido ou conta já verificada.";
    }
}
