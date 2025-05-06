package application.supermarche.Exceptions.UtilisateurException;

public class EmailDejaUtilise extends RuntimeException {
    public EmailDejaUtilise(String email) {
        super("L'email " + email + " est déjà utilisé");
    }
}
