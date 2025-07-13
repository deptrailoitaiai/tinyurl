package org.example.constants;

public enum ErrorMessage {
    UrlNotFound("url not found"),
    PasswordIncorrect("password is incorrect");

    private final String message;

    ErrorMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
