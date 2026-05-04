package jatatui.widgets.calendar;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Constraint;
import jatatui.core.layout.HorizontalAlignment;
import jatatui.core.layout.Layout;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.style.Style;
import jatatui.core.text.Line;
import jatatui.core.text.Span;
import jatatui.core.widgets.Widget;
import jatatui.widgets.block.Block;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/// Display a month calendar for the month containing `displayDate`.
///
/// Mirrors `ratatui_widgets::calendar::Monthly` (v0.30) — renamed to `Calendar` to match the
/// task description, but the behaviour is the same.
///
/// Uses [java.time.LocalDate] in place of upstream's `time::Date` from the Rust `time` crate.
public final class Calendar implements Widget {

  private final LocalDate displayDate;
  private final DateStyler events;
  private final Optional<Style> showSurrounding;
  private final Optional<Style> showWeekday;
  private final Optional<Style> showMonth;
  private final Style defaultStyle;
  private final Optional<Block> block;

  private Calendar(
      LocalDate displayDate,
      DateStyler events,
      Optional<Style> showSurrounding,
      Optional<Style> showWeekday,
      Optional<Style> showMonth,
      Style defaultStyle,
      Optional<Block> block) {
    this.displayDate = displayDate;
    this.events = events;
    this.showSurrounding = showSurrounding;
    this.showWeekday = showWeekday;
    this.showMonth = showMonth;
    this.defaultStyle = defaultStyle;
    this.block = block;
  }

  /// Construct a calendar for the `displayDate` and highlight the `events`.
  public static Calendar of(LocalDate displayDate, DateStyler events) {
    return new Calendar(
        displayDate,
        events,
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Style.empty(),
        Optional.empty());
  }

  // ---- Builder methods ----

  /// Fill the calendar slots for days not in the current month also, this causes each line to be
  /// completely filled. If there is an event style for a date, this style will be patched with the
  /// event's style.
  public Calendar withShowSurrounding(Style style) {
    return new Calendar(
        displayDate, events, Optional.of(style), showWeekday, showMonth, defaultStyle, block);
  }

  /// Display a header containing weekday abbreviations.
  public Calendar withShowWeekdaysHeader(Style style) {
    return new Calendar(
        displayDate, events, showSurrounding, Optional.of(style), showMonth, defaultStyle, block);
  }

  /// Display a header containing the month and year.
  public Calendar withShowMonthHeader(Style style) {
    return new Calendar(
        displayDate, events, showSurrounding, showWeekday, Optional.of(style), defaultStyle, block);
  }

  /// How to render otherwise unstyled dates.
  public Calendar withDefaultStyle(Style style) {
    return new Calendar(displayDate, events, showSurrounding, showWeekday, showMonth, style, block);
  }

  /// Render the calendar within a [Block].
  public Calendar withBlock(Block block) {
    return new Calendar(
        displayDate,
        events,
        showSurrounding,
        showWeekday,
        showMonth,
        defaultStyle,
        Optional.of(block));
  }

  // ---- Geometry ----

  /// Returns the width required to render the calendar.
  public int width() {
    int daysPerWeek = 7;
    int gutterWidth = 1;
    int dayWidth = 2;
    int width = daysPerWeek * (gutterWidth + dayWidth);
    if (block.isPresent()) {
      Block.SpacePair sp = block.get().horizontalSpace();
      width += sp.first() + sp.second();
    }
    return width;
  }

  /// Returns the height required to render the calendar.
  public int height() {
    int height = sundayBasedWeeks(displayDate);
    if (showMonth.isPresent()) height++;
    if (showWeekday.isPresent()) height++;
    if (block.isPresent()) {
      Block.SpacePair sp = block.get().verticalSpace();
      height += sp.first() + sp.second();
    }
    return height;
  }

  // ---- Helpers ----

  /// Return a style with only the background from the default style.
  private Style defaultBg() {
    return defaultStyle.bg().map(Calendar::fromBg).orElse(Style.empty());
  }

  private static Style fromBg(Color color) {
    return Style.empty().withBg(color);
  }

  /// Style a date.
  private Span formatDate(LocalDate date) {
    if (date.getMonth() == displayDate.getMonth() && date.getYear() == displayDate.getYear()) {
      return Span.styled(
          formatDay(date.getDayOfMonth()), defaultStyle.patch(events.getStyle(date)));
    }
    if (showSurrounding.isEmpty()) {
      return Span.styled("  ", defaultBg());
    }
    Style merged = defaultStyle.patch(showSurrounding.get()).patch(events.getStyle(date));
    return Span.styled(formatDay(date.getDayOfMonth()), merged);
  }

  /// `format!("{:2?}", n)` — right-aligned two-character field. We mirror `{:2?}` (Debug
  /// `padded`), which for u8 is the decimal value padded with spaces to 2 characters.
  private static String formatDay(int day) {
    return String.format("%2d", day);
  }

  /// Compute how many Sunday-based week rows are needed to render the month containing
  /// `displayDate`. Mirrors upstream `sunday_based_weeks`.
  public static int sundayBasedWeeks(LocalDate displayDate) {
    LocalDate firstOfMonth = displayDate.withDayOfMonth(1);
    LocalDate lastOfMonth = firstOfMonth.withDayOfMonth(firstOfMonth.lengthOfMonth());
    int firstWeek = sundayBasedWeek(firstOfMonth);
    int lastWeek = sundayBasedWeek(lastOfMonth);
    return Math.max(0, lastWeek - firstWeek) + 1;
  }

