package application.supermarche.Services.PackageProduit;

import application.supermarche.DTO.PackageProduit.ProduitDTO;
import application.supermarche.Entites.PackageProduit.Produit;
import application.supermarche.Enumeration.ErrorCode;
import application.supermarche.Exceptions.BusinessException;
import application.supermarche.Exceptions.ResourceNotFoundException;
import application.supermarche.Mapper.ProduitMapper;
import application.supermarche.Mapper.UtilisateurMapper;
import application.supermarche.Repository.ProduitRepository;
import application.supermarche.Repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProduitServiceImpl implements ProduitService {

    private final ProduitRepository produitRepository;
    private final StockRepository stockRepository;
    private final UtilisateurMapper utilisateurMapper;
    private static final int JOURS_ALERTE_PAR_DEFAUT = 21;


    @Override
    @Transactional
    public Produit ajouterProduit(Produit produit) {
        log.info("Ajout d'un nouveau produit : {}", produit.getProduit());

        if (produitRepository.existsByProduitAndFournisseur(produit.getProduit(), produit.getFournisseur())) {
            throw new BusinessException(
                    "Un produit similaire existe déjà",
                    ErrorCode.PRODUCT_ALREADY_EXISTS,
                    Map.of(
                            "productName", produit.getProduit(),
                            "supplier", produit.getFournisseur()
                    ));
        }

        return produitRepository.save(produit);
    }

    @Override
    public List<Produit> listerProduitsActifs() {
        return produitRepository.findByActifTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public Produit recupererProduit(Long id) {
        return produitRepository.findByIdAndActifTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Produit non trouvé avec ID: " + id,
                        ErrorCode.PRODUCT_NOT_FOUND));
    }

    @Override
    @Transactional
    public Produit modifierProduit(Long id, ProduitDTO produitDTO) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Produit non trouvé avec ID: " + id,
                        ErrorCode.PRODUCT_NOT_FOUND));

        if (!produit.isActif()) {
            throw new BusinessException(
                    "Impossible de modifier un produit désactivé",
                    ErrorCode.PRODUCT_INACTIVE);
        }

        // Validation du prix
        if (produitDTO.prix() <= 0) {
            throw new BusinessException(
                    "Le prix doit être positif",
                    ErrorCode.INVALID_PRICE);
        }

        // Mise à jour uniquement des champs modifiables
        produit.setProduit(produitDTO.produit());
        produit.setPrix(produitDTO.prix());
        produit.setCategorie(produitDTO.categorie());
        produit.setDateExpiration(produitDTO.dateExpiration());
        produit.setUnite(produitDTO.unite());
        produit.setFournisseur(produitDTO.fournisseur());
        produit.setDescription(produitDTO.description());

        return produitRepository.save(produit);
    }

    @Override
    @Transactional
    public void desactiverProduit(Long id) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Produit non trouvé avec ID: " + id,
                        ErrorCode.PRODUCT_NOT_FOUND));

        if (!produit.isActif()) {
            log.warn("Tentative de désactivation d'un produit déjà désactivé: {}", id);
            return;
        }

        produit.setActif(false);
        produitRepository.save(produit);

        stockRepository.findByProduitId(id).ifPresent(stock -> {
            if (stock.getQuantite() > 0) {
                log.info("Réinitialisation du stock ({} unités) pour le produit ID: {}",
                        stock.getQuantite(), id);
                stock.setQuantite(0);
                stockRepository.save(stock);
            }
        });
    }

    // liste des produits en rupture

    @Override
    @Transactional(readOnly = true)
    public List<Produit> listerProduitsEnRupture() {
        return produitRepository.findProduitsEnRupture();
    }


    // liste des produit expire

    @Override
    @Transactional(readOnly = true)
    public List<ProduitDTO> getProduitsAvecStatutExpiration(int joursAlerte) {
        if (joursAlerte < 1) {
            throw new BusinessException(
                    "Le seuil d'alerte doit être positif",
                    ErrorCode.INVALID_ALERT_THRESHOLD);
        }

        try {
            List<Produit> produits = produitRepository.findAllWithStock();
            return produits.stream()
                    .filter(p -> p.getDateExpiration() != null) // Filtre les produits sans date
                    .map(p -> ProduitMapper.toDto(p, joursAlerte, utilisateurMapper))
                    .toList();
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des produits expirés", e);
            throw new BusinessException(
                    "Erreur technique lors du traitement des dates d'expiration",
                    ErrorCode.EXPIRATION_DATE_PROCESSING_ERROR);
        }
    }

    // Méthode alternative si suppression absolument nécessaire
    @Transactional
    public void supprimerProduitAvecNettoyage(Long id) {
        // Implémentez cette méthode si vous avez vraiment besoin de supprimer physiquement
        // (nécessitera des modifications supplémentaires dans les repositories)
    }
}