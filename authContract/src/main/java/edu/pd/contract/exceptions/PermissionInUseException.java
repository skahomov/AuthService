package edu.pd.contract.exceptions;

public class PermissionInUseException extends RuntimeException {
    public PermissionInUseException(String message) {
        super(message);
    }
}