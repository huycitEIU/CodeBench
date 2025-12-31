package com.stukit.codebench.infrastructure.runner;

import java.io.Serial;

public class RunnerException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    public RunnerException(String message) {
        super(message);
    }

    public RunnerException(String message, Throwable cause) {
        super(message, cause);
    }
}