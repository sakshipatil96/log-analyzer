package com.example.loganalyzer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Parses application logs in the format {@code timestamp level message}.
 */
public final class LogParser {
    private static final Logger LOGGER = Logger.getLogger(LogParser.class.getName());
    private static final Pattern LOG_LINE_PATTERN = Pattern.compile(
            "^(?<timestamp>\\S+)\\s+(?<level>\\S+)\\s+(?<message>.*)$");
    private static final Set<String> SUPPORTED_LEVELS = Set.of("INFO", "WARN", "ERROR");

    private LogParser() {
    }

    /**
     * Parses valid log lines from a file and skips malformed lines with a warning.
     *
     * @param file the log file to read
     * @return parsed log entries in file order
     * @throws IOException if the file cannot be read
     */
    public static List<LogEntry> parse(Path file) throws IOException {
        try (Stream<String> lines = Files.lines(file, StandardCharsets.UTF_8)) {
            return lines
                    .map(LogParser::parseLine)
                    .flatMap(Optional::stream)
                    .collect(Collectors.toList());
        }
    }

    private static Optional<LogEntry> parseLine(String line) {
        String trimmedLine = line.trim();
        Matcher matcher = LOG_LINE_PATTERN.matcher(trimmedLine);

        if (!matcher.matches()) {
            warnAndSkip(line, "does not match the expected format");
            return Optional.empty();
        }

        String level = matcher.group("level");
        String message = matcher.group("message").trim();
        if (!SUPPORTED_LEVELS.contains(level)) {
            warnAndSkip(line, "uses an unsupported log level");
            return Optional.empty();
        }
        if (message.isEmpty()) {
            warnAndSkip(line, "has no message");
            return Optional.empty();
        }

        try {
            return Optional.of(new LogEntry(
                    LocalDateTime.parse(matcher.group("timestamp")),
                    level,
                    message));
        } catch (DateTimeParseException exception) {
            warnAndSkip(line, "has an invalid timestamp");
            return Optional.empty();
        }
    }

    private static void warnAndSkip(String line, String reason) {
        LOGGER.warning(() -> "Skipping log line because it " + reason + ": " + line);
    }
}
