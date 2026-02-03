package com.calendar;

import com.calendar.bst.EventBST;
import com.calendar.exception.EventConflictException;
import com.calendar.exception.EventNotFoundException;
import com.calendar.model.Event;
import com.calendar.model.EventCategory;
import com.calendar.model.EventPriority;
import com.calendar.persistence.CalendarPersistence;
import com.calendar.ui.ConsoleColors;
import com.calendar.ui.InputReader;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Scanner;

public class PersonalCalendar {

    private final EventBST calendar;
    private final InputReader input;
    private final CalendarPersistence persistence;
    private boolean hasUnsavedChanges;

    public PersonalCalendar() {
        this.calendar = new EventBST();
        this.input = new InputReader(new Scanner(System.in));
        this.persistence = new CalendarPersistence();
        this.hasUnsavedChanges = false;
    }

    public static void main(String[] args) {
        PersonalCalendar app = new PersonalCalendar();
        app.run();
    }

    public void run() {
        ConsoleColors.clearScreen();
        printWelcome();
        loadSavedEvents();

        boolean running = true;
        while (running) {
            printMenu();
            int choice = input.readIntInRange("Enter your choice: ", 0, 13);

            switch (choice) {
                case 1 -> addEvent();
                case 2 -> viewAllEvents();
                case 3 -> viewUpcomingEvents();
                case 4 -> viewTodaysEvents();
                case 5 -> searchByTitle();
                case 6 -> searchByDate();
                case 7 -> searchByDateRange();
                case 8 -> searchByCategory();
                case 9 -> deleteEvent();
                case 10 -> viewCalendarStructure();
                case 11 -> viewCalendarStats();
                case 12 -> saveEvents();
                case 13 -> loadEvents();
                case 0 -> running = confirmExit();
            }

            if (running) {
                input.waitForEnter();
            }
        }

        printGoodbye();
        input.close();
    }

    private void printWelcome() {
        System.out.println(ConsoleColors.CYAN_BOLD);
        System.out.println("╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║                                                               ║");
        System.out.println("║   📅  PERSONAL CALENDAR WITH BINARY SEARCH TREE  📅          ║");
        System.out.println("║                                                               ║");
        System.out.println("║          Efficiently organize your events!                    ║");
        System.out.println("║                   Version 2.0                                 ║");
        System.out.println("║                                                               ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════╝");
        System.out.println(ConsoleColors.RESET);
    }

    private void printMenu() {
        System.out.println();
        System.out.println(ConsoleColors.PURPLE_BOLD + "╔═════════════════════════════════════════════╗");
        System.out.println("║              📋 MAIN MENU 📋                ║");
        System.out.println("╠═════════════════════════════════════════════╣");
        System.out.println("║  1.  ➕ Add New Event                       ║");
        System.out.println("║  2.  📋 View All Events                     ║");
        System.out.println("║  3.  🔮 View Upcoming Events                ║");
        System.out.println("║  4.  📆 View Today's Events                 ║");
        System.out.println("║  5.  🔍 Search by Title                     ║");
        System.out.println("║  6.  📅 Search by Date                      ║");
        System.out.println("║  7.  📊 Search by Date Range                ║");
        System.out.println("║  8.  🏷️  Search by Category                 ║");
        System.out.println("║  9.  ❌ Delete Event                        ║");
        System.out.println("║  10. 🌳 View BST Structure                  ║");
        System.out.println("║  11. 📈 View Calendar Statistics            ║");
        System.out.println("║  12. 💾 Save Calendar                       ║");
        System.out.println("║  13. 📂 Load Calendar                       ║");
        System.out.println("║  0.  🚪 Exit                                ║");
        System.out.println("╚═════════════════════════════════════════════╝" + ConsoleColors.RESET);

        if (hasUnsavedChanges) {
            System.out.println(ConsoleColors.YELLOW + "  ⚠️  You have unsaved changes" + ConsoleColors.RESET);
        }
        System.out.println();
    }

