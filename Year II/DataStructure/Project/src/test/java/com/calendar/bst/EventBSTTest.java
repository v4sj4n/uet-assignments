package com.calendar.bst;

import com.calendar.exception.EventConflictException;
import com.calendar.exception.EventNotFoundException;
import com.calendar.exception.InvalidDateRangeException;
import com.calendar.model.Event;
import com.calendar.model.EventCategory;
import com.calendar.model.EventPriority;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the EventBST class.
 * Tests core BST operations including insert, search, delete, and traversal.
 * 
 * @author Personal Calendar Team
 * @version 1.0
 */
@DisplayName("EventBST Class Tests")
class EventBSTTest {

    private EventBST bst;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        bst = new EventBST();
        today = LocalDate.now();
    }

    @Nested
    @DisplayName("Insert Operations")
    class InsertTests {

        @Test
        @DisplayName("Should insert single event")
        void shouldInsertSingleEvent() {
            Event event = createEvent(today, LocalTime.of(10, 0), "Test Event");
            bst.insert(event);

            assertEquals(1, bst.getSize());
            assertFalse(bst.isEmpty());
        }

        @Test
        @DisplayName("Should insert multiple events in order")
        void shouldInsertMultipleEventsInOrder() {
            bst.insert(createEvent(today.plusDays(1), LocalTime.of(10, 0), "Event 2"));
            bst.insert(createEvent(today, LocalTime.of(10, 0), "Event 1"));
            bst.insert(createEvent(today.plusDays(2), LocalTime.of(10, 0), "Event 3"));

            assertEquals(3, bst.getSize());

            // Should be in chronological order
            List<Event> events = bst.getAllEvents();
            assertEquals("Event 1", events.get(0).getTitle());
            assertEquals("Event 2", events.get(1).getTitle());
            assertEquals("Event 3", events.get(2).getTitle());
        }

        @Test
        @DisplayName("Should throw for null event")
        void shouldThrowForNullEvent() {
            assertThrows(NullPointerException.class, () -> bst.insert(null));
        }

        @Test
        @DisplayName("Should allow events with same date/time")
        void shouldAllowEventsWithSameDateTime() {
            bst.insert(createEvent(today, LocalTime.of(10, 0), "Event 1"));
            bst.insert(createEvent(today, LocalTime.of(10, 0), "Event 2"));

            assertEquals(2, bst.getSize());
        }

        @Test
        @DisplayName("Should detect conflict when enabled")
        void shouldDetectConflictWhenEnabled() {
            Event event1 = Event.builder()
                    .date(today)
                    .time(LocalTime.of(10, 0))
                    .durationMinutes(60)
                    .title("Event 1")
                    .build();

            Event event2 = Event.builder()
                    .date(today)
                    .time(LocalTime.of(10, 30))
                    .durationMinutes(60)
                    .title("Event 2")
                    .build();

            bst.insert(event1, true);

            assertThrows(EventConflictException.class, () -> bst.insert(event2, true));
        }
    }

    @Nested
    @DisplayName("Search Operations")
    class SearchTests {

        @BeforeEach
        void addTestEvents() {
            bst.insert(createEvent(today, LocalTime.of(9, 0), "Morning Meeting"));
            bst.insert(createEvent(today, LocalTime.of(14, 0), "Afternoon Task"));
            bst.insert(createEvent(today.plusDays(1), LocalTime.of(10, 0), "Tomorrow Event"));
            bst.insert(createEvent(today.plusDays(2), LocalTime.of(15, 0), "Day After Tomorrow"));
        }

        @Test
        @DisplayName("Should find event by exact title")
        void shouldFindByExactTitle() {
            Event found = bst.searchByTitle("Morning Meeting");

            assertNotNull(found);
            assertEquals("Morning Meeting", found.getTitle());
        }

        @Test
        @DisplayName("Should find event by title case-insensitive")
        void shouldFindByTitleCaseInsensitive() {
            Event found = bst.searchByTitle("MORNING MEETING");

            assertNotNull(found);
            assertEquals("Morning Meeting", found.getTitle());
        }

        @Test
        @DisplayName("Should return null for non-existent title")
        void shouldReturnNullForNonExistentTitle() {
            Event found = bst.searchByTitle("Non-existent");

            assertNull(found);
        }

        @Test
        @DisplayName("Should find events by date")
        void shouldFindEventsByDate() {
            List<Event> events = bst.findEventsByDate(today);

            assertEquals(2, events.size());
        }

        @Test
        @DisplayName("Should find events in date range")
        void shouldFindEventsInRange() {
            List<Event> events = bst.findEventsInRange(today, today.plusDays(1));

            assertEquals(3, events.size());
        }

        @Test
        @DisplayName("Should throw for invalid date range")
        void shouldThrowForInvalidDateRange() {
            assertThrows(InvalidDateRangeException.class,
                    () -> bst.findEventsInRange(today.plusDays(1), today));
        }

        @Test
        @DisplayName("Should find event by ID")
        void shouldFindById() {
            Event original = createEvent(today, LocalTime.of(12, 0), "Find Me");
            bst.insert(original);

            Event found = bst.findById(original.getId());

            assertEquals(original.getTitle(), found.getTitle());
        }

        @Test
        @DisplayName("Should throw EventNotFoundException for non-existent ID")
        void shouldThrowForNonExistentId() {
            assertThrows(EventNotFoundException.class,
                    () -> bst.findById("non-existent-id"));
        }

        @Test
        @DisplayName("Should find events by title containing")
        void shouldFindByTitleContains() {
            List<Event> events = bst.searchByTitleContains("Meeting");

            assertEquals(1, events.size());
            assertTrue(events.get(0).getTitle().contains("Meeting"));
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteTests {

        @BeforeEach
        void addTestEvents() {
            bst.insert(createEvent(today, LocalTime.of(9, 0), "Event A"));
            bst.insert(createEvent(today.minusDays(1), LocalTime.of(10, 0), "Event B"));
            bst.insert(createEvent(today.plusDays(1), LocalTime.of(11, 0), "Event C"));
        }

        @Test
        @DisplayName("Should delete event by title")
        void shouldDeleteByTitle() {
            assertTrue(bst.deleteByTitle("Event A"));
            assertEquals(2, bst.getSize());
            assertNull(bst.searchByTitle("Event A"));
        }

        @Test
        @DisplayName("Should return false for non-existent title")
        void shouldReturnFalseForNonExistentTitle() {
            assertFalse(bst.deleteByTitle("Non-existent"));
            assertEquals(3, bst.getSize());
        }

        @Test
        @DisplayName("Should delete leaf node")
        void shouldDeleteLeafNode() {
            // Event B or C should be a leaf
            bst.deleteByTitle("Event B");
            assertEquals(2, bst.getSize());

            // Remaining events should still be accessible
            assertNotNull(bst.searchByTitle("Event A"));
            assertNotNull(bst.searchByTitle("Event C"));
        }

        @Test
        @DisplayName("Should delete node with one child")
        void shouldDeleteNodeWithOneChild() {
            // Add a node that will be a parent with one child
            bst.insert(createEvent(today.minusDays(2), LocalTime.of(10, 0), "Event D"));

            bst.deleteByTitle("Event B");
            assertEquals(3, bst.getSize());
            assertNotNull(bst.searchByTitle("Event D"));
        }

        @Test
        @DisplayName("Should delete root node")
        void shouldDeleteRootNode() {
            bst.deleteByTitle("Event A"); // Middle date, likely root
            assertEquals(2, bst.getSize());

            List<Event> events = bst.getAllEvents();
            assertEquals(2, events.size());
        }

        @Test
        @DisplayName("Should delete by ID")
        void shouldDeleteById() {
            Event event = createEvent(today, LocalTime.of(20, 0), "Delete Me");
            bst.insert(event);

            assertTrue(bst.deleteById(event.getId()));
            assertEquals(3, bst.getSize());
        }

        @Test
        @DisplayName("Should throw when deleting non-existent ID")
        void shouldThrowForNonExistentId() {
            assertThrows(EventNotFoundException.class,
                    () -> bst.deleteById("non-existent-id"));
        }
    }

    @Nested
    @DisplayName("Traversal Operations")
    class TraversalTests {

        @BeforeEach
        void addTestEvents() {
            // Add events out of order
            bst.insert(createEvent(today.plusDays(2), LocalTime.of(10, 0), "Day 3"));
            bst.insert(createEvent(today, LocalTime.of(10, 0), "Day 1"));
            bst.insert(createEvent(today.plusDays(1), LocalTime.of(10, 0), "Day 2"));
            bst.insert(createEvent(today.minusDays(1), LocalTime.of(10, 0), "Yesterday"));
        }

        @Test
        @DisplayName("Should return all events in chronological order")
        void shouldReturnEventsInOrder() {
            List<Event> events = bst.getAllEvents();

            assertEquals(4, events.size());
            assertEquals("Yesterday", events.get(0).getTitle());
            assertEquals("Day 1", events.get(1).getTitle());
            assertEquals("Day 2", events.get(2).getTitle());
            assertEquals("Day 3", events.get(3).getTitle());
        }

        @Test
        @DisplayName("Should return only upcoming events")
        void shouldReturnUpcomingEvents() {
            List<Event> events = bst.getUpcomingEvents();

            assertEquals(3, events.size());
            // All should be on or after today
            for (Event e : events) {
                assertFalse(e.getDate().isBefore(today));
            }
        }

        @Test
        @DisplayName("Should return today's events")
        void shouldReturnTodaysEvents() {
            List<Event> events = bst.getTodaysEvents();

            assertEquals(1, events.size());
            assertEquals(today, events.get(0).getDate());
        }

        @Test
        @DisplayName("Should return past events")
        void shouldReturnPastEvents() {
            List<Event> events = bst.getPastEvents();

            assertEquals(1, events.size());
            assertEquals("Yesterday", events.get(0).getTitle());
        }
    }

    @Nested
    @DisplayName("Category and Priority Filtering")
    class FilteringTests {

        @BeforeEach
        void addTestEvents() {
            bst.insert(Event.builder()
                    .date(today)
                    .time(LocalTime.of(10, 0))
                    .title("Work Event")
                    .category(EventCategory.WORK)
                    .priority(EventPriority.HIGH)
                    .build());

            bst.insert(Event.builder()
                    .date(today)
                    .time(LocalTime.of(14, 0))
                    .title("Personal Event")
                    .category(EventCategory.PERSONAL)
                    .priority(EventPriority.LOW)
                    .build());

            bst.insert(Event.builder()
                    .date(today)
                    .time(LocalTime.of(18, 0))
                    .title("Another Work Event")
                    .category(EventCategory.WORK)
                    .priority(EventPriority.MEDIUM)
                    .build());
        }

        @Test
        @DisplayName("Should filter by category")
        void shouldFilterByCategory() {
            List<Event> workEvents = bst.findByCategory(EventCategory.WORK);
            List<Event> personalEvents = bst.findByCategory(EventCategory.PERSONAL);

            assertEquals(2, workEvents.size());
            assertEquals(1, personalEvents.size());
        }

        @Test
        @DisplayName("Should filter by priority")
        void shouldFilterByPriority() {
            List<Event> highPriority = bst.findByPriority(EventPriority.HIGH);

            assertEquals(1, highPriority.size());
            assertEquals("Work Event", highPriority.get(0).getTitle());
        }
    }

    @Nested
    @DisplayName("Tree Analysis")
    class TreeAnalysisTests {

        @Test
        @DisplayName("Should return correct height for empty tree")
        void shouldReturnCorrectHeightForEmptyTree() {
            assertEquals(-1, bst.getHeight());
        }

        @Test
        @DisplayName("Should return correct height for single node")
        void shouldReturnCorrectHeightForSingleNode() {
            bst.insert(createEvent(today, LocalTime.of(10, 0), "Only"));
            assertEquals(0, bst.getHeight());
        }

        @Test
        @DisplayName("Should return correct height for multiple nodes")
        void shouldReturnCorrectHeightForMultipleNodes() {
            // Insert in order that creates a known structure
            bst.insert(createEvent(today.plusDays(2), LocalTime.of(10, 0), "Middle"));
            bst.insert(createEvent(today, LocalTime.of(10, 0), "Left"));
            bst.insert(createEvent(today.plusDays(4), LocalTime.of(10, 0), "Right"));

            assertEquals(1, bst.getHeight()); // Balanced with 3 nodes
        }

        @Test
        @DisplayName("Should calculate balance factor")
        void shouldCalculateBalanceFactor() {
            bst.insert(createEvent(today, LocalTime.of(10, 0), "Root"));
            bst.insert(createEvent(today.minusDays(1), LocalTime.of(10, 0), "Left"));

            int balanceFactor = bst.getBalanceFactor();
            assertEquals(1, balanceFactor); // Left-heavy by 1
        }

        @Test
        @DisplayName("Should count leaf nodes")
        void shouldCountLeafNodes() {
            bst.insert(createEvent(today.plusDays(1), LocalTime.of(10, 0), "Root"));
            bst.insert(createEvent(today, LocalTime.of(10, 0), "Left"));
            bst.insert(createEvent(today.plusDays(2), LocalTime.of(10, 0), "Right"));

            assertEquals(2, bst.getLeafCount());
        }

        @Test
        @DisplayName("Should count nodes per level")
        void shouldCountNodesPerLevel() {
            bst.insert(createEvent(today.plusDays(1), LocalTime.of(10, 0), "Root"));
            bst.insert(createEvent(today, LocalTime.of(10, 0), "Left"));
            bst.insert(createEvent(today.plusDays(2), LocalTime.of(10, 0), "Right"));

            Map<Integer, Integer> counts = bst.getNodesPerLevel();

            assertEquals(1, counts.get(0)); // Root level
            assertEquals(2, counts.get(1)); // Children level
        }

        @Test
        @DisplayName("Should detect balanced tree")
        void shouldDetectBalancedTree() {
            bst.insert(createEvent(today.plusDays(1), LocalTime.of(10, 0), "Root"));
            bst.insert(createEvent(today, LocalTime.of(10, 0), "Left"));
            bst.insert(createEvent(today.plusDays(2), LocalTime.of(10, 0), "Right"));

            assertTrue(bst.isBalanced());
        }

        @Test
        @DisplayName("Should detect unbalanced tree")
        void shouldDetectUnbalancedTree() {
            // Insert in ascending order to create unbalanced tree
            bst.insert(createEvent(today, LocalTime.of(10, 0), "Event 1"));
            bst.insert(createEvent(today.plusDays(1), LocalTime.of(10, 0), "Event 2"));
            bst.insert(createEvent(today.plusDays(2), LocalTime.of(10, 0), "Event 3"));
            bst.insert(createEvent(today.plusDays(3), LocalTime.of(10, 0), "Event 4"));

            assertFalse(bst.isBalanced());
        }

        @Test
        @DisplayName("Should generate statistics")
        void shouldGenerateStatistics() {
            bst.insert(createEvent(today, LocalTime.of(10, 0), "Event 1"));
            bst.insert(createEvent(today.plusDays(1), LocalTime.of(10, 0), "Event 2"));

            EventBST.BSTStatistics stats = bst.getStatistics();

            assertEquals(2, stats.totalNodes());
            assertNotNull(stats.toString()); // Should not throw
        }
    }

    @Nested
    @DisplayName("Conflict Detection")
    class ConflictDetectionTests {

        @Test
        @DisplayName("Should find conflicts on a date")
        void shouldFindConflictsOnDate() {
            Event event1 = Event.builder()
                    .date(today)
                    .time(LocalTime.of(10, 0))
                    .durationMinutes(60)
                    .title("Event 1")
                    .build();

            Event event2 = Event.builder()
                    .date(today)
                    .time(LocalTime.of(10, 30))
                    .durationMinutes(60)
                    .title("Event 2")
                    .build();

            // Insert without conflict checking
            bst.insert(event1);
            bst.insert(event2);

            List<Event[]> conflicts = bst.findConflictsOnDate(today);

            assertEquals(1, conflicts.size());
        }

        @Test
        @DisplayName("Should return empty list when no conflicts")
        void shouldReturnEmptyWhenNoConflicts() {
            Event event1 = Event.builder()
                    .date(today)
                    .time(LocalTime.of(10, 0))
                    .durationMinutes(60)
                    .title("Event 1")
                    .build();

            Event event2 = Event.builder()
                    .date(today)
                    .time(LocalTime.of(12, 0))
                    .durationMinutes(60)
                    .title("Event 2")
                    .build();

            bst.insert(event1);
            bst.insert(event2);

            List<Event[]> conflicts = bst.findConflictsOnDate(today);

            assertTrue(conflicts.isEmpty());
        }
    }

    @Nested
    @DisplayName("Utility Operations")
    class UtilityTests {

        @Test
        @DisplayName("Should clear all events")
        void shouldClearAllEvents() {
            bst.insert(createEvent(today, LocalTime.of(10, 0), "Event 1"));
            bst.insert(createEvent(today, LocalTime.of(11, 0), "Event 2"));

            bst.clear();

            assertTrue(bst.isEmpty());
            assertEquals(0, bst.getSize());
        }

        @Test
        @DisplayName("Should track modification count")
        void shouldTrackModificationCount() {
            int initial = bst.getModificationCount();

            bst.insert(createEvent(today, LocalTime.of(10, 0), "Event"));
            bst.deleteByTitle("Event");

            assertEquals(initial + 2, bst.getModificationCount());
        }

        @Test
        @DisplayName("Should generate tree structure string")
        void shouldGenerateTreeStructureString() {
            bst.insert(createEvent(today, LocalTime.of(10, 0), "Root Event"));

            String structure = bst.getTreeStructure();

            assertNotNull(structure);
            assertTrue(structure.contains("Root Event"));
        }
    }

    // Helper method for creating test events
    private Event createEvent(LocalDate date, LocalTime time, String title) {
        return Event.builder()
                .date(date)
                .time(time)
                .title(title)
                .build();
    }
}
