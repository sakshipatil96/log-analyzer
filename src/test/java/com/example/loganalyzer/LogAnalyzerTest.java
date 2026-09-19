package com.example.loganalyzer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class LogAnalyzerTest {
    private final LogAnalyzer analyzer = new LogAnalyzer();

    @Test
    void countsEntriesByLevel() {
        // Arrange
        List<LogEntry> entries = sampleEntries();

        // Act
        Map<String, Long> counts = analyzer.countByLevel(entries);

        // Assert
        assertEquals(Map.of("INFO", 1L, "WARN", 1L, "ERROR", 4L), counts);
    }

    @Test
    void groupsOnlyErrorsIntoHourlyBuckets() {
        // Arrange
        List<LogEntry> entries = sampleEntries();
        LocalDateTime tenOClock = LocalDateTime.of(2026, 9, 17, 10, 0);
        LocalDateTime elevenOClock = LocalDateTime.of(2026, 9, 17, 11, 0);

        // Act
        Map<LocalDateTime, Long> frequencies = analyzer.errorFrequencyByHour(entries);

        // Assert
        assertEquals(Map.of(tenOClock, 3L, elevenOClock, 1L), frequencies);
    }

    @Test
    void returnsTheMostFrequentError() {
        // Arrange
        List<LogEntry> entries = sampleEntries();

        // Act
        Optional<Map.Entry<String, Long>> topError = analyzer.topRecurringError(entries);

        // Assert
        assertEquals(Optional.of(Map.entry("Connection refused", 3L)), topError);
    }

    @Test
    void returnsEmptyWhenThereAreNoErrors() {
        // Arrange
        List<LogEntry> entries = List.of(
                entry(9, 10, "INFO", "Started"),
                entry(9, 15, "WARN", "Slow response"));

        // Act
        Optional<Map.Entry<String, Long>> topError = analyzer.topRecurringError(entries);

        // Assert
        assertTrue(topError.isEmpty());
    }

    @Test
    void selectsAlphabeticallyEarliestMessageWhenCountsAreTied() {
        // Arrange
        List<LogEntry> entries = List.of(
                entry(10, 1, "ERROR", "Zebra failure"),
                entry(10, 2, "ERROR", "Alpha failure"));

        // Act
        Optional<Map.Entry<String, Long>> topError = analyzer.topRecurringError(entries);

        // Assert
        assertEquals(Optional.of(Map.entry("Alpha failure", 1L)), topError);
    }

    private static List<LogEntry> sampleEntries() {
        return List.of(
                entry(9, 5, "INFO", "Started"),
                entry(9, 10, "WARN", "Slow response"),
                entry(10, 2, "ERROR", "Connection refused"),
                entry(10, 25, "ERROR", "Connection refused"),
                entry(10, 45, "ERROR", "Request timed out"),
                entry(11, 5, "ERROR", "Connection refused"));
    }

    private static LogEntry entry(int hour, int minute, String level, String message) {
        return new LogEntry(LocalDateTime.of(2026, 9, 17, hour, minute), level, message);
    }
}