    private void addEvent() {
        ConsoleColors.clearScreen();
        ConsoleColors.printHeader("ADD NEW EVENT");

        LocalDate date = input.readDate("Enter date (dd/MM/yyyy): ");
        LocalTime time = input.readTime("Enter time (HH:mm): ");
        int duration = input.readIntInRange("Enter duration in minutes (1-480): ", 1, 480);
        String title = input.readString("Enter event title: ");
        String description = input.readOptionalString("Enter description (optional): ");
        EventCategory category = input.readEnum("Select category:", EventCategory.class);
        EventPriority priority = input.readEnum("Select priority:", EventPriority.class);

        boolean checkConflicts = input.readConfirmation("Check for time conflicts?");

        try {
            Event event = Event.builder()
                    .date(date)
                    .time(time)
                    .durationMinutes(duration)
                    .title(title)
                    .description(description)
                    .category(category)
                    .priority(priority)
                    .build();

            calendar.insert(event, checkConflicts);
            hasUnsavedChanges = true;

            System.out.println(ConsoleColors.success("Event added successfully!"));
            System.out.println();
            System.out.println(event);
        } catch (EventConflictException e) {
            System.out.println(ConsoleColors.error(e.getMessage()));
            System.out.println();
            System.out.println("Conflicting event:");
            System.out.println(e.getExistingEvent());
        } catch (IllegalArgumentException e) {
            System.out.println(ConsoleColors.error(e.getMessage()));
        }
    }

    private void viewAllEvents() {
        ConsoleColors.clearScreen();
        ConsoleColors.printHeader("📋 ALL EVENTS");
        displayEvents(calendar.getAllEvents(), "No events in the calendar.");
    }

    private void viewUpcomingEvents() {
        ConsoleColors.clearScreen();
        ConsoleColors.printHeader("🔮 UPCOMING EVENTS");
        displayEvents(calendar.getUpcomingEvents(), "No upcoming events.");
    }

    private void viewTodaysEvents() {
        ConsoleColors.clearScreen();
        ConsoleColors.printHeader("📆 TODAY'S EVENTS - " + LocalDate.now().format(InputReader.DATE_FORMATTER));
        displayEvents(calendar.getTodaysEvents(), "No events scheduled for today.");
    }

    private void searchByTitle() {
        ConsoleColors.clearScreen();
        ConsoleColors.printHeader("🔍 SEARCH BY TITLE");

        String title = input.readString("Enter title to search: ");
        List<Event> results = calendar.searchByTitleContains(title);

        displayEvents(results, "No events found matching: " + title);
    }

    private void searchByDate() {
        ConsoleColors.clearScreen();
        ConsoleColors.printHeader("📅 SEARCH BY DATE");

        LocalDate date = input.readDate("Enter date (dd/MM/yyyy): ");
        List<Event> results = calendar.findEventsByDate(date);

        displayEvents(results, "No events found on " + date.format(InputReader.DATE_FORMATTER));
    }

    private void searchByDateRange() {
        ConsoleColors.clearScreen();
        ConsoleColors.printHeader("📊 SEARCH BY DATE RANGE");

        LocalDate startDate = input.readDate("Enter start date (dd/MM/yyyy): ");
        LocalDate endDate = input.readDate("Enter end date (dd/MM/yyyy): ");

        try {
            List<Event> results = calendar.findEventsInRange(startDate, endDate);
            displayEvents(results, String.format("No events found between %s and %s",
                    startDate.format(InputReader.DATE_FORMATTER),
                    endDate.format(InputReader.DATE_FORMATTER)));
        } catch (Exception e) {
            System.out.println(ConsoleColors.error(e.getMessage()));
        }
    }

    private void searchByCategory() {
        ConsoleColors.clearScreen();
        ConsoleColors.printHeader("🏷️ SEARCH BY CATEGORY");

        EventCategory category = input.readEnum("Select category:", EventCategory.class);
        List<Event> results = calendar.findByCategory(category);

        displayEvents(results, "No events found in category: " + category.getDisplayName());
    }

