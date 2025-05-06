package application.supermarche.Repository;

import application.supermarche.Entites.PackageUtilisateur.HistoriqueConnexion;
import org.springframework.data.repository.CrudRepository;

public interface HistoriqueConnexionRepository extends CrudRepository<HistoriqueConnexion, Integer> {
}
