package com.safran.config;

import com.safran.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true) // 👈 Active la détection des annotations @PreAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter; // 👈 Injection de notre filtre d'interception automatique

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // 🛡️ Désactivation du CSRF car nous travaillons avec des jetons JWT (sans état)
                .csrf().disable()

                // ⚡ Gestion de session sans état (aucune session HTTP classique stockée côté serveur)
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()

                // 🚥 Règles de filtrage des requêtes HTTP
                .authorizeRequests()
                // 🔓 Ressources publiques documentaires (Swagger v3 / OpenAPI)
                .antMatchers(
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-ui.html"
                ).permitAll()

                // 🔓 Endpoint d'authentification public (Génération du token)
                .antMatchers("/api/auth/**").permitAll()

                // 🔓 Permettre la création anonyme d'un premier compte utilisateur (S'enregistrer)
                .antMatchers(HttpMethod.POST, "/api/utilisateurs").permitAll()

                // 🔒 TOUTES les autres requêtes de l'application Safran doivent être authentifiées par jeton
                .anyRequest().authenticated()
                .and()

                // 🚫 Désactivation complète des formulaires de connexion et d'authentification HTTP basique standard
                .formLogin().disable()
                .httpBasic().disable();

        // ⛓️ Insertion du décodeur de jetons JWT juste avant le mécanisme de validation d'identité standard
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}