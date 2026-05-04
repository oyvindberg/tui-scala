package jatatui.examples.demo;

/// A single log entry shown on the first tab of the demo.
///
/// Replaces upstream `(&amp;str, &amp;str)` (`(event, level)` in `LOGS`) per the project rule that
/// tuples become dedicated record types.
public record LogEntry(String event, String level) {}
