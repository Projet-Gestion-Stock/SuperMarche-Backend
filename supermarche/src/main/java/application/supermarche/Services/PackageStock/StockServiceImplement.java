package application.supermarche.Services.PackageStock;

import application.supermarche.Entites.PackageProduit.Produit;
import application.supermarche.Entites.PackageStock.Stock;
import application.supermarche.Exceptions.BusinessException;
import application.supermarche.Exceptions.RessourceNotFoundException;
import application.supermarche.Repository.ProduitRepository;
import application.supermarche.Repository.StockRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class StockServiceImplement implements StockService {

    private static final int SEUIL_ALERTE_DEFAUT = 10;

    private final StockRepository stockRepository;
    private final ProduitRepository produitRepository;

    public StockServiceImplement(StockRepository stockRepository, ProduitRepository produitRepository) {
        this.stockRepository = stockRepository;
        this.produitRepository = produitRepository;
    }

    @Override
    public Stock initialiserStockPourProduit(Produit produit, int quantiteInitiale, int seuilAlerte) {
        Stock stock = new Stock();
        stock.setProduit(produit);
        stock.setQuantite(quantiteInitiale);
        stock.setSeuilAlerte(seuilAlerte);
        produit.setStock(stock);
        return stockRepository.save(stock);
    }

    @Override
    @Transactional
    public Stock mettreAJourStock(Long produitId, int quantiteAjoutee) {
        Stock stock = stockRepository.findByProduitId(produitId)
                .orElseThrow(() -> new EntityNotFoundException("Stock non trouvé pour le produit ID: " + produitId));

        if (stock == null) {
            stock = creerNouveauStock(produitId, quantiteAjoutee);
        } else {
            validerQuantite(stock, quantiteAjoutee);
            mettreAJourQuantite(stock, quantiteAjoutee);
            verifierAlerteStock(stock, produitId);
        }

        return stockRepository.save(stock);
    }

    private Stock creerNouveauStock(Long produitId, int quantiteInitiale) {
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new RessourceNotFoundException("Produit non trouvé avec l'id: " + produitId));
        return initialiserStockPourProduit(produit, quantiteInitiale, SEUIL_ALERTE_DEFAUT);
    }

    private void validerQuantite(Stock stock, int quantiteAjoutee) {
        int nouvelleQuantite = stock.getQuantite() + quantiteAjoutee;
        if (nouvelleQuantite < 0) {
            throw new BusinessException(
                    String.format("Opération invalide. Stock actuel: %d, Tentative d'ajout: %d. Le stock ne peut pas être négatif.",
                            stock.getQuantite(),
                            quantiteAjoutee)
            );
        }
    }

    private void mettreAJourQuantite(Stock stock, int quantiteAjoutee) {
        stock.setQuantite(stock.getQuantite() + quantiteAjoutee);
    }

    private void verifierAlerteStock(Stock stock, Long produitId) {
        if (stock.getQuantite() < stock.getSeuilAlerte()) {
            log.warn("ALERTE STOCK - Produit: {} (ID: {}). Quantité: {} (Seuil: {})",
                    stock.getProduit().getProduit(),
                    produitId,
                    stock.getQuantite(),
                    stock.getSeuilAlerte());
        }
    }

    @Override
    public List<Stock> listerStocksFaibles() {
        return stockRepository.findByQuantiteLessThanEqual(SEUIL_ALERTE_DEFAUT);
    }

    @Override
    public List<Stock> listerStocksEnRupture() {
        return stockRepository.findByQuantiteLessThanEqualAndProduitActifTrue(0);
    }

    @Override
    public Map<String, Object> statistiquesStock() {
        List<Stock> stocks = stockRepository.findAll();
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProduitsEnStock", stocks.size());
        stats.put("stocksFaibles", listerStocksFaibles().size());
        stats.put("stocksEnRupture", listerStocksEnRupture().size());
        stats.put("quantiteTotale", stocks.stream().mapToInt(Stock::getQuantite).sum());
        return stats;
    }
}
