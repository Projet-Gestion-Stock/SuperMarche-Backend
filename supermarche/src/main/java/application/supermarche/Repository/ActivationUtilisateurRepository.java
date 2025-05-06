package application.supermarche.Repository;

import application.supermarche.Entites.PackageUtilisateur.ActivationUtilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivationUtilisateurRepository extends JpaRepository<ActivationUtilisateur, Long> {
    List<ActivationUtilisateur> findByUtilisateurIdOrderByDateModificationDesc(Long utilisateurId);
}