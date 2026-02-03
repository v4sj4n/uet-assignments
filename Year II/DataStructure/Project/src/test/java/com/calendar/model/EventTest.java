package com.calendar.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Event class.
 * Tests the Builder pattern, immutability, comparison, and overlap detection.
 * 
 * @author Personal Calendar Team
 * @version 1.0
 */
@DisplayName("Event Class Tests")
class EventTest {

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create event with all required fields")
        void shouldCreateEventWithRequiredFields() {
            Event event = Event.builder()
                    .date(LocalDate.of(2026, 1, 26))
                    .time(LocalTime.of(14, 30))
                    .title("Test Event")
                    .build();

            assertNotNull(event);
            assertEquals("Test Event", event.getTitle());
            assertEquals(LocalDate.of(2026, 1, 26), event.getDate());
            assertEquals(LocalTime.of(14, 30), event.getTime());
            assertNotNull(event.getId()); // UUID should be auto-generated
        }

        @Test
        @DisplayName("Should create event with all optional fields")
        void shouldCreateEventWithAllFields() {
            Event event = Event.builder()
                    .date(LocalDate.of(2026, 1, 26))
                    .time(LocalTime.of(14, 30))
                    .durationMinutes(90)
                    .title("Full Event")
                    .description("This is a description")
                    .category(EventCategory.WORK)
                    .priority(EventPriority.HIGH)
                    .build();

            assertEquals("Full Event", event.getTitle());
            assertEquals("This is a description", event.getDescription());
            assertEquals(EventCategory.WORK, event.getCategory());
            assertEquals(EventPriority.HIGH, event.getPriority());
            assertEquals(Duration.ofMinutes(90), event.getDuration());
        }

        @Test
        @DisplayName("Should throw exception for null date")
        void shouldThrowForNullDate() {
            Event.Builder builder = Event.builder()
                    .time(LocalTime.of(14, 30))
                    .title("Test Event");

            assertThrows(IllegalArgumentException.class, builder::build);
        }

        @Test
        @DisplayName("Should throw exception for null time")
        void shouldThrowForNullTime() {
            Event.Builder builder = Event.builder()
                    .date(LocalDate.now())
                    .title("Test Event");

            assertThrows(IllegalArgumentException.class, builder::build);
        }

        @Test
        @DisplayName("Should throw exception for empty title")
        void shouldThrowForEmptyTitle() {
            Event.Builder builder = Event.builder()
                    .date(LocalDate.now())
                    .time(LocalTime.now())
                    .title("");

            assertThrows(IllegalArgumentException.class, builder::build);
        }

        @Test
        @DisplayName("Should use default values for optional fields")
        void shouldUseDefaultValues() {
            Event event = Event.builder()
                    .date(LocalDate.now())
                    .time(LocalTime.now())
                    .title("Test")
                    .build();

            assertEquals(Duration.ofHours(1), event.getDuration()); // Default 1 hour
            assertEquals(EventCategory.OTHER, event.getCategory()); // Default OTHER
            assertEquals(EventPriority.MEDIUM, event.getPriority()); // Default MEDIUM
            assertEquals("", event.getDescription()); // Default empty
        }