  /// Replicates `time::Date::sunday_based_week`: the week number where week 0 starts on the
  /// first Sunday of the year (or the start of the year if Jan 1 is Sunday). Days before that
  /// first Sunday are in week 0 as well (clamped at 0).
  private static int sundayBasedWeek(LocalDate date) {
    int dayOfYear = date.getDayOfYear(); // 1-based
    int sundayDayOfWeek = numberDaysFromSunday(date);
    // Compute as in `time` crate: ((dayOfYear - sundayDayOfWeek - 1) / 7) + 1, clamped at 0.
    int n = dayOfYear - sundayDayOfWeek - 1;
    if (n < 0) return 0;
    return n / 7 + 1;
  }

  /// `Weekday::number_days_from_sunday` — Sunday = 0, Monday = 1, ..., Saturday = 6.
  private static int numberDaysFromSunday(LocalDate date) {
    DayOfWeek dow = date.getDayOfWeek();
    // java.time DayOfWeek: Monday=1..Sunday=7. Map to Sunday-based: Sunday=0..Saturday=6.
    if (dow == DayOfWeek.SUNDAY) return 0;
    return dow.getValue();
  }

  // ---- Widget render ----

  @Override
  public void render(Rect area, Buffer buf) {
    block.ifPresent(b -> b.render(area, buf));
    Rect inner = block.map(b -> b.inner(area)).orElse(area);
    renderMonthly(inner, buf);
  }

  private void renderMonthly(Rect area, Buffer buf) {
    Rect[] segs =
        Layout.vertical(
                new Constraint.Length(showMonth.isPresent() ? 1 : 0),
                new Constraint.Length(showWeekday.isPresent() ? 1 : 0),
                new Constraint.Fill(1))
            .areas(area, 3);
    Rect monthHeader = segs[0];
    Rect daysHeader = segs[1];
    Rect daysArea = segs[2];

    // Draw the month name and year.
    if (showMonth.isPresent()) {
      Line line =
          Line.styled(monthName(displayDate) + " " + displayDate.getYear(), showMonth.get())
              .withAlignment(HorizontalAlignment.Center);
      // Inline render: build a single-row area, set style, write the line centered.
      renderLineCentered(line, monthHeader, buf);
    }

    // Draw days of week.
    if (showWeekday.isPresent()) {
      Span span = Span.styled(" Su Mo Tu We Th Fr Sa", showWeekday.get());
      buf.setSpan(daysHeader.left(), daysHeader.top(), span, daysHeader.width());
    }

    // Set the start of the calendar to the Sunday before the 1st (or the Sunday of the first).
    LocalDate firstOfMonth = displayDate.withDayOfMonth(1);
    LocalDate currDay = firstOfMonth.minusDays(numberDaysFromSunday(firstOfMonth));

    int y = daysArea.y();
    // go through all the weeks containing a day in the target month.
    while (!isAfterMonth(currDay, displayDate)) {
      List<Span> spans = new ArrayList<>(14);
      for (int i = 0; i < 7; i++) {
        // Draw the gutter — first col uses default style, rest use the bg-only default.
        if (i == 0) {
          spans.add(Span.styled(" ", Style.empty()));
        } else {
          spans.add(Span.styled(" ", defaultBg()));
        }
        spans.add(formatDate(currDay));
        currDay = currDay.plusDays(1);
      }
      if (buf.area().height() > y) {
        Line line = Line.fromSpans(spans);
        buf.setLine(daysArea.x(), y, line, area.width());
      }
      y += 1;
    }
  }

  /// Returns true when `currDay`'s month/year is after `displayDate`'s month, i.e. when iteration
  /// should stop. Mirrors `curr_day.month() != self.display_date.month().next()` upstream.
  private static boolean isAfterMonth(LocalDate currDay, LocalDate displayDate) {
    LocalDate nextMonthFirst = displayDate.withDayOfMonth(1).plusMonths(1);
    return currDay.getYear() > nextMonthFirst.getYear()
        || (currDay.getYear() == nextMonthFirst.getYear()
            && currDay.getMonthValue() == nextMonthFirst.getMonthValue());
  }

  private static void renderLineCentered(Line line, Rect area, Buffer buf) {
    if (area.isEmpty()) return;
    int width = line.width();
    int areaWidth = area.width();
    int indent = width <= areaWidth ? Math.max(0, areaWidth - width) / 2 : 0;
    Rect target = new Rect(area.x() + indent, area.y(), Math.max(0, areaWidth - indent), 1);
    buf.setLine(target.x(), target.y(), line, target.width());
  }

  /// Replicate the Rust `Display` impl of `time::Month` (full English name).
  private static String monthName(LocalDate date) {
    return switch (date.getMonth()) {
      case JANUARY -> "January";
      case FEBRUARY -> "February";
      case MARCH -> "March";
      case APRIL -> "April";
      case MAY -> "May";
      case JUNE -> "June";
      case JULY -> "July";
      case AUGUST -> "August";
      case SEPTEMBER -> "September";
      case OCTOBER -> "October";
      case NOVEMBER -> "November";
      case DECEMBER -> "December";
    };
  }
}
