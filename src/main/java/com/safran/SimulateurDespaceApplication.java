package com.safran;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class SimulateurDespaceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimulateurDespaceApplication.class, args);
    }

    // Avec ce Bean, Spring pourra enfin l'injecter dans votre UtilisateurService !
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}