package application.supermarche.Exceptions;

import application.supermarche.Enumeration.ErrorCode;
import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}