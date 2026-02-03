package com.calendar.persistence;

import com.calendar.exception.PersistenceException;
import com.calendar.model.Event;
import com.calendar.model.EventCategory;
import com.calendar.model.EventPriority;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CalendarPersistence {

    private static final String DEFAULT_FILENAME = "calendar_events.json";

    private final Path filePath;

    public CalendarPersistence() {
        this(Path.of(System.getProperty("user.home"), ".calendar", DEFAULT_FILENAME));
    }

    public CalendarPersistence(Path filePath) {
        this.filePath = filePath;
    }

    public void saveEvents(List<Event> events) {
        try {
            Path parent = filePath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }

            String json = eventsToJson(events);
            Files.writeString(filePath, json);
        } catch (IOException e) {
            throw new PersistenceException("Failed to save events", filePath.toString(), e);
        }
    }

    public List<Event> loadEvents() {
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }

        try {
            String json = Files.readString(filePath);
            return eventsFromJson(json);
        } catch (IOException e) {
            throw new PersistenceException("Failed to load events", filePath.toString(), e);
        }
    }

    public boolean hasExistingData() {
        return Files.exists(filePath);
    }

    public Path getFilePath() {
        return filePath;
    }

    public void deleteData() {
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new PersistenceException("Failed to delete data", filePath.toString(), e);
        }
    }

    private String eventsToJson(List<Event> events) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"version\": \"2.0\",\n");
        sb.append("  \"exportedAt\": \"").append(LocalDateTime.now()).append("\",\n");
        sb.append("  \"events\": [\n");

        for (int i = 0; i < events.size(); i++) {
            Event e = events.get(i);
            sb.append("    {\n");
            sb.append("      \"id\": \"").append(escapeJson(e.getId())).append("\",\n");
            sb.append("      \"date\": \"").append(e.getDate()).append("\",\n");
            sb.append("      \"time\": \"").append(e.getTime()).append("\",\n");
            sb.append("      \"durationMinutes\": ").append(e.getDuration().toMinutes()).append(",\n");
            sb.append("      \"title\": \"").append(escapeJson(e.getTitle())).append("\",\n");
            sb.append("      \"description\": \"").append(escapeJson(e.getDescription())).append("\",\n");
            sb.append("      \"category\": \"").append(e.getCategory().name()).append("\",\n");
            sb.append("      \"priority\": \"").append(e.getPriority().name()).append("\",\n");
            sb.append("      \"createdAt\": \"").append(e.getCreatedAt()).append("\"\n");
            sb.append("    }");
            if (i < events.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }

        sb.append("  ]\n");
        sb.append("}\n");
        return sb.toString();
    }

    private List<Event> eventsFromJson(String json) {
        List<Event> events = new ArrayList<>();

        Pattern eventPattern = Pattern.compile(
                "\\{[^{}]*\"id\"\\s*:\\s*\"([^\"]*)\"[^{}]*" +
                        "\"date\"\\s*:\\s*\"([^\"]*)\"[^{}]*" +
                        "\"time\"\\s*:\\s*\"([^\"]*)\"[^{}]*" +
                        "\"durationMinutes\"\\s*:\\s*(\\d+)[^{}]*" +
                        "\"title\"\\s*:\\s*\"([^\"]*)\"[^{}]*" +
                        "\"description\"\\s*:\\s*\"([^\"]*)\"[^{}]*" +
                        "\"category\"\\s*:\\s*\"([^\"]*)\"[^{}]*" +
                        "\"priority\"\\s*:\\s*\"([^\"]*)\"[^{}]*" +
                        "\"createdAt\"\\s*:\\s*\"([^\"]*)\"[^{}]*\\}",
                Pattern.DOTALL);

        Matcher matcher = eventPattern.matcher(json);

        while (matcher.find()) {
            try {
                Event event = Event.builder()
                        .id(unescapeJson(matcher.group(1)))
                        .date(LocalDate.parse(matcher.group(2)))
                        .time(LocalTime.parse(matcher.group(3)))
                        .durationMinutes(Integer.parseInt(matcher.group(4)))
                        .title(unescapeJson(matcher.group(5)))
                        .description(unescapeJson(matcher.group(6)))
                        .category(EventCategory.valueOf(matcher.group(7)))
                        .priority(EventPriority.valueOf(matcher.group(8)))
                        .createdAt(LocalDateTime.parse(matcher.group(9)))
                        .build();
                events.add(event);
            } catch (Exception e) {
                // Skip malformed events
                System.err.println("Warning: Skipping malformed event: " + e.getMessage());
            }
        }

        return events;
    }

    private String escapeJson(String text) {
        if (text == null)
            return "";
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String unescapeJson(String text) {
        if (text == null)
            return "";
        return text
                .replace("\\\"", "\"")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\\", "\\");
    }
}
