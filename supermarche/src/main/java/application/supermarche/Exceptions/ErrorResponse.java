package application.supermarche.Exceptions;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Data

public class ErrorResponse {

    public ErrorResponse(int status, String message, long timestamp, String path, String errorCode) {
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
        this.errorCode = errorCode;
    }

    private int status;
    private String message;
    private String errorCode;
    private long timestamp;

    public ErrorResponse(int value, String message, String name) {
    }
}
