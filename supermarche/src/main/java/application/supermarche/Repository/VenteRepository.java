package application.supermarche.Repository;

import application.supermarche.Entites.PackageVente.Vente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VenteRepository extends JpaRepository<Vente, Long> {
    boolean existsByNumeroRecu(String numero);
    List<Vente> findAllByOrderByDateVenteDesc();

}
