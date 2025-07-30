package org.example.constants;

public enum ErrorCode {
    SUCCESS("SUCCESS"),
    URL_NOT_FOUND("URL_NOT_FOUND"),
    URL_EXISTED("URL_EXISTED"),
    PASSWORD_IN_CORRECT("PASSWORD_IN_CORRECT"),
    URL_EXPIRED("URL_EXPIRED"),
    URL_DISABLED("URL_DISABLED"),
    PASSWORD_REQUIRED("PASSWORD_REQUIRED"),
    SYSTEM_ERROR("SYSTEM_ERROR");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
