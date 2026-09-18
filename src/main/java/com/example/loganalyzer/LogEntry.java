package com.example.loganalyzer;

import java.time.LocalDateTime;

/**
 * A single successfully parsed application log line.
 */
public record LogEntry(LocalDateTime timestamp, String level, String message) {
}
