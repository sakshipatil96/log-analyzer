package com.example.loganalyzer;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Produces aggregate statistics from parsed log entries.
 */
public final class LogAnalyzer {

    /**
     * Groups every entry by level and counts the entries in each group.
     *
     * @param entries entries to analyze
     * @return a count for each encountered log level
     */
    public Map<String, Long> countByLevel(List<LogEntry> entries) {
        return entries.stream()
                .collect(Collectors.groupingBy(LogEntry::level, Collectors.counting()));
    }

    /**
     * Filters to errors, truncates their timestamps to an hour, and counts each hour bucket.
     *
     * @param entries entries to analyze
     * @return error counts grouped by the hour in which they occurred
     */
    public Map<LocalDateTime, Long> errorFrequencyByHour(List<LogEntry> entries) {
        return entries.stream()
                .filter(entry -> "ERROR".equals(entry.level()))
                .collect(Collectors.groupingBy(
                        entry -> entry.timestamp().truncatedTo(ChronoUnit.HOURS),
                        Collectors.counting()));
    }

    /**
     * Groups error messages, counts their occurrences, and selects the most frequent message.
     *
     * <p>When counts are equal, the alphabetically earliest message is selected.</p>
     *
     * @param entries entries to analyze
     * @return the most frequent error message and its count, or an empty Optional when no errors exist
     */
    public Optional<Map.Entry<String, Long>> topRecurringError(List<LogEntry> entries) {
        return entries.stream()
                .filter(entry -> "ERROR".equals(entry.level()))
                .collect(Collectors.groupingBy(LogEntry::message, Collectors.counting()))
                .entrySet()
                .stream()
                .max(Comparator.<Map.Entry<String, Long>, Long>comparing(Map.Entry::getValue)
                        .thenComparing(Map.Entry::getKey, Comparator.reverseOrder()));
    }
}
