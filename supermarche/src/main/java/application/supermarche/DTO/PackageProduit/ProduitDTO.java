package application.supermarche.DTO.PackageProduit;

import application.supermarche.DTO.PackageUtilisateur.UtilisateurDTO;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record ProduitDTO(
        Long id,  // Ajouté pour avoir l'identifiant
        String produit,
        double prix,
        int quantiteDisponible,
        String categorie,
        int seuilAlerte,
        @JsonFormat(pattern = "dd-MM-yyyy")
        LocalDate dateExpiration,
        @JsonFormat(pattern = "dd-MM-yyyy")  // Ajouté pour la date d'ajout
        LocalDate dateAjout,
        String unite,
        String fournisseur,
        String description,
        String statutExpiration,
        UtilisateurDTO utilisateur  // Ajouté pour les infos basiques de l'utilisateur
) {
    public ProduitDTO {
        statutExpiration = statutExpiration != null ? statutExpiration : "BON_ÉTAT";
    }
}