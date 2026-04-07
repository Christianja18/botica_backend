package com.botica.botica.exception;

public class ProductoNotFoundException extends ResourceNotFoundException {
    public ProductoNotFoundException(String message) {
        super(message);
    }
}