package org.example.constants;

public enum ErrorCode {
    SUCCESS("SUCCESS"),
    USER_NOT_FOUND("USER_NOT_FOUND"),
    USER_EXISTED("USER_EXISTED"),
    USER_NOT_EXISTED("USER_NOT_EXISTED"),
    PASSWORD_IN_CORRECT("PASSWORD_IN_CORRECT"),
    SYSTEM_ERROR("SYSTEM_ERROR"),
    UNIDENTIFIED_TOKEN("UNIDENTIFIED_TOKEN");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
