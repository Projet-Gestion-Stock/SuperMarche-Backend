package application.supermarche.Exceptions;

import application.supermarche.Exceptions.UtilisateurException.AccesInterdit;
import application.supermarche.Exceptions.UtilisateurException.EmailDejaUtilise;
import application.supermarche.Exceptions.UtilisateurException.UtilisateurNonActive;
import application.supermarche.Exceptions.UtilisateurException.UtilisateurNotFound;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Gestion de l'exception UtilisateurNotFoundException
    @ExceptionHandler(UtilisateurNotFound.class)
    public ResponseEntity<ApiResponse> handleUtilisateurNotFound(UtilisateurNotFound ex) {
        ApiResponse response = new ApiResponse<>(false, HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return buildResponseEntity(HttpStatus.NOT_FOUND, response);
    }

    // Gestion de l'exception EmailDejaUtiliseException
    @ExceptionHandler(EmailDejaUtilise.class)
    public ResponseEntity<ApiResponse> handleEmailDejaUtiliseException(EmailDejaUtilise ex) {
        ApiResponse response = new ApiResponse<>(false, HttpStatus.CONFLICT.value(), ex.getMessage());
        return buildResponseEntity(HttpStatus.CONFLICT, response);
    }

    // Gestion de l'exception AccesInterditException
    @ExceptionHandler(AccesInterdit.class)
    public ResponseEntity<ApiResponse> handleAccesInterditException(AccesInterdit ex) {
        ApiResponse response = new ApiResponse<>(false, HttpStatus.FORBIDDEN.value(), ex.getMessage());
        return buildResponseEntity(HttpStatus.FORBIDDEN, response);
    }

    // Gestion de l'exception UtilisateurNonActiveException
    @ExceptionHandler(UtilisateurNonActive.class)
    public ResponseEntity<ApiResponse> handleUtilisateurNonActiveException(UtilisateurNonActive ex) {
        ApiResponse response = new ApiResponse<>(false, HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
        return buildResponseEntity(HttpStatus.UNAUTHORIZED, response);
    }

    // Méthode utilitaire pour créer une réponse standardisée
    private ResponseEntity<ApiResponse> buildResponseEntity(HttpStatus status, ApiResponse response) {
        response.setStatus(status.value());
        response.setError(status.getReasonPhrase());
        response.setTimestamp(LocalDateTime.now());
        return new ResponseEntity<>(response, status);
    }

    // MBK exception


    @ExceptionHandler(RessourceNotFoundException.class)
    public ResponseEntity<String> handleRessourceNotFound(RessourceNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        return new ResponseEntity<>("Erreur serveur : " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}