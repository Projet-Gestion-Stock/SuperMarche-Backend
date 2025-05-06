package application.supermarche.Services.PackageUtilisateur;

import application.supermarche.Entites.PackageUtilisateur.ActivationUtilisateur;
import application.supermarche.Entites.PackageUtilisateur.Utilisateur;
import application.supermarche.Repository.ActivationUtilisateurRepository;
import application.supermarche.Repository.UtilisateurRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ActivationUtilisateurService {
    private final UtilisateurRepository utilisateurRepository;
    private final ActivationUtilisateurRepository activationUtilisateurRepository;

    @Transactional
    public void changerStatutActivation(Long utilisateurId, Utilisateur modifiePar, boolean nouveauStatut) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        boolean ancienStatut = utilisateur.isActif();

        if (ancienStatut != nouveauStatut) {
            utilisateur.setActif(nouveauStatut);
            utilisateurRepository.save(utilisateur);

            ActivationUtilisateur activation = new ActivationUtilisateur();
            activation.setUtilisateur(utilisateur);
            activation.setModifiePar(modifiePar);
            activation.setStatutAvant(ancienStatut ? "ACTIF" : "INACTIF");
            activation.setStatutApres(nouveauStatut ? "ACTIF" : "INACTIF");

            activationUtilisateurRepository.save(activation);
        }
    }
}