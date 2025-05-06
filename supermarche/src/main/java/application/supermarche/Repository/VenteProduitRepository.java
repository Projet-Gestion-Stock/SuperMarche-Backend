package application.supermarche.Repository;

import application.supermarche.Entites.PackageVente.VenteProduit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VenteProduitRepository extends JpaRepository<VenteProduit, Long> {
    List<VenteProduit> findByVenteId(Long id);
}
