package application.supermarche.Exceptions.UtilisateurException;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ApiResponse<T> {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private T data;                        // Utilisation de T pour permettre d'envoyer différents types de données
    private boolean success;              // Ajout d'un attribut success

    // Constructeur pour les erreurs
    public ApiResponse(boolean success, int status, String message) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.message = message;
        this.success = success;        // Définit la réussite ou non
        this.error = null;            // Pas d'erreur si c'est une réussite
        this.data = null;            // Pas de données si c'est une erreur
    }

    // Constructeur pour les réponses avec données
    public ApiResponse(boolean success, int status, String message, T data) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.message = message;
        this.success = success;       // Définit la réussite ou non
        this.error = null;           // Pas d'erreur si c'est une réussite
        this.data = data;
    }

    // Setters manuels pour éviter les erreurs
    public void setStatus(int status) {
        this.status = status;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
