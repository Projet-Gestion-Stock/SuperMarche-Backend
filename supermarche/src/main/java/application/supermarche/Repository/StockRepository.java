package application.supermarche.Repository;

import application.supermarche.Entites.PackageStock.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
   // Stock findByProduitId(Long produitId);
   @Query("SELECT s FROM Stock s WHERE s.quantite <= 0 AND s.produit.actif = true")
   List<Stock> findByQuantiteLessThanEqualAndProduitActifTrue(int quantite);
    List<Stock> findByQuantiteLessThanEqual(int seuil);
    Optional<Stock> findByProduitId(Long produitId);
}
