package application.supermarche.Services.PackageCloudinary;

import application.supermarche.Entites.PackageProduit.Produit;
import application.supermarche.Enumeration.ErrorCode;
import application.supermarche.Exceptions.BusinessException;
import application.supermarche.Exceptions.ResourceNotFoundException;
import application.supermarche.Repository.ProduitRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class CloudinaryService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private final Cloudinary cloudinary;
    private final ProduitRepository produitRepository;

    public CloudinaryService(Cloudinary cloudinary, ProduitRepository produitRepository) {
        this.cloudinary = cloudinary;
        this.produitRepository = produitRepository;
    }

    public String uploadProduitImage(MultipartFile file, Long produitId) {
        try {
            log.info("Tentative d'upload d'image pour produit ID: {}", produitId);
            validateImageFile(file);

            Produit produit = produitRepository.findById(produitId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Produit non trouvé avec ID: ",
                            ErrorCode.PRODUCT_NOT_FOUND));

            Map<String, Object> options = createUploadOptions(produitId);
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
            String imageUrl = getSecureUrl(uploadResult);

            updateProduitImage(produit, imageUrl);
            log.info("Image uploadée avec succès - Produit ID: {}, URL: {}", produitId, imageUrl);

            return imageUrl;

        } catch (ResourceNotFoundException e) {
            log.warn("Produit non trouvé pour l'upload: {}", produitId);
            throw e;
        } catch (BusinessException e) {
            log.warn("Validation échouée pour l'upload: {}", e.getMessage());
            throw e;
        } catch (IOException e) {
            log.error("Erreur IO lors de l'upload: {}", e.getMessage());
            throw new BusinessException(
                    "Erreur lors de l'upload de l'image",
                    ErrorCode.IMAGE_UPLOAD_ERROR);
        } catch (Exception e) {
            log.error("Erreur technique lors de l'upload: {}", e.getMessage());
            throw new BusinessException(
                    "Erreur technique lors de l'upload",
                    ErrorCode.TECHNICAL_ERROR);
        }
    }

    public void deleteImage(Long produitId) {
        try {
            log.info("Tentative de suppression d'image pour produit ID: {}", produitId);
            Produit produit = produitRepository.findById(produitId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Produit non trouvé avec ID: ",
                            ErrorCode.PRODUCT_NOT_FOUND));

            if (produit.getImageUrl() == null) {
                log.debug("Aucune image à supprimer pour produit ID: {}", produitId);
                return;
            }

            String publicId = extractPublicIdFromUrl(produit.getImageUrl());
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());

            produit.setImageUrl(null);
            produitRepository.save(produit);
            log.info("Image supprimée avec succès - Produit ID: {}", produitId);

        } catch (ResourceNotFoundException e) {
            log.warn("Produit non trouvé pour suppression image: {}", produitId);
            throw e;
        } catch (IOException e) {
            log.error("Erreur IO lors de la suppression: {}", e.getMessage());
            throw new BusinessException(
                    "Erreur lors de la suppression de l'image",
                    ErrorCode.IMAGE_DELETE_ERROR);
        } catch (Exception e) {
            log.error("Erreur technique lors de la suppression: {}", e.getMessage());
            throw new BusinessException(
                    "Erreur technique lors de la suppression",
                    ErrorCode.TECHNICAL_ERROR);
        }
    }

    public String uploadLogo(MultipartFile file) {
        try {
            log.info("Tentative d'upload de logo");
            validateImageFile(file);

            Map<String, Object> params = new HashMap<>();
            params.put("folder", "supermarche/logos");
            params.put("public_id", "logo_" + System.currentTimeMillis());
            params.put("transformation", new Transformation()
                    .width(500).height(500).crop("limit").quality("auto:best"));

            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
            String logoUrl = getSecureUrl(uploadResult);

            log.info("Logo uploadé avec succès - URL: {}", logoUrl);
            return logoUrl;

        } catch (BusinessException e) {
            log.warn("Validation échouée pour l'upload logo: {}", e.getMessage());
            throw e;
        } catch (IOException e) {
            log.error("Erreur IO lors de l'upload logo: {}", e.getMessage());
            throw new BusinessException(
                    "Erreur lors de l'upload du logo",
                    ErrorCode.LOGO_UPLOAD_ERROR);
        } catch (Exception e) {
            log.error("Erreur technique lors de l'upload logo: {}", e.getMessage());
            throw new BusinessException(
                    "Erreur technique lors de l'upload du logo",
                    ErrorCode.TECHNICAL_ERROR);
        }
    }

    public void deleteLogo(String logoUrl) {
        try {
            if (logoUrl == null) {
                log.debug("Aucun logo à supprimer (URL null)");
                return;
            }

            log.info("Tentative de suppression de logo: {}", logoUrl);
            String publicId = extractPublicIdFromUrl(logoUrl);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Logo supprimé avec succès");

        } catch (IOException e) {
            log.error("Erreur IO lors de la suppression logo: {}", e.getMessage());
            throw new BusinessException(
                    "Erreur lors de la suppression du logo",
                    ErrorCode.LOGO_DELETE_ERROR);
        } catch (Exception e) {
            log.error("Erreur technique lors de la suppression logo: {}", e.getMessage());
            throw new BusinessException(
                    "Erreur technique lors de la suppression du logo",
                    ErrorCode.TECHNICAL_ERROR);
        }
    }

    // Méthodes privées utilitaires
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(
                    "Le fichier image est vide",
                    ErrorCode.EMPTY_FILE);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(
                    String.format("Taille maximale dépassée (%dMB max)",
                            MAX_FILE_SIZE / (1024 * 1024)),
                    ErrorCode.FILE_TOO_LARGE);
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException(
                    "Format d'image non supporté. Formats acceptés: " + ALLOWED_CONTENT_TYPES,
                    ErrorCode.INVALID_FILE_TYPE);
        }
    }

    private Map<String, Object> createUploadOptions(Long produitId) {
        Map<String, Object> options = new HashMap<>();
        options.put("folder", "supermarche/produits");
        options.put("public_id", "produit_" + produitId);
        options.put("overwrite", true);
        options.put("resource_type", "image");
        options.put("transformation", new Transformation()
                .width(800).height(800).crop("limit").quality("auto:best"));
        return options;
    }

    private String getSecureUrl(Map<?, ?> uploadResult) {
        String url = (String) uploadResult.get("secure_url");
        if (url == null) {
            throw new BusinessException(
                    "URL sécurisée non reçue de Cloudinary",
                    ErrorCode.CLOUDINARY_ERROR);
        }
        return url;
    }

    private String extractPublicIdFromUrl(String imageUrl) {
        try {
            String[] parts = imageUrl.split("/");
            String fileName = parts[parts.length - 1];
            return fileName.split("\\.")[0];
        } catch (Exception e) {
            throw new BusinessException(
                    "URL d'image Cloudinary invalide",
                    ErrorCode.INVALID_IMAGE_URL);
        }
    }

    private void updateProduitImage(Produit produit, String imageUrl) {
        produit.setImageUrl(imageUrl);
        produitRepository.save(produit);
    }
}
