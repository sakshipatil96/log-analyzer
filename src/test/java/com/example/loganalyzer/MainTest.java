package com.example.loganalyzer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MainTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void printsAllReportSectionsForAValidFile() {
        // Arrange
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayOutputStream errors = new ByteArrayOutputStream();

        // Act
        int exitCode = Main.run(
                new String[]{Path.of("sample-logs", "app.log").toString()},
                printStream(output),
                printStream(errors));

        // Assert
        assertEquals(0, exitCode);
        String report = output.toString(StandardCharsets.UTF_8);
        assertTrue(report.contains("Counts by Level"));
        assertTrue(report.contains("Error Frequency by Hour"));
        assertTrue(report.contains("Top Recurring Error"));
    }

    @Test
    void printsNoErrorsFoundForAnErrorFreeFile() throws IOException {
        // Arrange
        Path logFile = temporaryDirectory.resolve("no-errors.log");
        Files.writeString(logFile, "2026-09-17T09:00:00 INFO Application started\n");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayOutputStream errors = new ByteArrayOutputStream();

        // Act
        int exitCode = Main.run(
                new String[]{logFile.toString()},
                printStream(output),
                printStream(errors));

        // Assert
        assertEquals(0, exitCode);
        assertTrue(output.toString(StandardCharsets.UTF_8).contains("No errors found"));
    }

    @Test
    void rejectsMissingArgumentsWithUsageGuidance() {
        // Arrange
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayOutputStream errors = new ByteArrayOutputStream();

        // Act
        int exitCode = Main.run(new String[0], printStream(output), printStream(errors));

        // Assert
        assertEquals(2, exitCode);
        assertTrue(errors.toString(StandardCharsets.UTF_8).contains("Usage:"));
    }

    @Test
    void rejectsAnUnreadablePathWithAnErrorMessage() {
        // Arrange
        Path missingLogFile = temporaryDirectory.resolve("missing.log");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayOutputStream errors = new ByteArrayOutputStream();

        // Act
        int exitCode = Main.run(
                new String[]{missingLogFile.toString()},
                printStream(output),
                printStream(errors));

        // Assert
        assertEquals(1, exitCode);
        assertTrue(errors.toString(StandardCharsets.UTF_8).contains("Unable to read log file"));
    }

    private static PrintStream printStream(ByteArrayOutputStream output) {
        return new PrintStream(output, true, StandardCharsets.UTF_8);
    }
}
