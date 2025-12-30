package com.stukit.codebench.infrastructure.compiler;

public class CompileException extends Exception{

    public CompileException(String message) {
        super(message);
    }

    public CompileException(String message, Throwable cause) {
        super(message, cause);
    }
}
