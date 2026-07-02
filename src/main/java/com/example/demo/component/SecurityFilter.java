package com.example.demo.component;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.model.User;
import com.example.demo.service.AccountService;
import com.example.demo.service.TokenService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    private final AccountService accountService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = getTokenString(request);

        if (token != null) {
            String login = tokenService.getSubject(token);

            String role = tokenService.getClaimRole(token);

            User user = accountService.getUserByUsernameOrEmail(login);

            var authority = new SimpleGrantedAuthority("ROLE_" + role);

            var authentication = new UsernamePasswordAuthenticationToken(user, null, List.of(authority));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // Segue o fluxo normal da requisição
        filterChain.doFilter(request, response);
    }

    private String getTokenString(HttpServletRequest request) {
        var authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null) {
            String token = authorizationHeader.replace("Bearer ", "");
            if (token.equals("null")) {
                return null;
            }
            return token;
        }
        return null;
    }
}
