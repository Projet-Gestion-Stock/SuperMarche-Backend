package application.supermarche.Services.SupermarcheInfo;

import application.supermarche.DTO.SupermarcheInfo.HorairesUpdateDTO;
import application.supermarche.Entites.SupermarcheInfo.SupermarcheInfo;
import application.supermarche.Enumeration.JourSemaine;
import application.supermarche.Repository.SupermarcheInfoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
public class SupermarcheInfoService {

    private final SupermarcheInfoRepository repository;

    public SupermarcheInfoService(SupermarcheInfoRepository repository) {
        this.repository = repository;
    }

    public SupermarcheInfo getInfo() {
        return repository.findById(1L).orElseThrow(() -> new RuntimeException("Info manquante"));
    }

    @Transactional
    public SupermarcheInfo updateLogoOnly(String newLogoUrl) {
        SupermarcheInfo info = getInfo();
        info.setLogoUrl(newLogoUrl);
        return repository.save(info);
    }

    @Transactional
    public SupermarcheInfo updateInfo(SupermarcheInfo newInfo) {
        SupermarcheInfo existingInfo = getInfo();

        // Copie sélective des champs - NE PAS TOUCHER aux horaires
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

        existingInfo.setNom(newInfo.getNom());
        existingInfo.setLogoUrl(newInfo.getLogoUrl());
        existingInfo.setLocalisation(newInfo.getLocalisation());
        existingInfo.setTelephone(newInfo.getTelephone());

        if (newInfo.getHorairesOuverture() != null) {
            existingInfo.getHorairesOuverture().clear();
            existingInfo.getHorairesOuverture().putAll(newInfo.getHorairesOuverture());
        }

        return repository.save(existingInfo);
    }

    @Transactional
    public SupermarcheInfo updateHoraires(HorairesUpdateDTO dto) {
        if (!dto.isValid()) {
            throw new IllegalArgumentException("Format d'horaire invalide");
        }

        SupermarcheInfo info = getInfo();
        info.getHorairesOuverture().clear();
        info.getHorairesOuverture().putAll(dto.horaires());
        return repository.save(info);
    }

    public boolean estPendantLesHeuresOuvertes() {
        JourSemaine jour = JourSemaine.fromDayOfWeek(java.time.LocalDate.now().getDayOfWeek());
        SupermarcheInfo info = getInfo();

        LocalTime ouverture = info.getHeureOuverture(jour);
        LocalTime fermeture = info.getHeureFermeture(jour);

        if (ouverture == null || fermeture == null) {
            return false;
        }

        LocalTime maintenant = LocalTime.now();
        return !maintenant.isBefore(ouverture) && !maintenant.isAfter(fermeture);
    }

}
