package com.safran.service;

import com.safran.dto.UtilisateurDTO;
import com.safran.entity.Utilisateur;
import com.safran.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UtilisateurDTO> findAll() {
        return utilisateurRepository.findAll()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public UtilisateurDTO findById(Long id) {
        return utilisateurRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec id : " + id));
    }

    @Transactional
    public UtilisateurDTO create(UtilisateurDTO dto) {
        log.info("[INSCRIPTION] Création de l'utilisateur : {}", dto.getEmail());

        if (dto.getMotDePasse() == null || dto.getMotDePasse().isEmpty()) {
            throw new IllegalArgumentException("Le mot de passe en clair est obligatoire pour la création.");
        }

        Utilisateur utilisateur = Utilisateur.builder()
                .nom(dto.getNom())
                .email(dto.getEmail())
                // 🔒 AUTOMATIQUE : On récupère le mot de passe en clair du DTO et on le hache ici !
                .motDePasseHash(passwordEncoder.encode(dto.getMotDePasse()))
                .role(dto.getRole())
                .dateCreation(LocalDate.now())
                .build();

        return toDTO(utilisateurRepository.save(utilisateur));
    }

    @Transactional
    public UtilisateurDTO update(Long id, UtilisateurDTO dto) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec id : " + id));

        utilisateur.setNom(dto.getNom());
        utilisateur.setEmail(dto.getEmail());
        utilisateur.setRole(dto.getRole());

        // 🔒 Si un nouveau mot de passe en clair est fourni, on le re-hache automatiquement
        if (dto.getMotDePasse() != null && !dto.getMotDePasse().isEmpty()) {
            utilisateur.setMotDePasseHash(passwordEncoder.encode(dto.getMotDePasse()));
        }

        return toDTO(utilisateurRepository.save(utilisateur));
    }

    public void delete(Long id) {
        utilisateurRepository.deleteById(id);
    }

    public Utilisateur login(String email, String rawPassword) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Identifiants incorrects (Email introuvable)."));

        if (!passwordEncoder.matches(rawPassword, utilisateur.getMotDePasseHash())) {
            throw new IllegalArgumentException("Identifiants incorrects (Mot de passe erroné).");
        }

        return utilisateur;
    }

    // --- MAPPER PRIVÉ ---
    private UtilisateurDTO toDTO(Utilisateur u) {
        return UtilisateurDTO.builder()
                .id(u.getId())
                .nom(u.getNom())
                .email(u.getEmail())
                .role(u.getRole())
                .dateCreation(u.getDateCreation())
                // 🛡️ SÉCURITÉ : On ne renvoie JAMAIS le mot de passe (ni haché, ni en clair) dans les réponses HTTP
                .build();
    }
}