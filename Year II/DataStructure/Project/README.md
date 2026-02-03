# Personal Calendar with Binary Search Tree 📅

A **professional Java console application** for managing personal calendar events using a Binary Search Tree (BST) data structure. This project demonstrates advanced data structure concepts including BST operations, tree traversal, and algorithm optimization.

## 🎓 Course Information

- **Course:** Data Structures
- **University:** UET
- **Year:** II
- **Version:** 2.0

## 📋 Table of Contents

- [Features](#-features)
- [Project Structure](#-project-structure)
- [Prerequisites](#-prerequisites)
- [Installation & Running](#-installation--running)
- [Usage Guide](#-usage-guide)
- [Technical Documentation](#-technical-documentation)
- [Testing](#-testing)
- [Design Patterns](#-design-patterns)
- [Time Complexity Analysis](#-time-complexity-analysis)

## ✨ Features

### Core Functionality
- ➕ **Add Events** - Create events with date, time, duration, category, and priority
- 📋 **View All Events** - Display events in chronological order (in-order traversal)
- 🔮 **View Upcoming Events** - Filter events from today onwards
- 📆 **View Today's Events** - Quick access to current day's schedule
- 🔍 **Search by Title** - Find events by full or partial title match
- 📅 **Search by Date** - Locate all events on a specific date
- 📊 **Search by Date Range** - Query events within a time period
- 🏷️ **Search by Category** - Filter events by category (Work, Personal, Health, etc.)
- ❌ **Delete Event** - Remove events by title
- 🌳 **View BST Structure** - Visual tree representation
- 📈 **Calendar Statistics** - Detailed BST analysis (height, balance, etc.)
- 💾 **Persistent Storage** - Save/load calendar to JSON file

### Advanced Features
- ⚠️ **Conflict Detection** - Detect overlapping events
- 🔄 **Event Duration** - Support for events with variable duration
- 🎯 **Priority Levels** - Low, Medium, High, Urgent priorities
- 📁 **Event Categories** - Work, Personal, Health, Education, Social, Travel, Finance

## 📁 Project Structure

```
DataStructure/
├── pom.xml                          # Maven build configuration
├── README.md                        # This file
├── src/
│   ├── main/java/com/calendar/
│   │   ├── PersonalCalendar.java    # Main application entry point
│   │   ├── model/
│   │   │   ├── Event.java           # Immutable event with Builder pattern
│   │   │   ├── EventCategory.java   # Event categories enum
│   │   │   └── EventPriority.java   # Priority levels enum
│   │   ├── bst/
│   │   │   ├── EventBST.java        # Binary Search Tree implementation
│   │   │   └── BSTNode.java         # BST node class
│   │   ├── exception/
│   │   │   ├── CalendarException.java
│   │   │   ├── EventNotFoundException.java
│   │   │   ├── EventConflictException.java
│   │   │   ├── InvalidDateRangeException.java
│   │   │   └── PersistenceException.java
│   │   ├── persistence/
│   │   │   └── CalendarPersistence.java  # JSON file storage
│   │   └── ui/
│   │       ├── ConsoleColors.java    # ANSI color utilities
│   │       └── InputReader.java      # Input validation
│   └── test/java/com/calendar/
│       ├── model/
│       │   └── EventTest.java        # Event class unit tests
│       └── bst/
│           └── EventBSTTest.java     # BST unit tests
└── (legacy files - can be removed)
    ├── Event.java
    ├── EventBST.java
    └── PersonalCalendar.java
```

## 🔧 Prerequisites

- **Java JDK 21+** (tested with OpenJDK 25)
- **Maven 3.8+** (for building with tests)
- **macOS/Linux/Windows** terminal with Unicode support

### Verify Java Version
```bash
java --version
# Should show: openjdk 25 or similar
```

## 🚀 Installation & Running

### Option 1: Using Maven (Recommended)

```bash
# Navigate to project directory
cd "/Users/vashi/Documents/Projects/School/uet-assignments/Year I/DataStructure"

# Compile the project
mvn compile

# Run the application
mvn exec:java

# Or build and run JAR
mvn package
java --enable-preview -jar target/personal-calendar-bst-2.0.0.jar
```

### Option 2: Direct Java Compilation

```bash
# Navigate to source directory
cd "/Users/vashi/Documents/Projects/School/uet-assignments/Year I/DataStructure"

# Create output directory
mkdir -p target/classes

# Compile all Java files
javac -d target/classes --enable-preview --release 21 \
  src/main/java/com/calendar/**/*.java \
  src/main/java/com/calendar/*.java

# Run the application
java --enable-preview -cp target/classes com.calendar.PersonalCalendar
```

## 📖 Usage Guide

### Main Menu
```
╔═════════════════════════════════════════════╗
║              📋 MAIN MENU 📋                ║
╠═════════════════════════════════════════════╣
║  1.  ➕ Add New Event                       ║
║  2.  📋 View All Events                     ║
║  3.  🔮 View Upcoming Events                ║
║  4.  📆 View Today's Events                 ║
║  5.  🔍 Search by Title                     ║
║  6.  📅 Search by Date                      ║
║  7.  📊 Search by Date Range                ║
║  8.  🏷️  Search by Category                 ║
║  9.  ❌ Delete Event                        ║
║  10. 🌳 View BST Structure                  ║
║  11. 📈 View Calendar Statistics            ║
║  12. 💾 Save Calendar                       ║
║  0.  🚪 Exit                                ║
╚═════════════════════════════════════════════╝
```

### Adding an Event
1. Select option `1` from the menu
2. Enter date in `dd/MM/yyyy` format (e.g., `26/01/2026`)
3. Enter time in `HH:mm` format (e.g., `14:30`)
4. Enter duration in minutes (1-480)
5. Enter title and description
6. Select category and priority from provided options
7. Choose whether to check for time conflicts

### Viewing BST Structure
The application displays the tree structure showing:
- How events are organized (earlier dates to the left)
- Tree depth and branching
- Visual representation of BST properties

## 📚 Technical Documentation

### Event Class (Immutable)

The `Event` class uses the **Builder Pattern** for flexible object construction:

```java
Event event = Event.builder()
    .date(LocalDate.of(2026, 1, 26))
    .time(LocalTime.of(14, 30))
    .durationMinutes(60)
    .title("Team Meeting")
    .description("Weekly sync")
    .category(EventCategory.WORK)
    .priority(EventPriority.HIGH)
    .build();
```

### BST Ordering

Events are ordered by:
1. **Date** (primary) - Earlier dates to the left
2. **Time** (secondary) - Earlier times to the left
3. **Priority** (tertiary) - Higher priority first

### Key BST Operations

| Operation | Method | Average Case | Worst Case |
|-----------|--------|--------------|------------|
| Insert | `insert(Event)` | O(log n) | O(n) |
| Search by Date | `findEventsByDate(LocalDate)` | O(log n + k) | O(n) |
| Search by Title | `searchByTitle(String)` | O(n) | O(n) |
| Delete | `deleteByTitle(String)` | O(log n) | O(n) |
| Get All (In-order) | `getAllEvents()` | O(n) | O(n) |
| Range Query | `findEventsInRange(start, end)` | O(log n + k) | O(n) |

*Where n = total events, k = events in result*

## 🧪 Testing

### Running Tests with Maven

```bash
# Run all tests
mvn test

# Run tests with verbose output
mvn test -Dsurefire.useSystemClassLoader=false

# Run specific test class
mvn test -Dtest=EventBSTTest
```

### Test Coverage

The test suite covers:
- ✅ Event creation and validation
- ✅ BST insert, search, delete operations
- ✅ Traversal methods (in-order, range queries)
- ✅ Tree analysis (height, balance factor)
- ✅ Conflict detection
- ✅ Edge cases (empty tree, single node)

## 🏗️ Design Patterns

### 1. Builder Pattern (Event class)
- Enables flexible, readable object construction
- Enforces validation at build time
- Supports immutability

### 2. Immutable Objects
- `Event` class is immutable after creation
- Prevents BST corruption from external modifications
- Thread-safe by design

### 3. Custom Exception Hierarchy
```
CalendarException (base)
├── EventNotFoundException
├── EventConflictException
├── InvalidDateRangeException
└── PersistenceException
```

### 4. Record Types (BST Statistics)
Uses Java `record` for immutable data carriers.

## ⏱️ Time Complexity Analysis

### Binary Search Tree Properties

| Scenario | Height | Search/Insert/Delete |
|----------|--------|---------------------|
| Balanced | O(log n) | O(log n) |
| Skewed | O(n) | O(n) |

### Why BST for Calendar?

1. **Chronological ordering** - In-order traversal gives sorted events
2. **Efficient range queries** - Easy to find events in date ranges
3. **Dynamic operations** - Efficient insert/delete without array shifting
4. **Educational value** - Demonstrates core BST concepts

### Potential Improvements

For a self-balancing tree (AVL or Red-Black):
- Guaranteed O(log n) operations
- No worst-case linear complexity
- More complex implementation

## 📄 License

This project is developed for educational purposes as part of the Data Structures course at UET.

## 👨‍💻 Author

- **Student:** [Your Name]
- **Course:** Data Structures - Year I
- **University:** UET

---

*Built with ❤️ and Java*
