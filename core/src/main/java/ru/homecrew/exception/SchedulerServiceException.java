package ru.homecrew.exception;

public class SchedulerServiceException extends RuntimeException {

    public SchedulerServiceException(String message, Throwable cause) {
        super(message, cause);
        if (cause == null) {
            throw new IllegalArgumentException("SchedulerServiceException requires non-null cause");
        }
    }
}
