package tu_store.demo.dto;

public class ErrorResponse {
    private int status;
    private String error;
    private String timestamp;

    public ErrorResponse(int status, String error, String timestamp) {
        this.status = status;
        this.error = error;
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }
    public void setError(String error) {
        this.error = error;
    }

    public String getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
