package com.stukit.codebench.infrastructure.runner;

public class TimeoutException extends RuntimeException {
    public TimeoutException(String message) {
        super(message);
    }
}
