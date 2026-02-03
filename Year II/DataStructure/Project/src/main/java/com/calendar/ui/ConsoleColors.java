package com.calendar.ui;

public final class ConsoleColors {

    public static final String RESET = "\u001B[0m";

    // Regular Colors
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    // Bold Colors
    public static final String BLACK_BOLD = "\u001B[1;30m";
    public static final String RED_BOLD = "\u001B[1;31m";
    public static final String GREEN_BOLD = "\u001B[1;32m";
    public static final String YELLOW_BOLD = "\u001B[1;33m";
    public static final String BLUE_BOLD = "\u001B[1;34m";
    public static final String PURPLE_BOLD = "\u001B[1;35m";
    public static final String CYAN_BOLD = "\u001B[1;36m";
    public static final String WHITE_BOLD = "\u001B[1;37m";

    // Background Colors
    public static final String BLACK_BG = "\u001B[40m";
    public static final String RED_BG = "\u001B[41m";
    public static final String GREEN_BG = "\u001B[42m";
    public static final String YELLOW_BG = "\u001B[43m";
    public static final String BLUE_BG = "\u001B[44m";
    public static final String PURPLE_BG = "\u001B[45m";
    public static final String CYAN_BG = "\u001B[46m";
    public static final String WHITE_BG = "\u001B[47m";

    // Styles
    public static final String BOLD = "\u001B[1m";
    public static final String ITALIC = "\u001B[3m";
    public static final String UNDERLINE = "\u001B[4m";
    public static final String DIM = "\u001B[2m";

    private ConsoleColors() {
    }

    public static String colorize(String text, String color) {
        return color + text + RESET;
    }

    public static String success(String message) {
        return GREEN + "✅ " + message + RESET;
    }

    public static String error(String message) {
        return RED + "❌ " + message + RESET;
    }

    public static String warning(String message) {
        return YELLOW + "⚠️  " + message + RESET;
    }

    public static String info(String message) {
        return CYAN + "ℹ️  " + message + RESET;
    }

    public static void printLine(int length, String color) {
        System.out.println(color + "═".repeat(length) + RESET);
    }

    public static void printHeader(String title) {
        System.out.println();
        System.out.println(CYAN_BOLD + "═".repeat(55) + RESET);
        System.out.println(CYAN_BOLD + "  " + title + RESET);
        System.out.println(CYAN_BOLD + "═".repeat(55) + RESET);
        System.out.println();
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
