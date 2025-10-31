package ru.homecrew.exception;

import java.util.Objects;

public class WebServerConfigException extends RuntimeException {

    public WebServerConfigException(String message, Throwable cause) {
        super(message, Objects.requireNonNull(cause, "Cause must not be null"));
    }
}
