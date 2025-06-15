package io.github.bigpig.exceptions;

public class SmartAnalysisException extends RuntimeException {
    public SmartAnalysisException(String message) {
        super(message);
    }

    public SmartAnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}