package jatatui.widgets.calendar;

import jatatui.core.style.Style;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/// A simple [DateStyler] backed by a [HashMap] from [LocalDate] to [Style].
///
/// Mirrors `ratatui_widgets::calendar::CalendarEventStore` (v0.30).
public final class CalendarEventStore implements DateStyler {

  /// Direct access to the backing map (mirrors the upstream `pub HashMap<Date, Style>` field).
  public final Map<LocalDate, Style> events;

  private CalendarEventStore(Map<LocalDate, Style> events) {
    this.events = events;
  }

  /// Constructs an empty store.
  public static CalendarEventStore empty() {
    return new CalendarEventStore(new HashMap<>(4));
  }

  /// Constructs a store that has the current local date styled with the given style. Mirrors
  /// upstream `CalendarEventStore::today`.
  public static CalendarEventStore today(Style style) {
    CalendarEventStore store = empty();
    store.add(LocalDate.now(), style);
    return store;
  }

  /// Add a date and style to the store. Last write wins.
  public void add(LocalDate date, Style style) {
    events.put(date, style);
  }

  /// Looks up a style for `date`, defaulting to [Style#empty()].
  public Style lookupStyle(LocalDate date) {
    return Optional.ofNullable(events.get(date)).orElse(Style.empty());
  }

  @Override
  public Style getStyle(LocalDate date) {
    return lookupStyle(date);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CalendarEventStore other)) return false;
    return events.equals(other.events);
  }

  @Override
  public int hashCode() {
    return Objects.hash(events);
  }

  @Override
  public String toString() {
    return "CalendarEventStore[events=" + events + "]";
  }
}
