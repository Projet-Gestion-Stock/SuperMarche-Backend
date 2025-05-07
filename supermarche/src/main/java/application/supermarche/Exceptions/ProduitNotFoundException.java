package application.supermarche.Exceptions;

public class ProduitNotFoundException extends RuntimeException {
    public ProduitNotFoundException(Long produitId) {
        super("Produit non trouvé avec l'ID: " + produitId);
    }
}