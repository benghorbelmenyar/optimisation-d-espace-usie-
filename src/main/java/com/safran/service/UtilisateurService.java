package com.safran.service;

import com.safran.dto.UtilisateurDTO;
import com.safran.entity.Utilisateur;
import com.safran.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UtilisateurDTO> findAll() {
        return utilisateurRepository.findAll()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public UtilisateurDTO findById(Long id) {
        return toDTO(utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec id : " + id)));
    }

    public UtilisateurDTO create(UtilisateurDTO dto) {
        Utilisateur utilisateur = Utilisateur.builder()
                .nom(dto.getNom())
                .email(dto.getEmail())
                .motDePasseHash(passwordEncoder.encode(dto.getMotDePasse()))
                .role(dto.getRole())
                .dateCreation(LocalDate.now())
                .build();
        return toDTO(utilisateurRepository.save(utilisateur));
    }

    public UtilisateurDTO update(Long id, UtilisateurDTO dto) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec id : " + id));
        utilisateur.setNom(dto.getNom());
        utilisateur.setEmail(dto.getEmail());
        utilisateur.setRole(dto.getRole());
        if (dto.getMotDePasse() != null && !dto.getMotDePasse().isEmpty()) {
            utilisateur.setMotDePasseHash(passwordEncoder.encode(dto.getMotDePasse()));
        }
        return toDTO(utilisateurRepository.save(utilisateur));
    }

    public void delete(Long id) {
        utilisateurRepository.deleteById(id);
    }

    private UtilisateurDTO toDTO(Utilisateur u) {
        return UtilisateurDTO.builder()
                .id(u.getId()).nom(u.getNom()).email(u.getEmail())
                .role(u.getRole()).dateCreation(u.getDateCreation())
                .build(); // motDePasse volontairement non renvoyé
    }
}