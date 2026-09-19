package com.example.loganalyzer;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Command-line entry point for the log analyzer.
 */
public final class Main {
    private static final DateTimeFormatter HOUR_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00");

    private Main() {
    }

    /**
     * Runs the command-line program and returns its process exit code.
     *
     * @param args command-line arguments
     * @param out standard output destination
     * @param err standard error destination
     * @return zero on success, two for invalid arguments, or one for file-reading failures
     */
    public static int run(String[] args, PrintStream out, PrintStream err) {
        if (args.length != 1) {
            err.println("Usage: java -jar log-analyzer.jar <log-file-path>");
            return 2;
        }

        try {
            List<LogEntry> entries = LogParser.parse(Path.of(args[0]));
            printReport(entries, out);
            return 0;
        } catch (IOException | InvalidPathException | SecurityException exception) {
            err.printf("Unable to read log file '%s': %s%n", args[0], exception.getMessage());
            return 1;
        }
    }

    /**
     * Runs the analyzer with the process standard streams and exits with its result.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        System.exit(run(args, System.out, System.err));
    }

    private static void printReport(List<LogEntry> entries, PrintStream out) {
        LogAnalyzer analyzer = new LogAnalyzer();

        out.println("Counts by Level");
        analyzer.countByLevel(entries).entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> out.printf("%s: %d%n", entry.getKey(), entry.getValue()));

        out.println();
        out.println("Error Frequency by Hour");
        analyzer.errorFrequencyByHour(entries).entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> printHourCount(entry, out));

        out.println();
        out.println("Top Recurring Error");
        analyzer.topRecurringError(entries).ifPresentOrElse(
                entry -> out.printf("%s: %d%n", entry.getKey(), entry.getValue()),
                () -> out.println("No errors found"));
    }

    private static void printHourCount(Map.Entry<LocalDateTime, Long> entry, PrintStream out) {
        out.printf("%s: %d%n", HOUR_FORMAT.format(entry.getKey()), entry.getValue());
    }
}
