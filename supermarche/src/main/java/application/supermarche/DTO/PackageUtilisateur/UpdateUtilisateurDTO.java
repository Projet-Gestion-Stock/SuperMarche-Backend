package application.supermarche.DTO.PackageUtilisateur;

public record UpdateUtilisateurDTO(
        String nom,
        String email,
        String role  // Ajout du champ role
) {}