package com.calendar.exception;

import java.time.LocalDate;

public class InvalidDateRangeException extends CalendarException {

    private final LocalDate startDate;
    private final LocalDate endDate;

    public InvalidDateRangeException(LocalDate startDate, LocalDate endDate) {
        super(String.format("Invalid date range: start date (%s) must be before or equal to end date (%s)",
                startDate, endDate));
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}
