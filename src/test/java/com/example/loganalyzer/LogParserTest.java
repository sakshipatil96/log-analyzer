package com.example.loganalyzer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class LogParserTest {
    private static final Path SAMPLE_LOG = Path.of("sample-logs", "app.log");

    @Test
    void parsesValidLinesFromSampleLog() {
        // Arrange
        Path logFile = SAMPLE_LOG;

        // Act
        List<LogEntry> entries = assertDoesNotThrow(() -> LogParser.parse(logFile));

        // Assert
        assertEquals(18, entries.size());
        assertEquals(
                new LogEntry(LocalDateTime.of(2026, 9, 17, 8, 5, 12), "INFO", "Application started"),
                entries.get(0));
        assertEquals(
                new LogEntry(LocalDateTime.of(2026, 9, 17, 10, 22, 57), "ERROR", "Connection refused"),
                entries.get(9));
    }

    @Test
    void skipsMalformedLinesWithoutThrowing() {
        // Arrange
        Path logFile = SAMPLE_LOG;

        // Act
        List<LogEntry> entries = assertDoesNotThrow(() -> LogParser.parse(logFile));

        // Assert
        assertEquals(18, entries.size());
        assertEquals(false, entries.stream().anyMatch(entry -> entry.level().equals("DEBUG")));
    }
}
