package application.supermarche.DTO.PackageProduit;

public record ProduitStockDTO(
        Long id,
        String produit,
        String unite,
        String categorie
) {}