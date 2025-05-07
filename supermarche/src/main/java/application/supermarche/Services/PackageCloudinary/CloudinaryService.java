package application.supermarche.Services.PackageCloudinary;

import application.supermarche.Entites.PackageProduit.Produit;
import application.supermarche.Exceptions.ProduitNotFoundException;
import application.supermarche.Repository.ProduitRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;
    private final ProduitRepository produitRepository;

    public CloudinaryService(Cloudinary cloudinary, ProduitRepository produitRepository) {
        this.cloudinary = cloudinary;
        this.produitRepository = produitRepository;
    }

    // Liste blanche des formats acceptés
    private final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp");

    public String uploadProduitImage(MultipartFile file, Long produitId) {
        try {
            // Validation du fichier
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Le fichier image est vide");
            }

            if (!file.getContentType().startsWith("image/")) {
                throw new IllegalArgumentException("Seules les images sont autorisées");
            }

            if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
                throw new IllegalArgumentException("Format d'image non supporté");
            }

            // Récupération du produit
            Produit produit = produitRepository.findById(produitId)
                    .orElseThrow(() -> new ProduitNotFoundException(produitId));

            // Configuration de l'upload
            Map<String, Object> options = new HashMap<>();
            options.put("folder", "supermarche/produits");
            options.put("public_id", "produit_" + produitId);
            options.put("overwrite", true);
            options.put("resource_type", "image");
            options.put("quality", "auto:best");
            options.put("width", "800");
            options.put("height", "800");
            options.put("crop", "limit");

            // Upload vers Cloudinary
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
            String imageUrl = (String) uploadResult.get("secure_url");

            // Mise à jour du produit
            produit.setImageUrl(imageUrl);
            produitRepository.save(produit);

            log.info("Image uploadée avec succès pour le produit ID: {}", produitId);
            return imageUrl;

        } catch (IOException e) {
            log.error("Erreur lors de l'upload de l'image", e);
            throw new RuntimeException("Erreur lors de l'upload de l'image", e);
        }
    }

    public void deleteImage(Long produitId) {
        try {
            Produit produit = produitRepository.findById(produitId)
                    .orElseThrow(() -> new ProduitNotFoundException(produitId));

            if (produit.getImageUrl() == null) {
                return;
            }

            // Extraction du public_id à partir de l'URL
            String publicId = extractPublicIdFromUrl(produit.getImageUrl());

            // Suppression de l'image
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());

            // Mise à jour du produit
            produit.setImageUrl(null);
            produitRepository.save(produit);

            log.info("Image supprimée avec succès pour le produit ID: {}", produitId);

        } catch (IOException e) {
            log.error("Erreur lors de la suppression de l'image", e);
            throw new RuntimeException("Erreur lors de la suppression de l'image", e);
        }
    }

    private String extractPublicIdFromUrl(String imageUrl) {
        // Logique pour extraire le public_id de l'URL Cloudinary
        // Exemple d'URL: https://res.cloudinary.com/demo/image/upload/v1586162289/sample.jpg
        // Le public_id serait "sample"
        try {
            String[] parts = imageUrl.split("/");
            String fileName = parts[parts.length - 1];
            return fileName.split("\\.")[0];
        } catch (Exception e) {
            throw new IllegalArgumentException("URL d'image Cloudinary invalide");
        }
    }

    public String uploadLogo(MultipartFile file) throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("folder", "supermarche/logos"); // Dossier spécifique pour les logos
        params.put("public_id", "logo_" + System.currentTimeMillis()); // Nom unique

        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
        return (String) uploadResult.get("secure_url");
    }

    public void deleteLogo(String logoUrl) throws IOException {
        if (logoUrl == null) return;

        String publicId = extractPublicIdFromUrl(logoUrl);
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }

}
