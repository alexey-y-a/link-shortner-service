package ru.linkshortner.exceptions;

public class ConfigLoadException extends RuntimeException {

    public ConfigLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
