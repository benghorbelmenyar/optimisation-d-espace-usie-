package com.safran.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String email;
    private String role;
    private long expiresIn; // Temps de validité du token renvoyé au Front
}