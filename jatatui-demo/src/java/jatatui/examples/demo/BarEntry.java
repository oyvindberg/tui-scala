package jatatui.examples.demo;

/// A single bar shown in the bar chart on the first tab.
///
/// Replaces upstream `(&amp;str, u64)` (`(label, value)` in `EVENTS`) per the project rule that
/// tuples become dedicated record types.
public record BarEntry(String label, long value) {}