    private void deleteEvent() {
        ConsoleColors.clearScreen();
        ConsoleColors.printHeader("❌ DELETE EVENT");

        if (calendar.isEmpty()) {
            System.out.println(ConsoleColors.warning("Calendar is empty. Nothing to delete."));
            return;
        }

        // Show all events with simple format
        System.out.println(ConsoleColors.CYAN + "Current events:" + ConsoleColors.RESET);
        for (Event event : calendar.getAllEvents()) {
            System.out.println("  • " + event.toSimpleString());
        }
        System.out.println();

        String title = input.readString("Enter event title to delete: ");
        Event event = calendar.searchByTitle(title);

        if (event == null) {
            System.out.println(ConsoleColors.error("Event not found: " + title));
            return;
        }

        System.out.println("\nEvent to delete:");
        System.out.println(event);

        if (input.readConfirmation("Are you sure you want to delete this event?")) {
            try {
                calendar.deleteById(event.getId());
                hasUnsavedChanges = true;
                System.out.println(ConsoleColors.success("Event deleted successfully!"));
            } catch (EventNotFoundException e) {
                System.out.println(ConsoleColors.error(e.getMessage()));
            }
        } else {
            System.out.println(ConsoleColors.warning("Deletion cancelled."));
        }
    }

    private void viewCalendarStructure() {
        ConsoleColors.clearScreen();
        ConsoleColors.printHeader("🌳 BST STRUCTURE");

        if (calendar.isEmpty()) {
            System.out.println(ConsoleColors.warning("Calendar is empty."));
            return;
        }

        System.out.println(ConsoleColors.GREEN + "Binary Search Tree visualization:" + ConsoleColors.RESET);
        System.out.println(ConsoleColors.DIM + "(Events are ordered by date/time)" + ConsoleColors.RESET);
        System.out.println();
        calendar.printTree();
    }

    private void viewCalendarStats() {
        ConsoleColors.clearScreen();
        ConsoleColors.printHeader("📈 CALENDAR STATISTICS");

        if (calendar.isEmpty()) {
            System.out.println(ConsoleColors.warning("Calendar is empty. Add some events first!"));
            return;
        }

        System.out.println(calendar.getStatistics());

        System.out.println();
        System.out.println(ConsoleColors.CYAN + "╔══════════════════════════════════════════╗");
        System.out.printf("║  Today's Date:     %-21s ║%n",
                LocalDate.now().format(InputReader.DATE_FORMATTER));
        System.out.printf("║  Upcoming Events:  %-21d ║%n",
                calendar.getUpcomingEvents().size());
        System.out.printf("║  Today's Events:   %-21d ║%n",
                calendar.getTodaysEvents().size());
        System.out.printf("║  Past Events:      %-21d ║%n",
                calendar.getPastEvents().size());
        System.out.println("╚══════════════════════════════════════════╝" + ConsoleColors.RESET);

        List<Event[]> conflicts = calendar.findConflictsOnDate(LocalDate.now());
        if (!conflicts.isEmpty()) {
            System.out.println();
            System.out.println(ConsoleColors.warning("⚠️  Conflicts detected today:"));
            for (Event[] conflict : conflicts) {
                System.out.printf("  • '%s' overlaps with '%s'%n",
                        conflict[0].getTitle(), conflict[1].getTitle());
            }
        }
    }

    private void loadSavedEvents() {
        if (persistence.hasExistingData()) {
            System.out.println(ConsoleColors.info("Found saved calendar data..."));

            if (input.readConfirmation("Load saved events?")) {
                try {
                    List<Event> events = persistence.loadEvents();
                    for (Event event : events) {
                        calendar.insert(event);
                    }
                    System.out.println(ConsoleColors.success(
                            String.format("Loaded %d events from disk.", events.size())));
                } catch (Exception e) {
                    System.out.println(ConsoleColors.error("Failed to load events: " + e.getMessage()));
                }
            } else {
                System.out.println(ConsoleColors.info("Starting with empty calendar."));
            }
        } else {
            System.out.println(ConsoleColors.info("No saved data found. Starting with empty calendar."));
        }
    }

