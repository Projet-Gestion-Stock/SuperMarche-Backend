package application.supermarche.Controllers;

import application.supermarche.DTO.PackageVente.VenteRequestDTO;
import application.supermarche.DTO.PackageVente.VenteResponseDTO;
import application.supermarche.Entites.PackageUtilisateur.Utilisateur;
import application.supermarche.Entites.PackageVente.Vente;
import application.supermarche.Exceptions.ApiException;
import application.supermarche.Exceptions.ResourceNotFoundException;
import application.supermarche.Mapper.VenteMapper;
import application.supermarche.Services.PackageVente.VenteService;
import application.supermarche.Services.PackageUtilisateur.UtilisateurService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import application.supermarche.Exceptions.BusinessException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("ventes")
public class VenteController {

    private final VenteService venteService;
    private final UtilisateurService utilisateurService;
    private final VenteMapper venteMapper;

    public VenteController(VenteService venteService,
                           UtilisateurService utilisateurService,
                           VenteMapper venteMapper) {
        this.venteService = venteService;
        this.utilisateurService = utilisateurService;
        this.venteMapper = venteMapper;
    }

    @PostMapping(path = "staff/enregistrerVente", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<VenteResponseDTO> enregistrerVente(@RequestBody VenteRequestDTO venteRequest) {
        try {
            log.info("Tentative d'enregistrement d'une nouvelle vente");

            String emailUtilisateur = SecurityContextHolder.getContext().getAuthentication().getName();
            Utilisateur utilisateur = utilisateurService.findByEmail(emailUtilisateur);

            Vente nouvelleVente = venteService.enregistrerVente(venteRequest, utilisateur);
            VenteResponseDTO responseDto = venteMapper.toResponseDto(nouvelleVente);

            log.info("Vente enregistrée avec succès - ID: {}", nouvelleVente.getId());
            return new ResponseEntity<>(responseDto, HttpStatus.CREATED);

        } catch (BusinessException e) {
            log.warn("Erreur métier lors de l'enregistrement: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erreur technique lors de l'enregistrement: {}", e.getMessage());
            throw new ApiException("Erreur lors de l'enregistrement de la vente", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // liste des ventes

    @GetMapping(path = "staff/listerVentes", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<VenteResponseDTO>> listerVentes() {
        try {
            log.debug("Récupération de la liste des ventes");
            List<VenteResponseDTO> ventes = venteService.listerVentes();
            return ResponseEntity.ok(ventes);

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des ventes: {}", e.getMessage());
            throw new ApiException("Erreur lors de la récupération des ventes", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Information sur une vente spécifique

    @GetMapping(path = "staff/recupererVente/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VenteResponseDTO> recupererVente(@PathVariable Long id) {
        try {
            log.debug("Récupération de la vente ID: {}", id);
            VenteResponseDTO venteResponse = venteService.recupererVente(id);
            return ResponseEntity.ok(venteResponse);

        } catch (ResourceNotFoundException e) {
            log.warn("Vente non trouvée - ID: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la vente: {}", e.getMessage());
            throw new ApiException("Erreur lors de la récupération de la vente", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // generer le recu d'une vente

    @GetMapping(path = "staff/genererRecu/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> genererRecu(@PathVariable Long id) {
        try {
            log.info("Génération du reçu pour la vente ID: {}", id);
            String recu = venteService.genererRecu(id);
            return ResponseEntity.ok(recu);

        } catch (ResourceNotFoundException e) {
            log.warn("Vente non trouvée pour génération reçu - ID: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Erreur lors de la génération du reçu: {}", e.getMessage());
            throw new ApiException("Erreur lors de la génération du reçu", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // generer le recu d'une vente version pdf

    @GetMapping(path = "staff/genererRecuPDF/{id}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> genererRecuPDF(@PathVariable Long id) {
        try {
            log.info("Génération du PDF pour la vente ID: {}", id);
            byte[] pdf = venteService.genererRecuPDF(id);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=recu_vente_" + id + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);

        } catch (ResourceNotFoundException e) {
            log.warn("Vente non trouvée pour génération PDF - ID: {}", id);
            throw e;
        } catch (IOException e) {
            log.error("Erreur IO lors de la génération PDF: {}", e.getMessage());
            throw new ApiException("Erreur lors de la génération du PDF", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Erreur technique lors de la génération PDF: {}", e.getMessage());
            throw new ApiException("Erreur technique lors de la génération du PDF", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
