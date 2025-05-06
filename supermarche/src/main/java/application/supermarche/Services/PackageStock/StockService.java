package application.supermarche.Services.PackageStock;

import application.supermarche.Entites.PackageProduit.Produit;
import application.supermarche.Entites.PackageStock.Stock;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface StockService {
    Stock mettreAJourStock(Long produitId, int nouvelleQuantite);
    List<Stock> listerStocksFaibles();
    List<Stock> listerStocksEnRupture();
    Map<String, Object> statistiquesStock();

    // Ajout du seuilAlerte dans la méthode
    Stock initialiserStockPourProduit(Produit produit, int quantiteInitiale, int seuilAlerte);
}
