package application.supermarche.Exceptions;

public class JwtException extends RuntimeException {
    // Constructeur avec message uniquement
    public JwtException(String message) {
        super(message);
    }

    // Constructeur avec message et cause
    public JwtException(String message, Throwable cause) {
        super(message, cause);
    }
}