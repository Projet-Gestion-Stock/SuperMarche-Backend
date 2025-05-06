package application.supermarche.Mapper;

import application.supermarche.DTO.PackageProduit.ProduitStockDTO;
import application.supermarche.DTO.PackageStock.StockAlerteDTO;
import application.supermarche.Entites.PackageProduit.Produit;
import application.supermarche.Entites.PackageStock.Stock;
import org.springframework.stereotype.Component;

@Component
public class StockMapper {

    public ProduitStockDTO toProduitStockDTO(Produit produit) {
        return new ProduitStockDTO(
                produit.getId(),
                produit.getProduit(), // nom du produit
                produit.getUnite(),
                produit.getCategorie()
        );
    }

    public StockAlerteDTO toStockAlerteDTO(Stock stock) {
        return new StockAlerteDTO(
                stock.getId(),
                toProduitStockDTO(stock.getProduit()),
                stock.getQuantite(),
                stock.getSeuilAlerte()
        );
    }
}