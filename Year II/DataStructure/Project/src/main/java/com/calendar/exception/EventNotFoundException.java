package com.calendar.exception;

public class EventNotFoundException extends CalendarException {

    private final String searchCriteria;

    public EventNotFoundException(String searchCriteria) {
        super("Event not found: " + searchCriteria);
        this.searchCriteria = searchCriteria;
    }

    public String getSearchCriteria() {
        return searchCriteria;
    }
}
