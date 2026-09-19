# Log Analyzer

Log Analyzer is a small Java command-line tool that reads application log files, parses timestamped INFO, WARN, and ERROR entries, and prints a concise operational summary. Its report shows counts by level, the hourly frequency of errors, and the most frequently recurring error message.

## Requirements

- Java 17 or newer
- Maven 3.9 or newer

## Why Streams over loops

The aggregation code uses Java Streams and `Optional` to express filtering, grouping, counting, and the no-error case declaratively. A `cloc` comparison of equivalent reference implementations measured a **49% reduction** in nonblank, non-comment code lines for the Stream version.

| Measurement | Result |
| --- | ---: |
| Stream/Optional reference | 31 code lines |
| Loop/`HashMap` reference | 61 code lines |
| Reduction | `(61 - 31) / 61 = 49.18%` → **49%** |
| Tool | `cloc` 2.10 |

The throwaway references used identical imports, class boilerplate, and method signatures. Both implement level counting, ERROR-only hourly grouping, an empty result when no errors exist, and alphabetical tie-breaking for recurring errors. The loop version uses enhanced `for` loops, `HashMap`, explicit count updates, null checks, and manual top-error selection. This is a comparison of one equivalent aggregation implementation—not a performance benchmark, a universal line-count claim, or a measure of the whole application.

## Usage

Build the executable JAR and analyze the included sample log:

```bash
mvn package && java -jar target/log-analyzer.jar sample-logs/app.log
```

Example stdout output:

```text
Counts by Level
ERROR: 7
INFO: 7
WARN: 4

Error Frequency by Hour
2026-09-17 09:00: 2
2026-09-17 10:00: 2
2026-09-17 11:00: 1
2026-09-17 12:00: 1
2026-09-17 13:00: 1

Top Recurring Error
Connection refused: 4
```

Malformed lines are skipped and reported as warnings on stderr, so they do not pollute the summary written to stdout.

## Input format

Each valid line uses an ISO-8601 local timestamp, one of the supported levels, and a non-empty message:

```text
2026-09-17T10:15:30 ERROR Connection refused
```

Supported levels are `INFO`, `WARN`, and `ERROR`. Blank lines, invalid timestamps, unsupported levels, missing messages, and other malformed lines are skipped with warnings.

## Running tests

```bash
mvn test
```
