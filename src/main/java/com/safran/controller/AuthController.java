package com.safran.controller;

import com.safran.dto.AuthResponse;
import com.safran.dto.LoginRequest;
import com.safran.entity.Utilisateur;
import com.safran.security.JwtUtils;
import com.safran.security.TokenBlacklistService;
import com.safran.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UtilisateurService utilisateurService;
    private final TokenBlacklistService tokenBlacklistService; // 👈 À injecter
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authentifier(@RequestBody LoginRequest request) {
        try {
            Utilisateur utilisateur = utilisateurService.login(request.getEmail(), request.getMotDePasse());
            String token = jwtUtils.generateToken(utilisateur);

            return ResponseEntity.ok(new AuthResponse(token, utilisateur.getEmail(), utilisateur.getRole().name()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String header) {
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            // On pousse le token dans la liste noire
            tokenBlacklistService.blacklistToken(token);

            return ResponseEntity.ok("Déconnexion réussie. Le jeton de session a été révoqué.");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("En-tête Authorization invalide ou manquant.");
    }
}