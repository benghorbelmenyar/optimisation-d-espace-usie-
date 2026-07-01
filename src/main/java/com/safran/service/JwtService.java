package com.safran.service;

import com.safran.entity.Utilisateur;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;

@Service
public class JwtService {

    // 💡 Option 2 : Plus besoin de @Value, la bibliothèque génère une clé parfaite de 512 bits à la volée
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    // 15 minutes par défaut (en millisecondes)
    private final long ACCESS_TOKEN_VALIDITY = 15 * 60 * 1000;

    // 7 jours si "Remember Me" est coché (en millisecondes)
    private final long REMEMBER_ME_VALIDITY = 7 * 24 * 60 * 60 * 1000;

    /**
     * Génère un jeton JWT sécurisé pour l'utilisateur
     */
    public String generateToken(Utilisateur utilisateur, boolean rememberMe) {
        long validity = rememberMe ? REMEMBER_ME_VALIDITY : ACCESS_TOKEN_VALIDITY;
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .setClaims(new HashMap<>())
                .setSubject(utilisateur.getEmail())
                .claim("role", utilisateur.getRole().name()) // Ajout du rôle pour ton AuthService Angular
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + validity))
                .signWith(key) // 💡 On utilise directement la clé sécurisée automatique
                .compact();
    }

    /**
     * Extrait toutes les revendications (Claims) du token en utilisant la clé automatique
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key) // 💡 Utilisation de la même clé pour valider le jeton
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Retourne le temps d'expiration en fonction de l'option Remember Me
     */
    public long getExpirationTime(boolean rememberMe) {
        return rememberMe ? REMEMBER_ME_VALIDITY : ACCESS_TOKEN_VALIDITY;
    }
}