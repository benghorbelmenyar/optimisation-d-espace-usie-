package com.safran.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String username;
    private String password;
    private boolean rememberMe; // 👈 Capturé depuis le front
}