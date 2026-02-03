package com.calendar.model;

public enum EventCategory {
    WORK("💼", "Work"),
    PERSONAL("👤", "Personal"),
    HEALTH("🏥", "Health"),
    EDUCATION("📚", "Education"),
    SOCIAL("🎉", "Social"),
    TRAVEL("✈️", "Travel"),
    FINANCE("💰", "Finance"),
    OTHER("📌", "Other");

    private final String icon;
    private final String displayName;

    EventCategory(String icon, String displayName) {
        this.icon = icon;
        this.displayName = displayName;
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

    public static EventCategory fromDisplayName(String name) {
        for (EventCategory category : values()) {
            if (category.displayName.equalsIgnoreCase(name)) {
                return category;
            }
        }
        return OTHER;
    }
}
