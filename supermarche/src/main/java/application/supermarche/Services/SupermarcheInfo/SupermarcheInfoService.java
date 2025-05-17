package application.supermarche.Services.SupermarcheInfo;

import application.supermarche.DTO.SupermarcheInfo.HorairesUpdateDTO;
import application.supermarche.Entites.SupermarcheInfo.SupermarcheInfo;
import application.supermarche.Enumeration.JourSemaine;
import application.supermarche.Exceptions.BusinessException;
import application.supermarche.Exceptions.ResourceNotFoundException;
import application.supermarche.Repository.SupermarcheInfoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

import lombok.extern.slf4j.Slf4j;
import application.supermarche.Enumeration.ErrorCode;

@Slf4j
@Service
public class SupermarcheInfoService {

    private static final Long DEFAULT_INFO_ID = 1L;
    private final SupermarcheInfoRepository repository;

    public SupermarcheInfoService(SupermarcheInfoRepository repository) {
        this.repository = repository;
    }

    public SupermarcheInfo getInfo() {
        return repository.findById(DEFAULT_INFO_ID)
                .orElseThrow(() -> {
                    log.error("Informations du supermarché non configurées");
                    throw new ResourceNotFoundException(
                            "Configuration du supermarché manquante",
                            ErrorCode.SUPERMARKET_INFO_MISSING);
                });
    }

    @Transactional
    public SupermarcheInfo updateLogoOnly(String newLogoUrl) {
        try {
            log.info("Mise à jour du logo seulement");
            validateLogoUrl(newLogoUrl);

            SupermarcheInfo info = getInfo();
            info.setLogoUrl(newLogoUrl);

            SupermarcheInfo updated = repository.save(info);
            log.debug("Logo mis à jour vers: {}", newLogoUrl);

            return updated;
        } catch (BusinessException e) {
            log.warn("Erreur de validation du logo: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erreur technique lors de la mise à jour du logo: {}", e.getMessage());
            throw new BusinessException(
                    "Erreur lors de la mise à jour du logo",
                    ErrorCode.LOGO_UPDATE_ERROR);
        }
    }

    @Transactional
    public SupermarcheInfo updateInfo(SupermarcheInfo newInfo) {
        try {
            log.info("Mise à jour des informations du supermarché");
            validateSupermarketInfo(newInfo);

            SupermarcheInfo existingInfo = getInfo();

            // Copie sélective des champs
            if (newInfo.getNom() != null) {
                existingInfo.setNom(newInfo.getNom());
            }
            if (newInfo.getLogoUrl() != null) {
                existingInfo.setLogoUrl(newInfo.getLogoUrl());
            }
            if (newInfo.getLocalisation() != null) {
                existingInfo.setLocalisation(newInfo.getLocalisation());
            }
            if (newInfo.getTelephone() != null) {
                existingInfo.setTelephone(newInfo.getTelephone());
            }
            if (newInfo.getEmail() != null) {
                existingInfo.setEmail(newInfo.getEmail());
            }
            if (newInfo.getDescription() != null) {
                existingInfo.setDescription(newInfo.getDescription());
            }

            log.debug("Informations mises à jour: nom={}, logo={}, localisation={}, telephone={}, email={}, description={}",
                    newInfo.getNom(),
                    newInfo.getLogoUrl(),
                    newInfo.getLocalisation(),
                    newInfo.getTelephone(),
                    newInfo.getEmail(),
                    newInfo.getDescription()
            );


            return repository.save(existingInfo);
        } catch (BusinessException e) {
            log.warn("Erreur de validation des informations: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erreur technique lors de la mise à jour des informations: {}", e.getMessage());
            throw new BusinessException(
                    "Erreur lors de la mise à jour des informations",
                    ErrorCode.INFO_UPDATE_ERROR);
        }
    }

    @Transactional
    public SupermarcheInfo updateHoraires(HorairesUpdateDTO dto) {
        try {
            log.info("Mise à jour des horaires");
            if (!dto.isValid()) {
                throw new BusinessException(
                        "Format d'horaire invalide",
                        ErrorCode.INVALID_OPENING_HOURS);
            }

            SupermarcheInfo info = getInfo();
            info.getHorairesOuverture().clear();
            info.getHorairesOuverture().putAll(dto.horaires());

            SupermarcheInfo updated = repository.save(info);
            log.debug("Horaires mis à jour: {}", dto.horaires());

            return updated;
        } catch (BusinessException e) {
            log.warn("Erreur de validation des horaires: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erreur technique lors de la mise à jour des horaires: {}", e.getMessage());
            throw new BusinessException(
                    "Erreur lors de la mise à jour des horaires",
                    ErrorCode.OPENING_HOURS_UPDATE_ERROR);
        }
    }

    public boolean estPendantLesHeuresOuvertes() {
        try {
            JourSemaine jour = JourSemaine.fromDayOfWeek(java.time.LocalDate.now().getDayOfWeek());
            SupermarcheInfo info = getInfo();

            LocalTime ouverture = info.getHeureOuverture(jour);
            LocalTime fermeture = info.getHeureFermeture(jour);

            if (ouverture == null || fermeture == null) {
                log.warn("Pas d'horaires configurés pour {}", jour);
                return false;
            }

            LocalTime maintenant = LocalTime.now();
            boolean estOuvert = !maintenant.isBefore(ouverture) && !maintenant.isAfter(fermeture);

            log.debug("Vérification horaires: {}h - {}h (actuel: {}h) => {}",
                    ouverture, fermeture, maintenant, estOuvert ? "OUVERT" : "FERME");

            return estOuvert;
        } catch (Exception e) {
            log.error("Erreur lors de la vérification des horaires: {}", e.getMessage());
            return false; // Fail-safe
        }
    }

    // Méthodes de validation privées
    private void validateLogoUrl(String logoUrl) {
        if (logoUrl == null || logoUrl.isBlank()) {
            throw new BusinessException(
                    "L'URL du logo ne peut être vide",
                    ErrorCode.INVALID_LOGO_URL);
        }
        // Ajouter d'autres validations si nécessaire (format URL, etc.)
    }

    private void validateSupermarketInfo(SupermarcheInfo info) {
        if (info.getNom() != null && info.getNom().isBlank()) {
            throw new BusinessException(
                    "Le nom ne peut être vide",
                    ErrorCode.INVALID_NAME);
        }
        // Ajouter d'autres validations selon les besoins
    }
}
