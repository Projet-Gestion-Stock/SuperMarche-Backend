package application.supermarche.DTO.PackageUtilisateur;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InscriptionUtilisateurDTO(
        @NotBlank String nom,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6) String motDePasse, // Nom exact comme dans Postman
        @NotBlank String role
) {}