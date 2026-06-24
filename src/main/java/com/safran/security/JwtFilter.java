package com.safran.security;

import com.safran.entity.Utilisateur;
import com.safran.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 👈 1. IMPORT AJOUTÉ
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j // 👈 2. ANNOTATION AJOUTÉE (Génère la variable 'log' automatiquement)
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UtilisateurRepository utilisateurRepository;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            // 🛡️ VÉRIFICATION DE LA BLACKLIST
            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                log.warn("Tentative d'accès refusée : le token a été révoqué par un logout.");
                // Sécurité stricte : On renvoie directement un 401 Unauthorized sans continuer la chaîne
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write("Ce jeton de session est révoqué (Déconnecté).");
                return;
            }

            // Validation mathématique du token
            if (jwtUtils.validateToken(token)) {
                String email = jwtUtils.getEmailFromToken(token);
                Utilisateur utilisateur = utilisateurRepository.findByEmail(email).orElse(null);

                if (utilisateur != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + utilisateur.getRole().name());

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            utilisateur, null, Collections.singletonList(authority)
                    );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}