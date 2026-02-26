package com.restaurant.order.exception;

public class InvalidPincodeException extends RuntimeException {

    public InvalidPincodeException(String message) {
        super(message);
    }
}