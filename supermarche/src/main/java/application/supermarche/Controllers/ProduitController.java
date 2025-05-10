package application.supermarche.Controllers;


import application.supermarche.DTO.PackageProduit.ProduitDTO;
import application.supermarche.Entites.PackageProduit.Produit;
import application.supermarche.Entites.PackageUtilisateur.Utilisateur;
import application.supermarche.Exceptions.ApiException;
import application.supermarche.Exceptions.BusinessException;
import application.supermarche.Exceptions.ResourceNotFoundException;
import application.supermarche.Mapper.ProduitMapper;
import application.supermarche.Mapper.UtilisateurMapper;
import application.supermarche.Services.PackageProduit.ProduitService;
import application.supermarche.Services.PackageStock.StockService;
import application.supermarche.Services.PackageUtilisateur.UtilisateurService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping("produits")
public class ProduitController {

    private final ProduitService produitService;
    private final StockService stockService;
    private final UtilisateurService utilisateurService;
    private final UtilisateurMapper utilisateurMapper;

    public ProduitController(ProduitService produitService,
                             StockService stockService,
                             UtilisateurService utilisateurService,
                             UtilisateurMapper utilisateurMapper) {
        this.produitService = produitService;
        this.stockService = stockService;
        this.utilisateurService = utilisateurService;
        this.utilisateurMapper = utilisateurMapper;
    }

    @PostMapping(path = "gerant/ajouterProduit", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<ProduitDTO> ajouterProduit(@Valid @RequestBody ProduitDTO produitDTO) {
        try {
            log.info("Tentative d'ajout d'un nouveau produit: {}", produitDTO.produit());

            String emailUtilisateur = SecurityContextHolder.getContext().getAuthentication().getName();
            Utilisateur utilisateur = utilisateurService.findByEmail(emailUtilisateur);

            Produit produit = ProduitMapper.toEntity(produitDTO, utilisateur);
            Produit savedProduit = produitService.ajouterProduit(produit);

            stockService.initialiserStockPourProduit(
                    savedProduit,
                    produitDTO.quantiteDisponible(),
                    produitDTO.seuilAlerte()
            );

            ProduitDTO responseDto = ProduitMapper.toDto(savedProduit, utilisateurMapper);
            log.info("Produit ajouté avec succès - ID: {}", savedProduit.getId());

            return new ResponseEntity<>(responseDto, HttpStatus.CREATED);

        } catch (BusinessException e) {
            log.warn("Erreur métier lors de l'ajout: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erreur technique lors de l'ajout: {}", e.getMessage());
            throw new ApiException("Erreur lors de l'ajout du produit", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(path = "staff/listerProduits", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProduitDTO>> listerProduitsActifs() {
        try {
            log.debug("Récupération de la liste des produits actifs");
            List<Produit> produits = produitService.listerProduitsActifs();
            List<ProduitDTO> dtos = produits.stream()
                    .map(p -> ProduitMapper.toDto(p, utilisateurMapper))
                    .toList();
            return ResponseEntity.ok(dtos);

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des produits: {}", e.getMessage());
            throw new ApiException("Erreur lors de la récupération des produits", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(path = "gerant/recupererProduit/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProduitDTO> recupererProduit(@PathVariable Long id) {
        try {
            log.debug("Récupération du produit ID: {}", id);
            Produit produit = produitService.recupererProduit(id);
            ProduitDTO produitDTO = ProduitMapper.toDto(produit, utilisateurMapper);
            return ResponseEntity.ok(produitDTO);

        } catch (ResourceNotFoundException e) {
            log.warn("Produit non trouvé - ID: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du produit: {}", e.getMessage());
            throw new ApiException("Erreur lors de la récupération du produit", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping(path = "gerant/modifierProduit/{id}", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<ProduitDTO> modifierProduit(
            @PathVariable Long id,
            @Valid @RequestBody ProduitDTO produitDTO) {
        try {
            log.info("Modification du produit ID: {}", id);
            Produit updatedProduit = produitService.modifierProduit(id, produitDTO);
            ProduitDTO responseDto = ProduitMapper.toDto(updatedProduit, utilisateurMapper);
            return ResponseEntity.ok(responseDto);

        } catch (ResourceNotFoundException e) {
            log.warn("Produit non trouvé pour modification - ID: {}", id);
            throw e;
        } catch (BusinessException e) {
            log.warn("Erreur métier lors de la modification: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erreur technique lors de la modification: {}", e.getMessage());
            throw new ApiException("Erreur lors de la modification du produit", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("gerant/desactiverProduit/{id}")
    public ResponseEntity<Void> desactiverProduit(@PathVariable Long id) {
        try {
            log.info("Désactivation du produit ID: {}", id);
            produitService.desactiverProduit(id);
            return ResponseEntity.noContent().build();

        } catch (ResourceNotFoundException e) {
            log.warn("Produit non trouvé pour désactivation - ID: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Erreur lors de la désactivation: {}", e.getMessage());
            throw new ApiException("Erreur lors de la désactivation du produit", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(path = "gerant/listerProduitsEnRupture", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProduitDTO>> listerProduitsEnRupture() {
        try {
            log.debug("Récupération des produits en rupture de stock");
            List<Produit> produits = produitService.listerProduitsEnRupture();
            List<ProduitDTO> dtos = produits.stream()
                    .map(p -> ProduitMapper.toDto(p, utilisateurMapper))
                    .toList();
            return ResponseEntity.ok(dtos);

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des produits en rupture: {}", e.getMessage());
            throw new ApiException("Erreur lors de la récupération des produits en rupture", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("gerant/avec-statut-expiration")
    public ResponseEntity<List<ProduitDTO>> getProduitsAvecStatutExpiration(
            @RequestParam(defaultValue = "21") int joursAlerte) {
        try {
            log.debug("Récupération des produits avec statut d'expiration - Seuil: {} jours", joursAlerte);
            List<ProduitDTO> dtos = produitService.getProduitsAvecStatutExpiration(joursAlerte);
            return ResponseEntity.ok(dtos);

        } catch (BusinessException e) {
            log.warn("Paramètre invalide pour l'alerte d'expiration: {}", joursAlerte);
            throw e;
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des produits expirés: {}", e.getMessage());
            throw new ApiException("Erreur lors de la récupération des produits expirés", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
