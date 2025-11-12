package tu_store.demo.dto;


public class ApiError {
    private String errorCode;
    private String message;
    private Object details;


    public ApiError(String errorCode, String message) {
        this(errorCode, message, null);
    }
    public ApiError(String errorCode, String message, Object details) {
        this.errorCode = errorCode; this.message = message; this.details = details;
    }
    public String getErrorCode(){ return errorCode; }
    public String getMessage(){ return message; }
    public Object getDetails(){ return details; }
}
