package com.calendar.bst;

import com.calendar.exception.EventConflictException;
import com.calendar.exception.EventNotFoundException;
import com.calendar.exception.InvalidDateRangeException;
import com.calendar.model.Event;
import com.calendar.model.EventCategory;
import com.calendar.model.EventPriority;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;

public class EventBST {

    private BSTNode root;
    private int size;
    private int modificationCount;

    public EventBST() {
        this.root = null;
        this.size = 0;
        this.modificationCount = 0;
    }

    public void insert(Event event) {
        Objects.requireNonNull(event, "Event cannot be null");
        root = insertRecursive(root, event);
        size++;
    }

    public void insert(Event event, boolean checkForConflict) {
        if (checkForConflict) {
            List<Event> sameDay = findEventsByDate(event.getDate());
            for (Event existing : sameDay) {
                if (event.overlapsWith(existing)) {
                    throw new EventConflictException(event, existing);
                }
            }
        }
        insert(event);
    }

    private BSTNode insertRecursive(BSTNode node, Event event) {
        if (node == null) {
            return new BSTNode(event);
        }

        int comparison = event.compareTo(node.getEvent());
        if (comparison < 0) {
            node.setLeft(insertRecursive(node.getLeft(), event));
        } else {
            node.setRight(insertRecursive(node.getRight(), event));
        }

        return node;
    }

    public Event findById(String id) {
        Event result = findByIdRecursive(root, id);
        if (result == null) {
            throw new EventNotFoundException("id=" + id);
        }
        return result;
    }

    private Event findByIdRecursive(BSTNode node, String id) {
        if (node == null) {
            return null;
        }

        if (node.getEvent().getId().equals(id)) {
            return node.getEvent();
        }

        Event leftResult = findByIdRecursive(node.getLeft(), id);
        if (leftResult != null) {
            return leftResult;
        }

        return findByIdRecursive(node.getRight(), id);
    }

    public Event searchByTitle(String title) {
        return searchByTitleRecursive(root, title);
    }

    private Event searchByTitleRecursive(BSTNode node, String title) {
        if (node == null) {
            return null;
        }

        if (node.getEvent().getTitle().equalsIgnoreCase(title)) {
            return node.getEvent();
        }

        Event leftResult = searchByTitleRecursive(node.getLeft(), title);
        if (leftResult != null) {
            return leftResult;
        }

        return searchByTitleRecursive(node.getRight(), title);
    }

    public List<Event> searchByTitleContains(String titlePattern) {
        List<Event> results = new ArrayList<>();
        String lowerPattern = titlePattern.toLowerCase();
        findByPredicate(root, results, e -> e.getTitle().toLowerCase().contains(lowerPattern));
        return results;
    }

    public List<Event> findEventsByDate(LocalDate date) {
        Objects.requireNonNull(date, "Date cannot be null");
        List<Event> events = new ArrayList<>();
        findEventsByDateRecursive(root, date, events);
        return events;
    }

    private void findEventsByDateRecursive(BSTNode node, LocalDate date, List<Event> events) {
        if (node == null) {
            return;
        }

        LocalDate eventDate = node.getEvent().getDate();

        if (eventDate.compareTo(date) >= 0) {
            findEventsByDateRecursive(node.getLeft(), date, events);
        }

        if (eventDate.equals(date)) {
            events.add(node.getEvent());
        }

        if (eventDate.compareTo(date) <= 0) {
            findEventsByDateRecursive(node.getRight(), date, events);
        }
    }

    public List<Event> findEventsInRange(LocalDate startDate, LocalDate endDate) {
        Objects.requireNonNull(startDate, "Start date cannot be null");
        Objects.requireNonNull(endDate, "End date cannot be null");

        if (startDate.isAfter(endDate)) {
            throw new InvalidDateRangeException(startDate, endDate);
        }

        List<Event> events = new ArrayList<>();
        findEventsInRangeRecursive(root, startDate, endDate, events);
        return events;
    }

    private void findEventsInRangeRecursive(BSTNode node, LocalDate start, LocalDate end, List<Event> events) {
        if (node == null) {
            return;
        }

        LocalDate eventDate = node.getEvent().getDate();

        if (eventDate.compareTo(start) >= 0) {
            findEventsInRangeRecursive(node.getLeft(), start, end, events);
        }

        if (!eventDate.isBefore(start) && !eventDate.isAfter(end)) {
            events.add(node.getEvent());
        }

        if (eventDate.compareTo(end) <= 0) {
            findEventsInRangeRecursive(node.getRight(), start, end, events);
        }
    }

    public List<Event> findByCategory(EventCategory category) {
        List<Event> results = new ArrayList<>();
        findByPredicate(root, results, e -> e.getCategory() == category);
        return results;
    }

    public List<Event> findByPriority(EventPriority priority) {
        List<Event> results = new ArrayList<>();
        findByPredicate(root, results, e -> e.getPriority() == priority);
        return results;
    }

