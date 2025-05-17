package application.supermarche.DTO.PackageUtilisateur;


public record UtilisateurDTO(
        Long id,
        String nom,
        String email,
        String role
       // boolean actif
) {
}
