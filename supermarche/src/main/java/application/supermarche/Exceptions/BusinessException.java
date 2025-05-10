package application.supermarche.Exceptions;

import application.supermarche.Enumeration.ErrorCode;
import lombok.Getter;

import java.util.Map;

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Map<String, Object> details;

    public BusinessException(String message, ErrorCode errorCode) {
        this(message, errorCode, Map.of());
    }

    public BusinessException(String message, ErrorCode errorCode, Map<String, Object> details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }
    // ... getters
}