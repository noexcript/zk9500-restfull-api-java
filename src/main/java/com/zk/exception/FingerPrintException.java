package com.zk.exception;

public class FingerPrintException extends RuntimeException {
    public FingerPrintException(String message) {
        super(message);
    }

    public FingerPrintException(String message, Throwable cause) {
        super(message, cause);
    }

}
