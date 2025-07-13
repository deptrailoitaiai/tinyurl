package org.example.exeption;

public class PasswordUrlIncorrectException extends RuntimeException {
    public PasswordUrlIncorrectException(String message) {
        super(message);
    }
}
