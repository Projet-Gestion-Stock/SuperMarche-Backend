package application.supermarche.Exceptions;

public class ProduitNotFoundException extends RuntimeException {
    public ProduitNotFoundException(String message) {
        super(message);
    }
}
