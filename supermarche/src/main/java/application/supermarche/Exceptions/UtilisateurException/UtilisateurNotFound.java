package application.supermarche.Exceptions.UtilisateurException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UtilisateurNotFound extends RuntimeException {

    public UtilisateurNotFound(Long id) {
    super("Utilisateur avec ID " + id + " non trouvé");
    }
}
