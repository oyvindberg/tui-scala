package jatatui.widgets.calendar;

import jatatui.core.style.Style;
import java.time.LocalDate;

/// Provides a method for styling a given date.
///
/// Mirrors `ratatui_widgets::calendar::DateStyler` (v0.30). [Calendar] is generic on this trait,
/// so any type that implements this interface can be used.
@FunctionalInterface
public interface DateStyler {
  /// Given a date, return a style for that date.
  Style getStyle(LocalDate date);
}
