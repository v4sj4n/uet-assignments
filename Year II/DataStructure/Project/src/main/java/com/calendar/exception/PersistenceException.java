package com.calendar.exception;

public class PersistenceException extends CalendarException {

    private final String filePath;

    public PersistenceException(String message, String filePath, Throwable cause) {
        super(message + ": " + filePath, cause);
        this.filePath = filePath;
    }

    public PersistenceException(String message, String filePath) {
        super(message + ": " + filePath);
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }
}
