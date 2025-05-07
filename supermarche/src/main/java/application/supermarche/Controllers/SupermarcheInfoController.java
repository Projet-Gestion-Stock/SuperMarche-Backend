package application.supermarche.Controllers;

import application.supermarche.DTO.SupermarcheInfo.HorairesUpdateDTO;
import application.supermarche.Entites.SupermarcheInfo.SupermarcheInfo;
import application.supermarche.Enumeration.JourSemaine;
import application.supermarche.Services.PackageCloudinary.CloudinaryService;
import application.supermarche.Services.SupermarcheInfo.SupermarcheInfoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("supermarche")
public class SupermarcheInfoController {

    private final SupermarcheInfoService service;
    private final CloudinaryService cloudinaryService;

    public SupermarcheInfoController(SupermarcheInfoService service, CloudinaryService cloudinaryService) {
        this.service = service;
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping("ajouterLogo")
    public ResponseEntity<SupermarcheInfo> updateLogo(@RequestParam("file") MultipartFile file) {
        try {
            // 1. Supprimer l'ancien logo s'il existe
            SupermarcheInfo currentInfo = service.getInfo();
            if (currentInfo.getLogoUrl() != null) {
                cloudinaryService.deleteLogo(currentInfo.getLogoUrl());
            }

            // 2. Uploader le nouveau logo
            String newLogoUrl = cloudinaryService.uploadLogo(file);

            // 3. Mettre à jour UNIQUEMENT le logo
            SupermarcheInfo updatedInfo = service.updateLogoOnly(newLogoUrl);

            return ResponseEntity.ok(updatedInfo);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    @DeleteMapping("supprimerLogo")
    public ResponseEntity<Void> deleteLogo() {
        try {
            SupermarcheInfo info = service.getInfo();
            if (info.getLogoUrl() != null) {
                cloudinaryService.deleteLogo(info.getLogoUrl());
                info.setLogoUrl(null);
                service.updateInfo(info);
            }
            return ResponseEntity.noContent().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("recupererInfo")
    public SupermarcheInfo getInfo() {
        return service.getInfo();
    }

    @PutMapping("modifierInfo")
    public SupermarcheInfo updateInfo(@RequestBody SupermarcheInfo info) {
        return service.updateInfo(info);
    }

    // Voir les horaires d'ouverture et de fermeture

    @GetMapping("horaires")
    public Map<String, String> getHoraires() {
        return service.getInfo().getHorairesOuverture().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().name(),
                        Map.Entry::getValue
                ));
    }

    @PostMapping("ajouterHoraires")
    public ResponseEntity<SupermarcheInfo> updateHoraires(@RequestBody HorairesUpdateDTO dto) {
        return ResponseEntity.ok(service.updateHoraires(dto));
    }

    @GetMapping("horaires/statut")
    public Map<String, Object> getStatutOuverture() {
        boolean estOuvert = service.estPendantLesHeuresOuvertes();
        JourSemaine jour = JourSemaine.fromDayOfWeek(java.time.LocalDate.now().getDayOfWeek());
        SupermarcheInfo info = service.getInfo();

        return Map.of(
                "estOuvert", estOuvert,
                "jour", jour.name(),
                "horaires", info.getHorairesOuverture().get(jour),
                "prochaineOuverture", calculerProchaineOuverture(info)
        );
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