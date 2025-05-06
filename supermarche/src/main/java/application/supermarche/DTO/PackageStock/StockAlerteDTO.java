package application.supermarche.DTO.PackageStock;

import application.supermarche.DTO.PackageProduit.ProduitStockDTO;

public record StockAlerteDTO(
        Long stockId,
        ProduitStockDTO produit,
        int quantiteActuelle,
        int seuilAlerte
) {}