    private void saveEvents() {
        ConsoleColors.clearScreen();
        ConsoleColors.printHeader("💾 SAVE CALENDAR");

        if (calendar.isEmpty()) {
            System.out.println(ConsoleColors.warning("No events to save."));
            return;
        }

        System.out.printf("Saving %d events to: %s%n",
                calendar.getSize(), persistence.getFilePath());

        if (input.readConfirmation("Proceed with save?")) {
            try {
                persistence.saveEvents(calendar.getAllEvents());
                hasUnsavedChanges = false;
                System.out.println(ConsoleColors.success("Calendar saved successfully!"));
            } catch (Exception e) {
                System.out.println(ConsoleColors.error("Failed to save: " + e.getMessage()));
            }
        } else {
            System.out.println(ConsoleColors.warning("Save cancelled."));
        }
    }

    private void loadEvents() {
        ConsoleColors.clearScreen();
        ConsoleColors.printHeader("📂 LOAD CALENDAR");

        if (!persistence.hasExistingData()) {
            System.out.println(ConsoleColors.warning("No saved calendar data found."));
            System.out.printf("Expected file: %s%n", persistence.getFilePath());
            return;
        }

        if (!calendar.isEmpty() && hasUnsavedChanges) {
            System.out.println(ConsoleColors.warning("You have unsaved changes in your current calendar!"));
            if (!input.readConfirmation("Loading will replace current events. Continue?")) {
                System.out.println(ConsoleColors.info("Load cancelled."));
                return;
            }
        } else if (!calendar.isEmpty()) {
            if (!input.readConfirmation("Loading will replace current events. Continue?")) {
                System.out.println(ConsoleColors.info("Load cancelled."));
                return;
            }
        }

        try {
            List<Event> events = persistence.loadEvents();
            calendar.clear();
            for (Event event : events) {
                calendar.insert(event);
            }
            hasUnsavedChanges = false;
            System.out.println(ConsoleColors.success(
                    String.format("Loaded %d events from disk.", events.size())));
        } catch (Exception e) {
            System.out.println(ConsoleColors.error("Failed to load events: " + e.getMessage()));
        }
    }

    private boolean confirmExit() {
        if (hasUnsavedChanges) {
            System.out.println(ConsoleColors.warning("You have unsaved changes!"));
            if (input.readConfirmation("Save before exiting?")) {
                try {
                    persistence.saveEvents(calendar.getAllEvents());
                    System.out.println(ConsoleColors.success("Calendar saved."));
                } catch (Exception e) {
                    System.out.println(ConsoleColors.error("Failed to save: " + e.getMessage()));
                    if (!input.readConfirmation("Exit anyway?")) {
                        return true; // Continue running
                    }
                }
            }
        }
        return false; // Exit
    }

    // ==================== Helper Methods ====================

    private void displayEvents(List<Event> events, String emptyMessage) {
        if (events.isEmpty()) {
            System.out.println(ConsoleColors.warning(emptyMessage));
            return;
        }

        System.out.println(ConsoleColors.GREEN + "Found " + events.size() + " event(s):" + ConsoleColors.RESET);
        System.out.println();

        for (Event event : events) {
            System.out.println(event);
            System.out.println();
        }
    }

    private void printGoodbye() {
        System.out.println();
        System.out.println(ConsoleColors.PURPLE_BOLD);
        System.out.println("╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║                                                               ║");
        System.out.println("║        👋 Thank you for using Personal Calendar! 👋          ║");
        System.out.println("║                                                               ║");
        System.out.println("║              Have a productive day ahead!                     ║");
        System.out.println("║                                                               ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════╝");
        System.out.println(ConsoleColors.RESET);
    }
}
