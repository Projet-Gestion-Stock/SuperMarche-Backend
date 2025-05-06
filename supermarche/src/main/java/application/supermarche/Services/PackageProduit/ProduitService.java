package application.supermarche.Services.PackageProduit;

import application.supermarche.DTO.PackageProduit.ProduitDTO;
import application.supermarche.Entites.PackageProduit.Produit;

import java.util.Collection;
import java.util.List;

public interface ProduitService {
    Produit ajouterProduit(Produit produit);
    //List<Produit> listerProduits();
    Produit recupererProduit(Long id);
    Produit modifierProduit(Long id, ProduitDTO produitDTO);
    void desactiverProduit(Long id);
    List<Produit> listerProduitsEnRupture();
    List<Produit> listerProduitsActifs();
    List<ProduitDTO> getProduitsAvecStatutExpiration(int joursAlerte);
}