    private void findByPredicate(BSTNode node, List<Event> results, Predicate<Event> predicate) {
        if (node == null) {
            return;
        }

        findByPredicate(node.getLeft(), results, predicate);

        if (predicate.test(node.getEvent())) {
            results.add(node.getEvent());
        }

        findByPredicate(node.getRight(), results, predicate);
    }

    public boolean deleteById(String id) {
        Event eventToDelete = findById(id); // Throws if not found
        root = deleteRecursive(root, eventToDelete);
        size--;
        modificationCount++;
        return true;
    }

    public boolean deleteByTitle(String title) {
        Event eventToDelete = searchByTitle(title);
        if (eventToDelete == null) {
            return false;
        }
        root = deleteRecursive(root, eventToDelete);
        size--;
        modificationCount++;
        return true;
    }

    private BSTNode deleteRecursive(BSTNode node, Event event) {
        if (node == null) {
            return null;
        }

        int comparison = event.compareTo(node.getEvent());

        if (comparison < 0) {
            node.setLeft(deleteRecursive(node.getLeft(), event));
        } else if (comparison > 0) {
            node.setRight(deleteRecursive(node.getRight(), event));
        } else if (node.getEvent().getId().equals(event.getId())) {

            if (node.getLeft() == null && node.getRight() == null) {
                return null;
            }

            if (node.getLeft() == null) {
                return node.getRight();
            }
            if (node.getRight() == null) {
                return node.getLeft();
            }

            BSTNode successor = findMinNode(node.getRight());
            node.setEvent(successor.getEvent());
            node.setRight(deleteRecursive(node.getRight(), successor.getEvent()));
        } else {
            node.setRight(deleteRecursive(node.getRight(), event));
        }

        return node;
    }

    private BSTNode findMinNode(BSTNode node) {
        while (node.getLeft() != null) {
            node = node.getLeft();
        }
        return node;
    }

    public List<Event> getAllEvents() {
        List<Event> events = new ArrayList<>();
        inOrderTraversal(root, events);
        return events;
    }

    private void inOrderTraversal(BSTNode node, List<Event> events) {
        if (node != null) {
            inOrderTraversal(node.getLeft(), events);
            events.add(node.getEvent());
            inOrderTraversal(node.getRight(), events);
        }
    }

    public List<Event> getUpcomingEvents() {
        LocalDate today = LocalDate.now();
        List<Event> events = new ArrayList<>();
        getUpcomingEventsRecursive(root, today, events);
        return events;
    }

    private void getUpcomingEventsRecursive(BSTNode node, LocalDate today, List<Event> events) {
        if (node == null) {
            return;
        }

        getUpcomingEventsRecursive(node.getLeft(), today, events);

        if (!node.getEvent().getDate().isBefore(today)) {
            events.add(node.getEvent());
        }

        getUpcomingEventsRecursive(node.getRight(), today, events);
    }

    public List<Event> getTodaysEvents() {
        return findEventsByDate(LocalDate.now());
    }

    public List<Event> getPastEvents() {
        LocalDate today = LocalDate.now();
        List<Event> events = new ArrayList<>();
        findByPredicate(root, events, e -> e.getDate().isBefore(today));
        return events;
    }

    public int getSize() {
        return size;
    }

    public boolean isEmpty() {
        return root == null;
    }

    public int getHeight() {
        return getHeightRecursive(root);
    }

    private int getHeightRecursive(BSTNode node) {
        if (node == null) {
            return -1;
        }
        return 1 + Math.max(
                getHeightRecursive(node.getLeft()),
                getHeightRecursive(node.getRight()));
    }

    public int getBalanceFactor() {
        if (root == null) {
            return 0;
        }
        return getHeightRecursive(root.getLeft()) - getHeightRecursive(root.getRight());
    }

    public boolean isBalanced() {
        return isBalancedRecursive(root);
    }

    private boolean isBalancedRecursive(BSTNode node) {
        if (node == null) {
            return true;
        }

        int leftHeight = getHeightRecursive(node.getLeft());
        int rightHeight = getHeightRecursive(node.getRight());
        int balanceFactor = leftHeight - rightHeight;

        if (Math.abs(balanceFactor) > 1) {
            return false;
        }

        return isBalancedRecursive(node.getLeft()) && isBalancedRecursive(node.getRight());
    }

    public int getMinDepth() {
        return getMinDepthRecursive(root);
    }

    private int getMinDepthRecursive(BSTNode node) {
        if (node == null) {
            return -1;
        }
        if (node.getLeft() == null && node.getRight() == null) {
            return 0;
        }
        if (node.getLeft() == null) {
            return 1 + getMinDepthRecursive(node.getRight());
        }
        if (node.getRight() == null) {
            return 1 + getMinDepthRecursive(node.getLeft());
        }
        return 1 + Math.min(
                getMinDepthRecursive(node.getLeft()),
                getMinDepthRecursive(node.getRight()));
    }

