package application.supermarche.Controllers;

import application.supermarche.DTO.PackageVente.VenteRequestDTO;
import application.supermarche.DTO.PackageVente.VenteResponseDTO;
import application.supermarche.Entites.PackageUtilisateur.Utilisateur;
import application.supermarche.Entites.PackageVente.Vente;
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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("ventes")
public class VenteController {

    private final VenteService venteService;
    private final UtilisateurService utilisateurService;
    private final VenteMapper venteMapper;

    public VenteController(VenteService venteService, UtilisateurService utilisateurService, VenteMapper venteMapper) {
        this.venteService = venteService;
        this.utilisateurService = utilisateurService;
        this.venteMapper = venteMapper;
    }

    @PostMapping(path = "enregistrerVente", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<VenteResponseDTO> enregistrerVente(@RequestBody VenteRequestDTO venteRequest) {
        // Récupération de l'utilisateur connecté
        String emailUtilisateur = SecurityContextHolder.getContext().getAuthentication().getName();
        Utilisateur utilisateur = utilisateurService.findByEmail(emailUtilisateur);

        // Enregistrement de la vente
        Vente nouvelleVente = venteService.enregistrerVente(venteRequest, utilisateur);

        // Conversion en DTO
        VenteResponseDTO responseDto = venteMapper.toResponseDto(nouvelleVente);

        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }


  @GetMapping(path = "listerVentes", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<VenteResponseDTO>> listerVentes() {
      List<VenteResponseDTO> ventes = venteService.listerVentes();
      return ResponseEntity.ok(ventes);
  }

    @GetMapping(path = "recupererVente/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VenteResponseDTO> recupererVente(@PathVariable Long id) {
        // Récupérer les détails de la vente
        VenteResponseDTO venteResponse = venteService.recupererVente(id);
        return new ResponseEntity<>(venteResponse, HttpStatus.OK);
    }

    @GetMapping(path = "genererRecu/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> genererRecu(@PathVariable Long id) {
        // Générer un reçu pour la vente
        String recu = venteService.genererRecu(id);
        return new ResponseEntity<>(recu, HttpStatus.OK);
    }

    // generation du pdf

    @GetMapping(path = "genererRecuPDF/{id}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> genererRecuPDF(@PathVariable Long id) throws IOException {
        byte[] pdf = venteService.genererRecuPDF(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=recu_vente_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Erreur");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponse);
    }


}
