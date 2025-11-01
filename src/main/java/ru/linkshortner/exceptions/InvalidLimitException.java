package ru.linkshortner.exceptions;

public class InvalidLimitException extends RuntimeException {

    public InvalidLimitException(String message) {
        super(message);
    }
}
