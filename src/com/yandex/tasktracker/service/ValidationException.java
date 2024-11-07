package com.yandex.tasktracker.service;

public class ValidationException extends RuntimeException {

    public ValidationException(final String message) {
        super(message);
    }
}