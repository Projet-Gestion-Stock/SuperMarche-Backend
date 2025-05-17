package application.supermarche.Controllers;

import application.supermarche.DTO.SupermarcheInfo.HorairesUpdateDTO;
import application.supermarche.Entites.SupermarcheInfo.SupermarcheInfo;
import application.supermarche.Enumeration.JourSemaine;
import application.supermarche.Exceptions.ApiException;
import application.supermarche.Services.PackageCloudinary.CloudinaryService;
import application.supermarche.Services.SupermarcheInfo.SupermarcheInfoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import application.supermarche.Enumeration.ErrorCode;
import application.supermarche.Exceptions.ResourceNotFoundException;
import application.supermarche.Exceptions.BusinessException;

@Slf4j
@RestController
@RequestMapping("supermarche")
public class SupermarcheInfoController {

    private final SupermarcheInfoService service;
    private final CloudinaryService cloudinaryService;

    public SupermarcheInfoController(SupermarcheInfoService service,
                                     CloudinaryService cloudinaryService) {
        this.service = service;
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping("admin/ajouterLogo")
    public ResponseEntity<SupermarcheInfo> updateLogo(@RequestParam("file") MultipartFile file) {
        try {
            log.info("Tentative de mise à jour du logo");

            // Validation du fichier
            if (file.isEmpty()) {
                throw new BusinessException("Le fichier logo est vide", ErrorCode.EMPTY_FILE);
            }

            // 1. Supprimer l'ancien logo
            SupermarcheInfo currentInfo = service.getInfo();
            if (currentInfo.getLogoUrl() != null) {
                log.debug("Suppression de l'ancien logo: {}", currentInfo.getLogoUrl());
                cloudinaryService.deleteLogo(currentInfo.getLogoUrl());
            }

            // 2. Uploader le nouveau logo
            log.debug("Upload du nouveau logo");
            String newLogoUrl = cloudinaryService.uploadLogo(file);

            // 3. Mettre à jour le logo
            SupermarcheInfo updatedInfo = service.updateLogoOnly(newLogoUrl);
            log.info("Logo mis à jour avec succès");

            return ResponseEntity.ok(updatedInfo);

        } catch (BusinessException e) {
            log.warn("Erreur métier lors de la mise à jour du logo: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erreur technique lors de la mise à jour du logo: {}", e.getMessage());
            throw new ApiException("Erreur technique", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("admin/supprimerLogo")
    public ResponseEntity<Void> deleteLogo() {
        try {
            log.info("Tentative de suppression du logo");
            SupermarcheInfo info = service.getInfo();

            if (info.getLogoUrl() != null) {
                log.debug("Suppression du logo existant: {}", info.getLogoUrl());
                cloudinaryService.deleteLogo(info.getLogoUrl());
                info.setLogoUrl(null);
                service.updateInfo(info);
                log.info("Logo supprimé avec succès");
            } else {
                log.debug("Aucun logo à supprimer");
            }

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("Erreur technique lors de la suppression du logo: {}", e.getMessage());
            throw new ApiException("Erreur technique", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("staff/recupererInfo")
    public ResponseEntity<SupermarcheInfo> getInfo() {
        try {
            log.debug("Récupération des informations du supermarché");
            return ResponseEntity.ok(service.getInfo());
        } catch (ResourceNotFoundException e) {
            log.warn("Informations du supermarché non trouvées");
            throw e;
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des informations: {}", e.getMessage());
            throw new ApiException("Erreur technique", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("admin/modifierInfo")
    public ResponseEntity<SupermarcheInfo> updateInfo(@RequestBody SupermarcheInfo info) {
        try {
            log.info("Mise à jour des informations du supermarché");
            return ResponseEntity.ok(service.updateInfo(info));
        } catch (BusinessException e) {
            log.warn("Erreur de validation des informations: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erreur technique lors de la mise à jour: {}", e.getMessage());
            throw new ApiException("Erreur technique", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("staff/horaires")
    public ResponseEntity<Map<String, String>> getHoraires() {
        try {
            log.debug("Récupération des horaires");
            Map<JourSemaine, String> horaires = service.getInfo().getHorairesOuverture();

            Map<String, String> response = horaires.entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> e.getKey().name(),
                            Map.Entry::getValue
                    ));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des horaires: {}", e.getMessage());
            throw new ApiException("Erreur technique", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("admin/ajouterHoraires")
    public ResponseEntity<SupermarcheInfo> updateHoraires(@RequestBody HorairesUpdateDTO dto) {
        try {
            log.info("Mise à jour des horaires");
            return ResponseEntity.ok(service.updateHoraires(dto));
        } catch (BusinessException e) {
            log.warn("Erreur de validation des horaires: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erreur technique lors de la mise à jour des horaires: {}", e.getMessage());
            throw new ApiException("Erreur technique", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("staff/horaires/statut")
    public ResponseEntity<Map<String, Object>> getStatutOuverture() {
        try {
            log.debug("Vérification du statut d'ouverture");
            boolean estOuvert = service.estPendantLesHeuresOuvertes();
            JourSemaine jour = JourSemaine.fromDayOfWeek(java.time.LocalDate.now().getDayOfWeek());
            SupermarcheInfo info = service.getInfo();

            Map<String, Object> response = Map.of(
                    "estOuvert", estOuvert,
                    "jour", jour.name(),
                    "horaires", info.getHorairesOuverture().get(jour),
                    "prochaineOuverture", calculerProchaineOuverture(info)
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de la vérification du statut: {}", e.getMessage());
            throw new ApiException("Erreur technique", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String calculerProchaineOuverture(SupermarcheInfo info) {
        LocalDateTime maintenant = LocalDateTime.now();
        DayOfWeek jourActuel = maintenant.getDayOfWeek();
        LocalTime heureActuelle = maintenant.toLocalTime();

        // 1. Vérifier si le supermarché ouvre plus tard dans la même journée
        JourSemaine jourSemaineActuel = JourSemaine.fromDayOfWeek(jourActuel);
        String horairesAujourdhui = info.getHorairesOuverture().get(jourSemaineActuel);

        if (horairesAujourdhui != null) {
            String[] parties = horairesAujourdhui.split("-");
            LocalTime ouvertureAujourdhui = LocalTime.parse(parties[0]);
            LocalTime fermetureAujourdhui = LocalTime.parse(parties[1]);

            // Si c'est fermé maintenant mais qu'il rouvre plus tard aujourd'hui
            if (heureActuelle.isBefore(ouvertureAujourdhui)) {
                return formatProchaineOuverture("Aujourd'hui", ouvertureAujourdhui);
            }

            // Si c'est fermé après la fermeture aujourd'hui
            if (heureActuelle.isAfter(fermetureAujourdhui)) {
                // On passe au jour suivant
                return trouverProchaineOuverture(info, maintenant.plusDays(1), 6);
            }
        }

        // 2. Chercher dans les 6 prochains jours (7 jours max)
        return trouverProchaineOuverture(info, maintenant.plusDays(1), 6);
    }

    private String trouverProchaineOuverture(SupermarcheInfo info, LocalDateTime dateDebut, int joursARechercher) {
        for (int i = 0; i <= joursARechercher; i++) {
            LocalDateTime dateCourante = dateDebut.plusDays(i);
            JourSemaine jourSemaine = JourSemaine.fromDayOfWeek(dateCourante.getDayOfWeek());
            String horaires = info.getHorairesOuverture().get(jourSemaine);

            if (horaires != null && !horaires.isEmpty()) {
                String[] parties = horaires.split("-");
                LocalTime ouverture = LocalTime.parse(parties[0]);

                String prefixe = (i == 0) ? "Demain" : formatJourSemaine(jourSemaine);
                return formatProchaineOuverture(prefixe, ouverture);
            }
        }

        return "Aucune ouverture prévue dans les prochains jours";
    }

    private String formatProchaineOuverture(String prefixe, LocalTime heure) {
        return String.format("%s à %02d:%02d", prefixe, heure.getHour(), heure.getMinute());
    }

    private String formatJourSemaine(JourSemaine jour) {
        switch (jour) {
            case LUNDI: return "Lundi";
            case MARDI: return "Mardi";
            case MERCREDI: return "Mercredi";
            case JEUDI: return "Jeudi";
            case VENDREDI: return "Vendredi";
            case SAMEDI: return "Samedi";
            case DIMANCHE: return "Dimanche";
            default: return jour.name();
        }
    }
}