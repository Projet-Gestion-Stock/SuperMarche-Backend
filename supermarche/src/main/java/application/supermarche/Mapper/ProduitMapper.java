package application.supermarche.Mapper;

import application.supermarche.DTO.PackageProduit.ProduitDTO;
import application.supermarche.Entites.PackageProduit.Produit;
import application.supermarche.Entites.PackageStock.Stock;
import application.supermarche.Entites.PackageUtilisateur.Utilisateur;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ProduitMapper {
    private static final int JOURS_ALERTE_PAR_DEFAUT = 21;

    public static Produit toEntity(ProduitDTO dto, Utilisateur utilisateur) {
        Produit produit = new Produit();
        produit.setProduit(dto.produit());
        produit.setPrix(dto.prix());
        produit.setCategorie(dto.categorie());
        produit.setDateExpiration(dto.dateExpiration());
        produit.setUnite(dto.unite());
        produit.setFournisseur(dto.fournisseur());
        produit.setDescription(dto.description());
        produit.setUtilisateur(utilisateur);

        // Création du stock associé
        Stock stock = new Stock();
        stock.setQuantite(dto.quantiteDisponible());
        stock.setSeuilAlerte(dto.seuilAlerte());
        stock.setProduit(produit);
        produit.setStock(stock);

        return produit;
    }


    public static ProduitDTO toDto(Produit produit, int joursAlerte, UtilisateurMapper utilisateurMapper) {
        return new ProduitDTO(
                produit.getId(),
                produit.getProduit(),
                produit.getPrix(),
                produit.getStock() != null ? produit.getStock().getQuantite() : 0,
                produit.getCategorie(),
                produit.getStock() != null ? produit.getStock().getSeuilAlerte() : 0,
                produit.getDateExpiration(),
                produit.getDateAjout(),
                produit.getUnite(),
                produit.getFournisseur(),
                produit.getDescription(),
                getStatutExpiration(produit, joursAlerte), // Méthode extraite pour plus de clarté
                utilisateurMapper.toDTO(produit.getUtilisateur())
        );
    }

    private static String getStatutExpiration(Produit produit, int joursAlerte) {
        if (produit.getDateExpiration() == null) return "BON_ÉTAT";

        long joursRestants = ChronoUnit.DAYS.between(LocalDate.now(), produit.getDateExpiration());

        if (joursRestants <= 0) return "PÉRIMÉ";
        if (joursRestants <= joursAlerte) return "ALERTE_PRESQUE_PÉRIMÉ";
        return "BON_ÉTAT";
    }

    public static ProduitDTO toDto(Produit produit, UtilisateurMapper utilisateurMapper) {
        return toDto(produit, JOURS_ALERTE_PAR_DEFAUT, utilisateurMapper);
    }

    public static void updateEntityFromDto(ProduitDTO dto, Produit entity) {
        entity.setProduit(dto.produit());
        entity.setPrix(dto.prix());
        entity.setCategorie(dto.categorie());
        entity.setDateExpiration(dto.dateExpiration());
        entity.setUnite(dto.unite());
        entity.setFournisseur(dto.fournisseur());
        entity.setDescription(dto.description());
    }
}