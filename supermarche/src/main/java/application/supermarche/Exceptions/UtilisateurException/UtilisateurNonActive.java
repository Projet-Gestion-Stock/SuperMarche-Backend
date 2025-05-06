package application.supermarche.Exceptions.UtilisateurException;

public class UtilisateurNonActive extends RuntimeException {
    public UtilisateurNonActive(Long id) {
        super("Utilisateur avec ID " + id + " est inactif");
    }
}