    public int getLeafCount() {
        return getLeafCountRecursive(root);
    }

    private int getLeafCountRecursive(BSTNode node) {
        if (node == null) {
            return 0;
        }
        if (node.getLeft() == null && node.getRight() == null) {
            return 1;
        }
        return getLeafCountRecursive(node.getLeft()) + getLeafCountRecursive(node.getRight());
    }

    public Map<Integer, Integer> getNodesPerLevel() {
        Map<Integer, Integer> levelCounts = new TreeMap<>();
        countNodesPerLevel(root, 0, levelCounts);
        return levelCounts;
    }

    private void countNodesPerLevel(BSTNode node, int level, Map<Integer, Integer> counts) {
        if (node == null) {
            return;
        }
        counts.merge(level, 1, Integer::sum);
        countNodesPerLevel(node.getLeft(), level + 1, counts);
        countNodesPerLevel(node.getRight(), level + 1, counts);
    }

    public BSTStatistics getStatistics() {
        return new BSTStatistics(
                size,
                getHeight(),
                getMinDepth(),
                getBalanceFactor(),
                isBalanced(),
                getLeafCount(),
                getNodesPerLevel());
    }

    public void printTree() {
        if (root == null) {
            System.out.println("  (Empty Calendar)");
            return;
        }
        printTreeRecursive(root, "", true);
    }

    private void printTreeRecursive(BSTNode node, String prefix, boolean isLast) {
        if (node != null) {
            System.out.println(prefix + (isLast ? "└── " : "├── ") + node.getEvent().toCompactString());

            boolean hasLeft = node.getLeft() != null;
            boolean hasRight = node.getRight() != null;

            if (hasRight || hasLeft) {
                if (hasRight) {
                    printTreeRecursive(node.getRight(), prefix + (isLast ? "    " : "│   "), !hasLeft);
                }
                if (hasLeft) {
                    printTreeRecursive(node.getLeft(), prefix + (isLast ? "    " : "│   "), true);
                }
            }
        }
    }

    public String getTreeStructure() {
        if (root == null) {
            return "(Empty Calendar)";
        }
        StringBuilder sb = new StringBuilder();
        buildTreeString(root, "", true, sb);
        return sb.toString();
    }

    private void buildTreeString(BSTNode node, String prefix, boolean isLast, StringBuilder sb) {
        if (node != null) {
            sb.append(prefix)
                    .append(isLast ? "└── " : "├── ")
                    .append(node.getEvent().toCompactString())
                    .append("\n");

            boolean hasLeft = node.getLeft() != null;
            boolean hasRight = node.getRight() != null;

            if (hasRight || hasLeft) {
                if (hasRight) {
                    buildTreeString(node.getRight(), prefix + (isLast ? "    " : "│   "), !hasLeft, sb);
                }
                if (hasLeft) {
                    buildTreeString(node.getLeft(), prefix + (isLast ? "    " : "│   "), true, sb);
                }
            }
        }
    }

    public void clear() {
        root = null;
        size = 0;
        modificationCount++;
    }

    public int getModificationCount() {
        return modificationCount;
    }

    public List<Event[]> findConflictsOnDate(LocalDate date) {
        List<Event[]> conflicts = new ArrayList<>();
        List<Event> events = findEventsByDate(date);

        for (int i = 0; i < events.size(); i++) {
            for (int j = i + 1; j < events.size(); j++) {
                if (events.get(i).overlapsWith(events.get(j))) {
                    conflicts.add(new Event[] { events.get(i), events.get(j) });
                }
            }
        }

        return conflicts;
    }

    public record BSTStatistics(
            int totalNodes,
            int height,
            int minDepth,
            int balanceFactor,
            boolean isBalanced,
            int leafCount,
            Map<Integer, Integer> nodesPerLevel) {
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("╔══════════════════════════════════════════╗\n");
            sb.append("║          BST STATISTICS                  ║\n");
            sb.append("╠══════════════════════════════════════════╣\n");
            sb.append(String.format("║  Total Nodes:      %-20d ║%n", totalNodes));
            sb.append(String.format("║  Height:           %-20d ║%n", height));
            sb.append(String.format("║  Min Depth:        %-20d ║%n", minDepth));
            sb.append(String.format("║  Balance Factor:   %-20d ║%n", balanceFactor));
            sb.append(String.format("║  Is Balanced:      %-20s ║%n", isBalanced ? "Yes ✓" : "No ✗"));
            sb.append(String.format("║  Leaf Nodes:       %-20d ║%n", leafCount));
            sb.append("╠══════════════════════════════════════════╣\n");
            sb.append("║  Nodes Per Level:                        ║\n");
            for (Map.Entry<Integer, Integer> entry : nodesPerLevel.entrySet()) {
                sb.append(String.format("║    Level %d: %-27d ║%n", entry.getKey(), entry.getValue()));
            }
            sb.append("╚══════════════════════════════════════════╝\n");
            return sb.toString();
        }
    }
}
