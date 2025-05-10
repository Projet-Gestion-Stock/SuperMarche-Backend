package application.supermarche.Repository;

import application.supermarche.Entites.PackageProduit.Produit;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProduitRepository extends JpaRepository<Produit, Long> {

    @Query("SELECT p FROM Produit p WHERE p.actif = true")
    List<Produit> findByActifTrue();

    @Query("SELECT p FROM Produit p WHERE p.id = ?1 AND p.actif = true")
    Optional<Produit> findByIdAndActifTrue(Long id);

   @EntityGraph(attributePaths = {"stock", "utilisateur"})
   @Query("SELECT p FROM Produit p WHERE p.actif = true AND p.stock.quantite <= 0")
   List<Produit> findProduitsEnRupture();

    // Utilisez @Query pour une requête personnalisée
    @Query("SELECT p FROM Produit p LEFT JOIN FETCH p.stock WHERE p.actif = true")
    List<Produit> findAllWithStock();

    // Ajoutez aussi cette méthode pour la tâche planifiée
    @Query("SELECT p FROM Produit p WHERE p.dateExpiration < :date AND p.actif = true")
    List<Produit> findByDateExpirationBeforeAndActifTrue(@Param("date") LocalDate date);

    boolean existsByProduitAndFournisseur(String produit, String fournisseur);

}