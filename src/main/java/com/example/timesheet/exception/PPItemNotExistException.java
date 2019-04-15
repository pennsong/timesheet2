package com.example.timesheet.exception;

public class PPItemNotExistException extends RuntimeException {
    public PPItemNotExistException(String message) {
        super(message);
    }
}
