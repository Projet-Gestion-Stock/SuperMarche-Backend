package application.supermarche.DTO.PackageVente;

public record VenteProduitDTO(

    Long id,
    String produitNom,
    Integer quantite,
    Double prixUnitaire
) {}
