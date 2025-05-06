package application.supermarche.Exceptions.UtilisateurException;

public class AccesInterdit extends RuntimeException {
    public AccesInterdit(String message) {
        super(message);
    }
}
