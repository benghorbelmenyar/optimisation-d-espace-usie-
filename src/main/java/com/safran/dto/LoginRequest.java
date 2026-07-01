package com.safran.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String motDePasse;
    private boolean rememberMe; // Capturé depuis la case à cocher du Front Angular
}