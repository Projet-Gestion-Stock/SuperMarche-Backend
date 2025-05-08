package application.supermarche.Controllers;

import application.supermarche.DTO.PackageCloudinary.ImageResponseDTO;
import application.supermarche.Entites.PackageProduit.Produit;
import application.supermarche.Exceptions.ProduitNotFoundException;
import application.supermarche.Repository.ProduitRepository;
import application.supermarche.Services.PackageCloudinary.CloudinaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("images")
public class ImageController {

    private final CloudinaryService cloudinaryService;
    private final ProduitRepository produitRepository;

    public ImageController(CloudinaryService cloudinaryService, ProduitRepository produitRepository) {
        this.cloudinaryService = cloudinaryService;
        this.produitRepository = produitRepository;
    }

    @PostMapping(value = "gerant/upload/{produitId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageResponseDTO> uploadImage(
            @PathVariable Long produitId,
            @RequestParam("file") MultipartFile file) {

        String imageUrl = cloudinaryService.uploadProduitImage(file, produitId);
        return ResponseEntity.ok(
                new ImageResponseDTO(imageUrl, "Image ajoutée")
        );
    }

    @DeleteMapping("gerant/supprimer/{produitId}")
    public ResponseEntity<ImageResponseDTO> deleteImage(@PathVariable Long produitId) {
        try {
            // 1. Vérifier si le produit existe
            if (!produitRepository.existsById(produitId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ImageResponseDTO(
                                null,
                                "Suppression impossible : produit non trouvé (ID: " + produitId + ")"
                        ));
            }

            // 2. Vérifier si le produit a une image
            Produit produit = produitRepository.findById(produitId).orElse(null);
            if (produit != null && produit.getImageUrl() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ImageResponseDTO(
                                null,
                                "Suppression impossible : ce produit n'a pas d'image associée (ID: " + produitId + ")"
                        ));
            }

            // 3. Supprimer l'image
            cloudinaryService.deleteImage(produitId);

            // 4. Retourner une réponse détaillée
            return ResponseEntity.ok()
                    .body(new ImageResponseDTO(
                            null,
                            "Image supprimée avec succès pour le produit ID: " + produitId
                    ));

        } catch (Exception e) {
            log.error("Erreur lors de la suppression de l'image pour le produit ID: " + produitId, e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ImageResponseDTO(
                            null,
                            "Erreur lors de la suppression : " + e.getMessage() + " (Produit ID: " + produitId + ")"
                    ));
        }
    }

    @GetMapping("gerant/info/{produitId}")
    public ResponseEntity<ImageResponseDTO> getImageUrl(@PathVariable Long produitId) {
        try {
            Produit produit = produitRepository.findById(produitId)
                    .orElseThrow(() -> new ProduitNotFoundException(produitId));

            if (produit.getImageUrl() == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ImageResponseDTO(null, "Aucune image disponible"));
            }

            // Vous pouvez aussi générer une URL transformée si besoin
            String transformedUrl = generateOptimizedImageUrl(produit.getImageUrl());

            return ResponseEntity.ok(
                    new ImageResponseDTO(transformedUrl, "Image trouvée"));

        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'image", e);
            return ResponseEntity.internalServerError()
                    .body(new ImageResponseDTO(null, "Erreur serveur"));
        }
    }

    private String generateOptimizedImageUrl(String originalUrl) {
        // Exemple: Ajouter des paramètres d'optimisation Cloudinary
        if (originalUrl.contains("res.cloudinary.com")) {
            int uploadIndex = originalUrl.indexOf("/upload/") + 8;
            return originalUrl.substring(0, uploadIndex) +
                    "c_fill,w_400,h_400,q_auto/" +
                    originalUrl.substring(uploadIndex);
        }
        return originalUrl;
    }

}