package application.supermarche.Services.PackageUtilisateur;

import application.supermarche.Entites.PackageUtilisateur.ActivationUtilisateur;
import application.supermarche.Entites.PackageUtilisateur.Utilisateur;
import application.supermarche.Enumeration.ErrorCode;
import application.supermarche.Exceptions.BusinessException;
import application.supermarche.Repository.ActivationUtilisateurRepository;
import application.supermarche.Repository.UtilisateurRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;


import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivationUtilisateurService {
    private final UtilisateurRepository utilisateurRepository;
    private final ActivationUtilisateurRepository activationUtilisateurRepository;

    // Activer ou desactiver personnel

    @Transactional
    public void changerStatutActivation(Long utilisateurId, Utilisateur modifiePar, boolean nouveauStatut) {
        try {
            log.info("Tentative de changement de statut pour l'utilisateur ID: {} par: {}", utilisateurId, modifiePar.getEmail());

            Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                    .orElseThrow(() -> {
                        log.warn("Utilisateur non trouvé - ID: {}", utilisateurId);
                        return new BusinessException("Utilisateur introuvable", ErrorCode.USER_NOT_FOUND);
                    });

            if (utilisateur.equals(modifiePar)) {
                log.warn("Tentative d'auto-modification du statut par l'utilisateur: {}", modifiePar.getEmail());
                throw new BusinessException("Auto-modification non autorisée", ErrorCode.SELF_MODIFICATION_NOT_ALLOWED);
            }

            boolean ancienStatut = utilisateur.isActif();

            if (ancienStatut != nouveauStatut) {
                utilisateur.setActif(nouveauStatut);
                utilisateurRepository.save(utilisateur);

                ActivationUtilisateur activation = ActivationUtilisateur.builder()
                        .utilisateur(utilisateur)
                        .modifiePar(modifiePar)
                        .statutAvant(ancienStatut ? "ACTIF" : "INACTIF")
                        .statutApres(nouveauStatut ? "ACTIF" : "INACTIF")
                        .dateModification(LocalDateTime.now())
                        .build();

                activationUtilisateurRepository.save(activation);
                log.info("Statut modifié avec succès pour l'utilisateur ID: {}. Nouveau statut: {}", utilisateurId, nouveauStatut);
            } else {
                log.debug("Aucun changement de statut nécessaire pour l'utilisateur ID: {}", utilisateurId);
            }

        } catch (DataIntegrityViolationException e) {
            log.error("Erreur d'intégrité des données lors de la modification du statut: {}", e.getMessage());
            throw new BusinessException("Erreur de persistance des données", ErrorCode.DATABASE_ERROR);
        }
    }
}