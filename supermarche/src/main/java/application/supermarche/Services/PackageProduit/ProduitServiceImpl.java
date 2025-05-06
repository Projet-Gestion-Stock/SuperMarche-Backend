package application.supermarche.Services.PackageProduit;

import application.supermarche.DTO.PackageProduit.ProduitDTO;
import application.supermarche.Entites.PackageProduit.Produit;
import application.supermarche.Mapper.ProduitMapper;
import application.supermarche.Mapper.UtilisateurMapper;
import application.supermarche.Repository.ProduitRepository;
import application.supermarche.Repository.StockRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
                .orElseThrow(() -> new EntityNotFoundException(
                        "Produit actif non trouvé. Soit l'ID est invalide (" + id + "), soit le produit est désactivé"));
    }


    @Override
    @Transactional
    public Produit modifierProduit(Long id, ProduitDTO produitDTO) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé avec l'ID : " + id));

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
        // Désactiver le produit
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé"));
        produit.setActif(false);
        produitRepository.save(produit);

        // Mettre à zéro le stock associé
        stockRepository.findByProduitId(id)
                .ifPresent(stock -> {
                    stock.setQuantite(0);
                    stockRepository.save(stock);
                    log.info("Stock mis à zéro pour le produit ID: {}", id);
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
        List<Produit> produits = produitRepository.findAllWithStock();
        return produits.stream()
                .map(p -> ProduitMapper.toDto(p, joursAlerte, utilisateurMapper))
                .toList();
    }

    // Méthode alternative si suppression absolument nécessaire
    @Transactional
    public void supprimerProduitAvecNettoyage(Long id) {
        // Implémentez cette méthode si vous avez vraiment besoin de supprimer physiquement
        // (nécessitera des modifications supplémentaires dans les repositories)
    }
}