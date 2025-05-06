package application.supermarche.DTO.PackageStock;

import application.supermarche.DTO.PackageProduit.ProduitStockDTO;

public record StockResponseDTO(
        Long id,
        ProduitStockDTO produit,
        int quantiteTotale,
        int seuilAlerte
) {}