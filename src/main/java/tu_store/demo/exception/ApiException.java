package tu_store.demo.exception;


import org.springframework.http.HttpStatus;


public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;
    private final Object details;


    public ApiException(HttpStatus status, String errorCode, String message) {
        this(status, errorCode, message, null);
    }
    public ApiException(HttpStatus status, String errorCode, String message, Object details) {
        super(message);
        this.status = status; this.errorCode = errorCode; this.details = details;
    }
    public HttpStatus getStatus(){ return status; }
    public String getErrorCode(){ return errorCode; }
    public Object getDetails(){ return details; }
}