package application.supermarche.DTO.PackageUtilisateur;

import application.supermarche.Entites.PackageUtilisateur.Utilisateur;

public record UtilisateurAvecEtatDTO(
        Long id,
        String nom,
        String email,
        String role,
        boolean actif
) {
    // Constructeur alternatif pour conversion depuis Utilisateur
    public UtilisateurAvecEtatDTO(Utilisateur utilisateur) {
        this(
                utilisateur.getId(),
                utilisateur.getNom(),
                utilisateur.getEmail(),
                utilisateur.getRole().name(),
                utilisateur.isActif()
        );
    }
}