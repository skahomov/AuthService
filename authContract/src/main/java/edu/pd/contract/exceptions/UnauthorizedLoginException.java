package edu.pd.contract.exceptions;

public class UnauthorizedLoginException extends RuntimeException {
    public UnauthorizedLoginException(String message) {
        super(message);
    }
}
