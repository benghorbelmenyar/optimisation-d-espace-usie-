package com.safran.controller;

import com.safran.dto.AuthResponse;
import com.safran.dto.LoginRequest;
import com.safran.entity.Utilisateur;
import com.safran.security.TokenBlacklistService;
import com.safran.service.JwtService;
import com.safran.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Évite les blocages CORS avec ton serveur de dev Angular
public class AuthController {

    private final UtilisateurService utilisateurService;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtService jwtService; // Injection harmonisée avec la classe JwtService

    @PostMapping("/login")
    public ResponseEntity<?> authentifier(@RequestBody LoginRequest request) {
        try {
            // 1. Authentification via la base de données
            Utilisateur utilisateur = utilisateurService.login(request.getEmail(), request.getMotDePasse());

            // 2. Génération du token adapté à l'option Cochée / Décochée "Remember Me"
            String token = jwtService.generateToken(utilisateur, request.isRememberMe());
            long expiresIn = jwtService.getExpirationTime(request.isRememberMe());

            // 3. Retour de l'objet complet avec les 4 arguments requis par le DTO AuthResponse
            AuthResponse response = new AuthResponse(
                    token,
                    utilisateur.getEmail(),
                    utilisateur.getRole().name(),
                    expiresIn
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader(value = "Authorization", required = false) String header) {
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            // Révocation du token via insertion dans la Blacklist
            tokenBlacklistService.blacklistToken(token);

            return ResponseEntity.ok("Déconnexion réussie. Le jeton de session a été révoqué.");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("En-tête Authorization invalide ou manquant.");
    }
}