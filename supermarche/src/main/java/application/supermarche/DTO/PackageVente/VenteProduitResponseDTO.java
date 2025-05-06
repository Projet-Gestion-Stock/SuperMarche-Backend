package application.supermarche.DTO.PackageVente;

public record VenteProduitResponseDTO(
        Long produitId,
        String produitNom, // Nom du produit
        int quantiteVendue,
        double prixUnitaire
) {}