        @Test
        @DisplayName("Should allow creating modified copy with toBuilder")
        void shouldAllowModifiedCopy() {
            Event original = Event.builder()
                    .date(LocalDate.of(2026, 1, 26))
                    .time(LocalTime.of(14, 30))
                    .title("Original")
                    .build();

            Event modified = original.toBuilder()
                    .title("Modified")
                    .build();

            assertEquals("Original", original.getTitle());
            assertEquals("Modified", modified.getTitle());
            assertEquals(original.getDate(), modified.getDate());
            // IDs should be the same since we used toBuilder
            assertEquals(original.getId(), modified.getId());
        }
    }

    @Nested
    @DisplayName("Comparison Tests")
    class ComparisonTests {

        @Test
        @DisplayName("Should order events by date")
        void shouldOrderByDate() {
            Event earlier = createEvent(LocalDate.of(2026, 1, 1), LocalTime.of(12, 0));
            Event later = createEvent(LocalDate.of(2026, 1, 2), LocalTime.of(12, 0));

            assertTrue(earlier.compareTo(later) < 0);
            assertTrue(later.compareTo(earlier) > 0);
        }

        @Test
        @DisplayName("Should order events by time when dates are equal")
        void shouldOrderByTimeWhenDatesEqual() {
            LocalDate date = LocalDate.of(2026, 1, 26);
            Event morning = createEvent(date, LocalTime.of(9, 0));
            Event afternoon = createEvent(date, LocalTime.of(14, 0));

            assertTrue(morning.compareTo(afternoon) < 0);
            assertTrue(afternoon.compareTo(morning) > 0);
        }

        @Test
        @DisplayName("Should order by priority when date and time are equal")
        void shouldOrderByPriorityWhenDateTimeEqual() {
            LocalDate date = LocalDate.of(2026, 1, 26);
            LocalTime time = LocalTime.of(14, 0);

            Event lowPriority = Event.builder()
                    .date(date)
                    .time(time)
                    .title("Low Priority")
                    .priority(EventPriority.LOW)
                    .build();

            Event highPriority = Event.builder()
                    .date(date)
                    .time(time)
                    .title("High Priority")
                    .priority(EventPriority.HIGH)
                    .build();

            // Higher priority should come first (negative comparison)
            assertTrue(highPriority.compareTo(lowPriority) < 0);
        }
    }

    @Nested
    @DisplayName("Overlap Detection Tests")
    class OverlapTests {

        @Test
        @DisplayName("Should detect overlapping events")
        void shouldDetectOverlap() {
            LocalDate date = LocalDate.of(2026, 1, 26);

            Event event1 = Event.builder()
                    .date(date)
                    .time(LocalTime.of(10, 0))
                    .durationMinutes(60)
                    .title("Event 1")
                    .build();

            Event event2 = Event.builder()
                    .date(date)
                    .time(LocalTime.of(10, 30))
                    .durationMinutes(60)
                    .title("Event 2")
                    .build();

            assertTrue(event1.overlapsWith(event2));
            assertTrue(event2.overlapsWith(event1)); // Should be symmetric
        }

        @Test
        @DisplayName("Should not detect overlap for non-overlapping events")
        void shouldNotDetectOverlapForSeparateEvents() {
            LocalDate date = LocalDate.of(2026, 1, 26);

            Event event1 = Event.builder()
                    .date(date)
                    .time(LocalTime.of(10, 0))
                    .durationMinutes(60)
                    .title("Event 1")
                    .build();

            Event event2 = Event.builder()
                    .date(date)
                    .time(LocalTime.of(11, 30))
                    .durationMinutes(60)
                    .title("Event 2")
                    .build();

            assertFalse(event1.overlapsWith(event2));
        }

        @Test
        @DisplayName("Should not detect overlap for different dates")
        void shouldNotDetectOverlapForDifferentDates() {
            Event event1 = Event.builder()
                    .date(LocalDate.of(2026, 1, 26))
                    .time(LocalTime.of(10, 0))
                    .durationMinutes(60)
                    .title("Event 1")
                    .build();

            Event event2 = Event.builder()
                    .date(LocalDate.of(2026, 1, 27))
                    .time(LocalTime.of(10, 0))
                    .durationMinutes(60)
                    .title("Event 2")
                    .build();

            assertFalse(event1.overlapsWith(event2));
        }

        @Test
        @DisplayName("Should not detect overlap for adjacent events")
        void shouldNotDetectOverlapForAdjacentEvents() {
            LocalDate date = LocalDate.of(2026, 1, 26);

            Event event1 = Event.builder()
                    .date(date)
                    .time(LocalTime.of(10, 0))
                    .durationMinutes(60)
                    .title("Event 1")
                    .build();

            Event event2 = Event.builder()
                    .date(date)
                    .time(LocalTime.of(11, 0))
                    .durationMinutes(60)
                    .title("Event 2")
                    .build();

            assertFalse(event1.overlapsWith(event2));
        }
    }

    @Nested
    @DisplayName("Temporal Status Tests")
    class TemporalStatusTests {

        @Test
        @DisplayName("Should correctly identify today's events")
        void shouldIdentifyTodaysEvents() {
            Event todayEvent = Event.builder()
                    .date(LocalDate.now())
                    .time(LocalTime.now())
                    .title("Today's Event")
                    .build();

            Event tomorrowEvent = Event.builder()
                    .date(LocalDate.now().plusDays(1))
                    .time(LocalTime.now())
                    .title("Tomorrow's Event")
                    .build();

            assertTrue(todayEvent.isToday());
            assertFalse(tomorrowEvent.isToday());
        }

        @Test
        @DisplayName("Should correctly calculate end time")
        void shouldCalculateEndTime() {
            Event event = Event.builder()
                    .date(LocalDate.now())
                    .time(LocalTime.of(10, 0))
                    .durationMinutes(90)
                    .title("Test")
                    .build();

            assertEquals(LocalTime.of(11, 30), event.getEndTime());
        }
    }

    @Nested
    @DisplayName("Equality Tests")
    class EqualityTests {

        @Test
        @DisplayName("Should be equal when IDs match")
        void shouldBeEqualWhenIdsMatch() {
            Event event1 = Event.builder()
                    .id("same-id")
                    .date(LocalDate.now())
                    .time(LocalTime.now())
                    .title("Event 1")
                    .build();

            Event event2 = Event.builder()
                    .id("same-id")
                    .date(LocalDate.now().plusDays(1))
                    .time(LocalTime.now())
                    .title("Event 2")
                    .build();

            assertEquals(event1, event2);
            assertEquals(event1.hashCode(), event2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when IDs differ")
        void shouldNotBeEqualWhenIdsDiffer() {
            Event event1 = Event.builder()
                    .date(LocalDate.now())
                    .time(LocalTime.now())
                    .title("Same Title")
                    .build();

            Event event2 = Event.builder()
                    .date(LocalDate.now())
                    .time(LocalTime.now())
                    .title("Same Title")
                    .build();

            assertNotEquals(event1, event2); // Different auto-generated UUIDs
        }
    }

    // Helper method for creating simple events
    private Event createEvent(LocalDate date, LocalTime time) {
        return Event.builder()
                .date(date)
                .time(time)
                .title("Test Event")
                .build();
    }
}
