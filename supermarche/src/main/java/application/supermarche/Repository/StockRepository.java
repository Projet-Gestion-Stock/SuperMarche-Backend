package application.supermarche.Repository;

import application.supermarche.Entites.PackageStock.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
   // Stock findByProduitId(Long produitId);
   @Query("SELECT s FROM Stock s WHERE s.quantite <= 0 AND s.produit.actif = true")
   List<Stock> findByQuantiteLessThanEqualAndProduitActifTrue(int quantite);

    @Query("SELECT s FROM Stock s WHERE s.quantite > 0 AND s.quantite <= :seuil")
    List<Stock> findStockFaibleStrict(@Param("seuil") int seuil);

    @Query("SELECT s FROM Stock s WHERE s.produit.actif = true")
    List<Stock> findAllStocksProduitsActifs();

    @Query("SELECT s FROM Stock s WHERE s.produit.actif = true AND s.quantite > 0 AND s.quantite <= :seuil")
    List<Stock> findStockFaibleStrictActif(@Param("seuil") int seuil);

    @Query("SELECT s FROM Stock s WHERE s.produit.actif = true AND s.quantite = 0")
    List<Stock> findStockEnRuptureActif();


    Optional<Stock> findByProduitId(Long produitId);
}
