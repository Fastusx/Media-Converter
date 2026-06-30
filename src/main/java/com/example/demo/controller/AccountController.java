package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.service.AccountService;
import org.springframework.web.bind.annotation.RequestMapping;

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
    public ResponseEntity<?> createAccount(@RequestBody CadastroRequest request) {

        ResponseEntity<?> createdUser = accountService.validateAccountCreation(request.email(), request.username(),
                request.password(), request.confirmPassword());

        return createdUser;

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody CadastroRequest request) {
        ResponseEntity<?> loggedUser = accountService.login(request.username(), request.password());
        return loggedUser;
    }

}
