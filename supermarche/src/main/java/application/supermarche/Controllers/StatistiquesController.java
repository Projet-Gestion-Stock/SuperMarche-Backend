package application.supermarche.Controllers;


import application.supermarche.Exceptions.ApiException;
import application.supermarche.Enumeration.ErrorCode;
import application.supermarche.Exceptions.BusinessException;
import application.supermarche.Services.PackageStatistique.StatistiquesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("statistiques")
public class StatistiquesController {

    private final StatistiquesService statistiquesService;

    public StatistiquesController(StatistiquesService statistiquesService) {
        this.statistiquesService = statistiquesService;
    }

    @GetMapping(path = "gerant/ventes", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> statistiquesVentes() {
        try {
            log.info("Demande des statistiques de ventes");
            Map<String, Object> stats = statistiquesService.statistiquesVentes();
            log.debug("Statistiques de ventes récupérées avec succès");
            return ResponseEntity.ok(stats);

        } catch (BusinessException e) {
            log.warn("Erreur métier dans statistiquesVentes: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erreur technique dans statistiquesVentes: {}", e.getMessage());
            throw new ApiException(
                    "Erreur lors du calcul des statistiques de ventes",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(path = "gerant/produits", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> produitsPlusVendus(
            @RequestParam(required = false, defaultValue = "15") int limit) {
        try {
            log.info("Demande des produits plus vendus avec limite: {}", limit);

            if (limit <= 0 || limit > 20) {
                throw new BusinessException(
                        "La limite doit être entre 1 et 20",
                        ErrorCode.INVALID_PARAMETER);
            }

            Map<String, Object> stats = statistiquesService.produitsPlusVendus(limit);
            log.debug("Top {} produits vendus récupérés avec succès", limit);
            return ResponseEntity.ok(stats);

        } catch (BusinessException e) {
            log.warn("Erreur métier dans produitsPlusVendus: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erreur technique dans produitsPlusVendus: {}", e.getMessage());
            throw new ApiException(
                    "Erreur lors du calcul des produits plus vendus",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}