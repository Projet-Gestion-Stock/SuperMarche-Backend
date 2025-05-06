package application.supermarche.Exceptions;

public class RessourceNotFoundException extends RuntimeException {
    public RessourceNotFoundException(String message) {
        super(message);
    }
}
