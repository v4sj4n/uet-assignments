package com.calendar.model;

public enum EventPriority {
    LOW(1, "🟢", "Low"),
    MEDIUM(2, "🟡", "Medium"),
    HIGH(3, "🟠", "High"),
    URGENT(4, "🔴", "Urgent");

    private final int level;
    private final String icon;
    private final String displayName;

    EventPriority(int level, String icon, String displayName) {
        this.level = level;
        this.icon = icon;
        this.displayName = displayName;
    }

    public int getLevel() {
        return level;
    }

    public String getIcon() {
        return icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return icon + " " + displayName;
    }

    public static EventPriority fromDisplayName(String name) {
        for (EventPriority priority : values()) {
            if (priority.displayName.equalsIgnoreCase(name)) {
                return priority;
            }
        }
        return MEDIUM;
    }
}
