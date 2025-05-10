package application.supermarche.Services.PackageStock;

import application.supermarche.Entites.PackageProduit.Produit;
import application.supermarche.Entites.PackageStock.Stock;
import application.supermarche.Enumeration.ErrorCode;
import application.supermarche.Exceptions.BusinessException;
import application.supermarche.Exceptions.ResourceNotFoundException;
import application.supermarche.Repository.ProduitRepository;
import application.supermarche.Repository.StockRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;

@Slf4j
@Service
public class StockServiceImplement implements StockService {

    private static final int SEUIL_ALERTE_DEFAUT = 10;
    private static final String STOCK_NOT_FOUND = "Stock non trouvé pour le produit ID: %d";
    private static final String PRODUCT_NOT_FOUND = "Produit non trouvé avec l'ID: %d";

    private final StockRepository stockRepository;
    private final ProduitRepository produitRepository;

    public StockServiceImplement(StockRepository stockRepository,
                                 ProduitRepository produitRepository) {
        this.stockRepository = stockRepository;
        this.produitRepository = produitRepository;
    }

    @Override
    @Transactional
    public Stock initialiserStockPourProduit(Produit produit, int quantiteInitiale, int seuilAlerte) {
        try {
            log.info("Initialisation stock pour produit ID: {}", produit.getId());

            if (produit.getStock() != null) {
                throw new BusinessException("Un stock existe déjà pour ce produit", ErrorCode.STOCK_ALREADY_EXISTS);
            }

            Stock stock = new Stock();
            stock.setProduit(produit);
            stock.setQuantite(quantiteInitiale);
            stock.setSeuilAlerte(seuilAlerte);
            produit.setStock(stock);

            Stock savedStock = stockRepository.save(stock);
            log.info("Stock initialisé avec ID: {}", savedStock.getId());

            return savedStock;

        } catch (DataIntegrityViolationException e) {
            log.error("Erreur base de données lors de l'initialisation du stock: {}", e.getMessage());
            throw new BusinessException("Erreur technique lors de la création du stock", ErrorCode.DATABASE_ERROR);
        }
    }

    @Override
    @Transactional
    public Stock mettreAJourStock(Long produitId, int quantiteAjoutee) {
        try {
            log.info("Mise à jour stock pour produit ID: {}, quantité: {}", produitId, quantiteAjoutee);

            Stock stock = stockRepository.findByProduitId(produitId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            String.format(STOCK_NOT_FOUND, produitId),
                            ErrorCode.STOCK_NOT_FOUND));

            validerQuantite(stock, quantiteAjoutee);
            mettreAJourQuantite(stock, quantiteAjoutee);
            verifierAlerteStock(stock, produitId);

            Stock updatedStock = stockRepository.save(stock);
            log.info("Stock mis à jour - Produit ID: {}, Nouvelle quantité: {}",
                    produitId, updatedStock.getQuantite());

            return updatedStock;

        } catch (ResourceNotFoundException e) {
            log.warn("Création d'un nouveau stock pour produit ID: {}", produitId);
            return creerNouveauStock(produitId, quantiteAjoutee);
        }
    }

    private Stock creerNouveauStock(Long produitId, int quantiteInitiale) {
        try {
            Produit produit = produitRepository.findById(produitId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            String.format(PRODUCT_NOT_FOUND, produitId),
                            ErrorCode.PRODUCT_NOT_FOUND));

            return initialiserStockPourProduit(produit, quantiteInitiale, SEUIL_ALERTE_DEFAUT);

        } catch (BusinessException e) {
            log.error("Erreur lors de la création du stock: {}", e.getMessage());
            throw e;
        }
    }

    private void validerQuantite(Stock stock, int quantiteAjoutee) {
        int nouvelleQuantite = stock.getQuantite() + quantiteAjoutee;
        if (nouvelleQuantite < 0) {
            String errorMsg = String.format(
                    "Opération invalide. Stock actuel: %d, Tentative d'ajout: %d. Le stock ne peut pas être négatif.",
                    stock.getQuantite(),
                    quantiteAjoutee
            );
            log.error(errorMsg);
            throw new BusinessException(errorMsg, ErrorCode.INVALID_STOCK_OPERATION);
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
        try {
            List<Stock> stocks = stockRepository.findByQuantiteLessThanEqual(SEUIL_ALERTE_DEFAUT);
            log.info("Récupération de {} stocks faibles", stocks.size());
            return stocks;
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des stocks faibles: {}", e.getMessage());
            throw new BusinessException("Erreur technique", ErrorCode.DATABASE_ERROR);
        }
    }

    @Override
    public List<Stock> listerStocksEnRupture() {
        try {
            List<Stock> stocks = stockRepository.findByQuantiteLessThanEqualAndProduitActifTrue(0);
            log.info("Récupération de {} stocks en rupture", stocks.size());
            return stocks;
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des stocks en rupture: {}", e.getMessage());
            throw new BusinessException("Erreur technique", ErrorCode.DATABASE_ERROR);
        }
    }

    @Override
    public Map<String, Object> statistiquesStock() {
        try {
            List<Stock> stocks = stockRepository.findAll();
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalProduitsEnStock", stocks.size());
            stats.put("stocksFaibles", listerStocksFaibles().size());
            stats.put("stocksEnRupture", listerStocksEnRupture().size());
            stats.put("quantiteTotale", stocks.stream().mapToInt(Stock::getQuantite).sum());

            log.info("Génération des statistiques de stock");
            return stats;

        } catch (Exception e) {
            log.error("Erreur lors du calcul des statistiques: {}", e.getMessage());
            throw new BusinessException("Erreur technique", ErrorCode.DATABASE_ERROR);
        }
    }
}
