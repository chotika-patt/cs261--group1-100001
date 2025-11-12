package tu_store.demo.exception;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tu_store.demo.dto.ApiError;
import tu_store.demo.exception.ApiException;


@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiException(ApiException ex){
        ApiError err = new ApiError(ex.getErrorCode(), ex.getMessage(), ex.getDetails());
        return ResponseEntity.status(ex.getStatus()).body(err);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnknown(Exception ex){
        ex.printStackTrace();
        ApiError err = new ApiError("INTERNAL_ERROR", "Internal server error");
        return ResponseEntity.status(500).body(err);
    }
}
