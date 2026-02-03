package com.calendar.exception;

import com.calendar.model.Event;

public class EventConflictException extends CalendarException {

    private final Event newEvent;
    private final Event existingEvent;

    public EventConflictException(Event newEvent, Event existingEvent) {
        super(String.format("Event '%s' conflicts with existing event '%s' on %s",
                newEvent.getTitle(),
                existingEvent.getTitle(),
                newEvent.getDate()));
        this.newEvent = newEvent;
        this.existingEvent = existingEvent;
    }

    public Event getNewEvent() {
        return newEvent;
    }

    public Event getExistingEvent() {
        return existingEvent;
    }
}
