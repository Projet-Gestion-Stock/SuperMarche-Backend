package application.supermarche.Controllers;


import application.supermarche.DTO.PackageProduit.ProduitDTO;
import application.supermarche.Entites.PackageProduit.Produit;
import application.supermarche.Entites.PackageStock.Stock;
import application.supermarche.Exceptions.RessourceNotFoundException;
import application.supermarche.Mapper.ProduitMapper;
import application.supermarche.Mapper.UtilisateurMapper;
import application.supermarche.Repository.ProduitRepository;
import application.supermarche.Services.PackageProduit.ProduitService;
import application.supermarche.Services.PackageStock.StockService;
import application.supermarche.Services.PackageUtilisateur.UtilisateurService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("produits")
public class ProduitController {


    private final ProduitRepository produitRepository;
    private final StockService stockService;
    private final UtilisateurService utilisateurService;
    private final ProduitService produitService;
    private final UtilisateurMapper utilisateurMapper;

    public ProduitController(ProduitRepository produitRepository, StockService stockService, UtilisateurService utilisateurService, ProduitService produitService, UtilisateurMapper utilisateurMapper) {
        this.produitRepository = produitRepository;
        this.stockService = stockService;
        this.utilisateurService = utilisateurService;
        this.produitService = produitService;
        this.utilisateurMapper = utilisateurMapper;
    }


    @PostMapping(path = "ajouterProduit", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<ProduitDTO> ajouterProduit(@RequestBody ProduitDTO produitDTO) {
        // Récupérer l'utilisateur connecté
        String emailUtilisateur = SecurityContextHolder.getContext().getAuthentication().getName();
        var utilisateur = utilisateurService.findByEmail(emailUtilisateur);

        // Mapper DTO vers entité et sauvegarder
        Produit produit = ProduitMapper.toEntity(produitDTO, utilisateur);
        Produit savedProduit = produitService.ajouterProduit(produit);

        // Initialiser le stock
        stockService.initialiserStockPourProduit(savedProduit, produitDTO.quantiteDisponible(), produitDTO.seuilAlerte());

        // Convertir en DTO pour la réponse
        ProduitDTO responseDto = ProduitMapper.toDto(savedProduit, utilisateurMapper);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    // Lister l'ensemble des produits

    @GetMapping(path = "listerProduits", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProduitDTO>> listerProduitsActifs() {
        List<Produit> produits = produitService.listerProduitsActifs();
        List<ProduitDTO> dtos = produits.stream()
                .map(p -> ProduitMapper.toDto(p, utilisateurMapper)) // Utilisation avec lambda
                .toList();
        return ResponseEntity.ok(dtos);
    }


   @GetMapping(path = "recupererProduit/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
   public ResponseEntity<ProduitDTO> recupererProduit(@PathVariable Long id) {
       Produit produit = produitService.recupererProduit(id);
       ProduitDTO produitDTO = ProduitMapper.toDto(produit, utilisateurMapper);
       return ResponseEntity.ok(produitDTO);
   }


    @PutMapping(path = "modifierProduit/{id}", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<ProduitDTO> modifierProduit(
            @PathVariable Long id,
            @RequestBody ProduitDTO produitDTO) {

        Produit updatedProduit = produitService.modifierProduit(id, produitDTO);
        ProduitDTO responseDto = ProduitMapper.toDto(updatedProduit, utilisateurMapper);

        return ResponseEntity.ok(responseDto);
    }

    // desactiver un produit au lieu de le supprimer

    @PostMapping("desactiverProduit/{id}")
    public ResponseEntity<Void> desactiverProduit(@PathVariable Long id) {
        produitService.desactiverProduit(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(path = "listerProduitsEnRupture", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProduitDTO>> listerProduitsEnRupture() {
        List<Produit> produits = produitService.listerProduitsEnRupture();
        List<ProduitDTO> dtos = produits.stream()
                .map(p -> ProduitMapper.toDto(p, utilisateurMapper))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    // Endpoint pour produits périmés
    @GetMapping("avec-statut-expiration")
    public ResponseEntity<List<ProduitDTO>> getProduitsAvecStatutExpiration(
            @RequestParam(defaultValue = "21") int joursAlerte) {

        List<ProduitDTO> dtos = produitService.getProduitsAvecStatutExpiration(joursAlerte);
        return ResponseEntity.ok(dtos);
    }

}
