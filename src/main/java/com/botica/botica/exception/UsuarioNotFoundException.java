package com.botica.botica.exception;

public class UsuarioNotFoundException extends ResourceNotFoundException {
    public UsuarioNotFoundException(String message) {
        super(message);
    }
}