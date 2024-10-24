package com.yandex.tasktracker.service.file;

public class ManagerSaveException extends RuntimeException {

    public ManagerSaveException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public String getDetailMessage() {
        return getMessage() + getCause();
    }
}
