package com.calendar.ui;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class InputReader {

    private final Scanner scanner;

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public InputReader(Scanner scanner) {
        this.scanner = scanner;
    }

    public int readInt(String prompt) {
        while (true) {
            System.out.print(ConsoleColors.YELLOW + prompt + ConsoleColors.RESET);
            try {
                String input = scanner.nextLine().trim();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println(ConsoleColors.error("Please enter a valid number."));
            }
        }
    }

    public int readIntInRange(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt);
            if (value >= min && value <= max) {
                return value;
            }
            System.out.println(ConsoleColors.error(
                    String.format("Please enter a number between %d and %d.", min, max)));
        }
    }

    public String readString(String prompt) {
        while (true) {
            System.out.print(ConsoleColors.YELLOW + prompt + ConsoleColors.RESET);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println(ConsoleColors.error("Input cannot be empty."));
        }
    }

    public String readOptionalString(String prompt) {
        System.out.print(ConsoleColors.YELLOW + prompt + ConsoleColors.RESET);
        return scanner.nextLine().trim();
    }

    public LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(ConsoleColors.YELLOW + prompt + ConsoleColors.RESET);
            try {
                String input = scanner.nextLine().trim();
                return LocalDate.parse(input, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println(ConsoleColors.error(
                        "Invalid date format. Please use dd/MM/yyyy (e.g., 26/01/2026)"));
            }
        }
    }

    public LocalTime readTime(String prompt) {
        while (true) {
            System.out.print(ConsoleColors.YELLOW + prompt + ConsoleColors.RESET);
            try {
                String input = scanner.nextLine().trim();
                return LocalTime.parse(input, TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println(ConsoleColors.error(
                        "Invalid time format. Please use HH:mm (e.g., 14:30)"));
            }
        }
    }

    public int readDurationMinutes(String prompt) {
        while (true) {
            int minutes = readInt(prompt);
            if (minutes > 0 && minutes <= 1440) { // Max 24 hours
                return minutes;
            }
            System.out.println(ConsoleColors.error(
                    "Duration must be between 1 and 1440 minutes (24 hours)."));
        }
    }

    public boolean readConfirmation(String prompt) {
        while (true) {
            System.out.print(ConsoleColors.YELLOW + prompt + " (y/n): " + ConsoleColors.RESET);
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("y") || input.equals("yes")) {
                return true;
            }
            if (input.equals("n") || input.equals("no")) {
                return false;
            }
            System.out.println(ConsoleColors.error("Please enter 'y' for yes or 'n' for no."));
        }
    }

    public <T extends Enum<T>> T readEnum(String prompt, Class<T> enumClass) {
        T[] values = enumClass.getEnumConstants();

        System.out.println(ConsoleColors.CYAN + prompt + ConsoleColors.RESET);
        for (int i = 0; i < values.length; i++) {
            System.out.printf("  %d. %s%n", i + 1, values[i].toString());
        }

        int choice = readIntInRange("Enter choice: ", 1, values.length);
        return values[choice - 1];
    }

    public void waitForEnter() {
        System.out.print(ConsoleColors.CYAN + "\nPress Enter to continue..." + ConsoleColors.RESET);
        scanner.nextLine();
    }

    public void close() {
        scanner.close();
    }
}
