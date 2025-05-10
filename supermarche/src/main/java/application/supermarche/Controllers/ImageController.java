package application.supermarche.Controllers;

import application.supermarche.DTO.PackageCloudinary.ImageResponseDTO;
import application.supermarche.Entites.PackageProduit.Produit;
import application.supermarche.Enumeration.ErrorCode;
import application.supermarche.Exceptions.ApiException;
import application.supermarche.Exceptions.BusinessException;
import application.supermarche.Exceptions.ResourceNotFoundException;
import application.supermarche.Services.PackageCloudinary.CloudinaryService;
import application.supermarche.Services.PackageProduit.ProduitService;
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
    private final ProduitService produitService;

    public ImageController(CloudinaryService cloudinaryService,
                           ProduitService produitService) {
        this.cloudinaryService = cloudinaryService;
        this.produitService = produitService;
    }

    @PostMapping(value = "gerant/upload/{produitId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageResponseDTO> uploadImage(
            @PathVariable Long produitId,
            @RequestParam("file") MultipartFile file) {

        try {
            log.info("Tentative d'upload d'image pour produit ID: {}", produitId);

            // Vérification que le produit existe
            produitService.recupererProduit(produitId);

            String imageUrl = cloudinaryService.uploadProduitImage(file, produitId);
            log.info("Image uploadée avec succès - Produit ID: {}, URL: {}", produitId, imageUrl);

            return ResponseEntity.ok(
                    new ImageResponseDTO(imageUrl, "Image uploadée avec succès")
            );

        } catch (ResourceNotFoundException e) {
            log.warn("Produit non trouvé pour l'upload: {}", produitId);
            throw e;
        } catch (BusinessException e) {
            log.warn("Erreur de validation du fichier: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erreur lors de l'upload de l'image: {}", e.getMessage());
            throw new ApiException(
                    "Erreur lors de l'upload de l'image",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("gerant/supprimer/{produitId}")
    public ResponseEntity<ImageResponseDTO> deleteImage(@PathVariable Long produitId) {
        try {
            log.info("Tentative de suppression d'image pour produit ID: {}", produitId);

            Produit produit = produitService.recupererProduit(produitId);

            if (produit.getImageUrl() == null) {
                throw new BusinessException(
                        "Ce produit n'a pas d'image associée",
                        ErrorCode.NO_IMAGE_TO_DELETE);
            }

            cloudinaryService.deleteImage(produitId);
            log.info("Image supprimée avec succès - Produit ID: {}", produitId);

            return ResponseEntity.ok(
                    new ImageResponseDTO(null, "Image supprimée avec succès")
            );

        } catch (ResourceNotFoundException e) {
            log.warn("Produit non trouvé pour suppression image: {}", produitId);
            throw e;
        } catch (BusinessException e) {
            log.warn("Erreur métier lors de la suppression: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erreur technique lors de la suppression: {}", e.getMessage());
            throw new ApiException(
                    "Erreur lors de la suppression de l'image",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("gerant/info/{produitId}")
    public ResponseEntity<ImageResponseDTO> getImageUrl(@PathVariable Long produitId) {
        try {
            log.debug("Récupération d'URL d'image pour produit ID: {}", produitId);

            Produit produit = produitService.recupererProduit(produitId);

            if (produit.getImageUrl() == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ImageResponseDTO(null, "Aucune image disponible"));
            }

            String optimizedUrl = generateOptimizedImageUrl(produit.getImageUrl());
            return ResponseEntity.ok(
                    new ImageResponseDTO(optimizedUrl, "Image disponible")
            );

        } catch (ResourceNotFoundException e) {
            log.warn("Produit non trouvé pour récupération image: {}", produitId);
            throw e;
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'image: {}", e.getMessage());
            throw new ApiException(
                    "Erreur lors de la récupération de l'image",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String generateOptimizedImageUrl(String originalUrl) {
        try {
            if (originalUrl.contains("res.cloudinary.com")) {
                int uploadIndex = originalUrl.indexOf("/upload/") + 8;
                return originalUrl.substring(0, uploadIndex) +
                        "c_fill,w_400,h_400,q_auto,f_auto/" +
                        originalUrl.substring(uploadIndex);
            }
            return originalUrl;
        } catch (Exception e) {
            log.warn("Erreur lors de la génération de l'URL optimisée: {}", e.getMessage());
            return originalUrl;
        }
    }
}