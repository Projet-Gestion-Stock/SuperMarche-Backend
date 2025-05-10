package application.supermarche.Exceptions;

public class TokenExpiredException extends JwtException {
    public TokenExpiredException(String message) {
        super(message);
    }
}