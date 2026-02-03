package com.calendar.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

public final class Event implements Comparable<Event> {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final String id;
    private final LocalDate date;
    private final LocalTime time;
    private final Duration duration;
    private final String title;
    private final String description;
    private final EventCategory category;
    private final EventPriority priority;
    private final LocalDateTime createdAt;

    private Event(Builder builder) {
        this.id = builder.id;
        this.date = builder.date;
        this.time = builder.time;
        this.duration = builder.duration;
        this.title = builder.title;
        this.description = builder.description;
        this.category = builder.category;
        this.priority = builder.priority;
        this.createdAt = builder.createdAt;
    }

    public String getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    public Duration getDuration() {
        return duration;
    }

    public LocalTime getEndTime() {
        return time.plus(duration);
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public EventCategory getCategory() {
        return category;
    }

    public EventPriority getPriority() {
        return priority;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getStartDateTime() {
        return LocalDateTime.of(date, time);
    }

    public LocalDateTime getEndDateTime() {
        return getStartDateTime().plus(duration);
    }

    @Override
    public int compareTo(Event other) {
        int dateComparison = this.date.compareTo(other.date);
        if (dateComparison != 0) {
            return dateComparison;
        }

        int timeComparison = this.time.compareTo(other.time);
        if (timeComparison != 0) {
            return timeComparison;
        }

        return Integer.compare(other.priority.getLevel(), this.priority.getLevel());
    }

    public boolean overlapsWith(Event other) {
        if (!this.date.equals(other.date)) {
            return false;
        }

        LocalTime thisStart = this.time;
        LocalTime thisEnd = this.getEndTime();
        LocalTime otherStart = other.time;
        LocalTime otherEnd = other.getEndTime();

        return thisStart.isBefore(otherEnd) && otherStart.isBefore(thisEnd);
    }

    public boolean isToday() {
        return date.equals(LocalDate.now());
    }

    public boolean isPast() {
        return getEndDateTime().isBefore(LocalDateTime.now());
    }

    public boolean isUpcoming() {
        return getStartDateTime().isAfter(LocalDateTime.now());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Event event = (Event) obj;
        return Objects.equals(id, event.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        String durationStr = formatDuration(duration);
        return String.format(
                "┌─────────────────────────────────────────────────────┐%n" +
                        "│ %s %-47s │%n" +
                        "├─────────────────────────────────────────────────────┤%n" +
                        "│ 📅 %s  ⏰ %s - %s (%s)           │%n" +
                        "│ %s  %s                                     │%n" +
                        "├─────────────────────────────────────────────────────┤%n" +
                        "│ 📝 %-47s │%n" +
                        "└─────────────────────────────────────────────────────┘",
                category.getIcon(),
                truncate(title, 47),
                date.format(DATE_FORMATTER),
                time.format(TIME_FORMATTER),
                getEndTime().format(TIME_FORMATTER),
                durationStr,
                priority.getIcon(),
                priority.getDisplayName(),
                truncate(description, 47));
    }

    public String toSimpleString() {
        return String.format("%s %s %s - %s",
                priority.getIcon(),
                date.format(DATE_FORMATTER),
                time.format(TIME_FORMATTER),
                title);
    }

    public String toCompactString() {
        return String.format("%s %s %s",
                date.format(DATE_FORMATTER),
                time.format(TIME_FORMATTER),
                truncate(title, 20));
    }

    private String truncate(String text, int maxLength) {
        if (text == null)
            return "";
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        if (hours > 0 && minutes > 0) {
            return hours + "h " + minutes + "m";
        } else if (hours > 0) {
            return hours + "h";
        } else {
            return minutes + "m";
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder()
                .id(this.id)
                .date(this.date)
                .time(this.time)
                .duration(this.duration)
                .title(this.title)
                .description(this.description)
                .category(this.category)
                .priority(this.priority)
                .createdAt(this.createdAt);
    }

    public static class Builder {
        private String id = UUID.randomUUID().toString();
        private LocalDate date;
        private LocalTime time;
        private Duration duration = Duration.ofHours(1);
        private String title;
        private String description = "";
        private EventCategory category = EventCategory.OTHER;
        private EventPriority priority = EventPriority.MEDIUM;
        private LocalDateTime createdAt = LocalDateTime.now();

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder date(LocalDate date) {
            this.date = date;
            return this;
        }

        public Builder time(LocalTime time) {
            this.time = time;
            return this;
        }

        public Builder duration(Duration duration) {
            this.duration = duration;
            return this;
        }

        public Builder durationMinutes(int minutes) {
            this.duration = Duration.ofMinutes(minutes);
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder category(EventCategory category) {
            this.category = category;
            return this;
        }

        public Builder priority(EventPriority priority) {
            this.priority = priority;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Event build() {
            validate();
            return new Event(this);
        }

        private void validate() {
            if (date == null) {
                throw new IllegalArgumentException("Event date cannot be null");
            }
            if (time == null) {
                throw new IllegalArgumentException("Event time cannot be null");
            }
            if (title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException("Event title cannot be null or empty");
            }
            if (duration == null || duration.isNegative() || duration.isZero()) {
                throw new IllegalArgumentException("Event duration must be positive");
            }
            if (category == null) {
                throw new IllegalArgumentException("Event category cannot be null");
            }
            if (priority == null) {
                throw new IllegalArgumentException("Event priority cannot be null");
            }
        }
    }
}
