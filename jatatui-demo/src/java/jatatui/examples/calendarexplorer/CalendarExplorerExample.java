package jatatui.examples.calendarexplorer;

import jatatui.core.layout.Constraint;
import jatatui.core.layout.Layout;
import jatatui.core.layout.Margin;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import jatatui.core.terminal.Frame;
import jatatui.core.text.Line;
import jatatui.core.text.Text;
import jatatui.crossterm.Jatatui;
import jatatui.widgets.calendar.Calendar;
import jatatui.widgets.calendar.CalendarEventStore;
import jatatui.widgets.paragraph.Paragraph;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Event;
import tui.crossterm.KeyCode;
import tui.crossterm.KeyEventKind;

/// A jatatui example that demonstrates how to render calendars with different styles.
///
/// Marks the holidays and seasons on the calendar.
///
/// Java port of `examples/apps/calendar-explorer/src/main.rs` from ratatui v0.30.
public final class CalendarExplorerExample {

  private static final CrosstermJni JNI = new CrosstermJni();

  private CalendarExplorerExample() {}

  public static void main(String[] args) throws IOException {
    Jatatui.runIo(terminal -> {
      LocalDate selectedDate = LocalDate.now();
      StyledCalendar calendarStyle = StyledCalendar.Default;
      while (true) {
        StyledCalendar finalCalendarStyle = calendarStyle;
        LocalDate finalSelectedDate = selectedDate;
        terminal.draw(frame -> render(frame, finalCalendarStyle, finalSelectedDate));
        Event event = JNI.read();
        if (!(event instanceof Event.Key key)
            || key.keyEvent().kind() != KeyEventKind.Press) {
          continue;
        }
        KeyCode code = key.keyEvent().code();
        if (code instanceof KeyCode.Char ch) {
          char c = ch.c();
          if (c == 'q') {
            return;
          } else if (c == 's') {
            calendarStyle = calendarStyle.next();
          } else if (c == 'n') {
            selectedDate = nextMonth(selectedDate);
          } else if (c == 'p') {
            selectedDate = prevMonth(selectedDate);
          } else if (c == 'h') {
            selectedDate = selectedDate.minusDays(1);
          } else if (c == 'j') {
            selectedDate = selectedDate.plusWeeks(1);
          } else if (c == 'k') {
            selectedDate = selectedDate.minusWeeks(1);
          } else if (c == 'l') {
            selectedDate = selectedDate.plusDays(1);
          }
        } else if (code instanceof KeyCode.Tab) {
          selectedDate = nextMonth(selectedDate);
        } else if (code instanceof KeyCode.BackTab) {
          selectedDate = prevMonth(selectedDate);
        } else if (code instanceof KeyCode.Left) {
          selectedDate = selectedDate.minusDays(1);
        } else if (code instanceof KeyCode.Down) {
          selectedDate = selectedDate.plusWeeks(1);
        } else if (code instanceof KeyCode.Up) {
          selectedDate = selectedDate.minusWeeks(1);
        } else if (code instanceof KeyCode.Right) {
          selectedDate = selectedDate.plusDays(1);
        }
      }
    });
  }

  private static LocalDate nextMonth(LocalDate date) {
    if (date.getMonth() == Month.DECEMBER) {
      return date.withMonth(Month.JANUARY.getValue()).withYear(date.getYear() + 1);
    }
    return date.withMonth(date.getMonth().plus(1).getValue());
  }

  private static LocalDate prevMonth(LocalDate date) {
    if (date.getMonth() == Month.JANUARY) {
      return date.withMonth(Month.DECEMBER.getValue()).withYear(date.getYear() - 1);
    }
    return date.withMonth(date.getMonth().minus(1).getValue());
  }

  /// Render the UI with a calendar.
  private static void render(Frame frame, StyledCalendar calendarStyle, LocalDate selectedDate) {
    List<Line> headerLines = new ArrayList<>(3);
    headerLines.add(Line.styled("Calendar Example", Style.empty().bold()));
    headerLines.add(Line.from(
        "<q> Quit | <s> Change Style | <n> Next Month | <p> Previous Month, <hjkl> Move"));
    headerLines.add(Line.from(
        "Current date: " + selectedDate + " | Current style: " + calendarStyle));
    Text header = Text.fromLines(headerLines);

    Rect[] rows = Layout.vertical(
        new Constraint.Length(header.height()),
        new Constraint.Fill(1)).split(frame.area());
    Rect textArea = rows[0];
    Rect area = rows[1];
    frame.renderWidget(Paragraph.of(header.centered()), textArea);
    calendarStyle.renderYear(frame, area, selectedDate);
  }

  // ---- StyledCalendar ----

  private enum StyledCalendar {
    Default,
    Surrounding,
    WeekdaysHeader,
    SurroundingAndWeekdaysHeader,
    MonthHeader,
    MonthAndWeekdaysHeader;

    /// Cycle through the different styles.
    StyledCalendar next() {
      return switch (this) {
        case Default -> Surrounding;
        case Surrounding -> WeekdaysHeader;
        case WeekdaysHeader -> SurroundingAndWeekdaysHeader;
        case SurroundingAndWeekdaysHeader -> MonthHeader;
        case MonthHeader -> MonthAndWeekdaysHeader;
        case MonthAndWeekdaysHeader -> Default;
      };
    }

    @Override
    public String toString() {
      return switch (this) {
        case Default -> "Default";
        case Surrounding -> "Show Surrounding";
        case WeekdaysHeader -> "Show Weekdays Header";
        case SurroundingAndWeekdaysHeader -> "Show Surrounding and Weekdays Header";
        case MonthHeader -> "Show Month Header";
        case MonthAndWeekdaysHeader -> "Show Month Header and Weekdays Header";
      };
    }

    void renderYear(Frame frame, Rect area, LocalDate date) {
      CalendarEventStore events = events(date);

      Layout vertical = Layout.vertical(
          new Constraint.Ratio(1, 3),
          new Constraint.Ratio(1, 3),
          new Constraint.Ratio(1, 3));
      Layout horizontal = Layout.horizontal(
          new Constraint.Ratio(1, 4),
          new Constraint.Ratio(1, 4),
          new Constraint.Ratio(1, 4),
          new Constraint.Ratio(1, 4));

      List<Rect> areas = new ArrayList<>(12);
      for (Rect row : area.inner(new Margin(1, 1)).layoutVec(vertical)) {
        areas.addAll(row.layoutVec(horizontal));
      }
      for (int i = 0; i < areas.size() && i < 12; i++) {
        LocalDate month = date.withDayOfMonth(1).withMonth(i + 1);
        renderMonth(frame, areas.get(i), month, events);
      }
    }

    void renderMonth(Frame frame, Rect area, LocalDate date, CalendarEventStore events) {
      Style defaultStyle = Style.empty().bold().withBg(new Color.Rgb(50, 50, 50));
      Style empty = Style.empty();
      Calendar calendar = switch (this) {
        case Default -> Calendar.of(date, events)
            .withDefaultStyle(defaultStyle)
            .withShowMonthHeader(empty);
        case Surrounding -> Calendar.of(date, events)
            .withDefaultStyle(defaultStyle)
            .withShowMonthHeader(empty)
            .withShowSurrounding(Style.empty().dim());
        case WeekdaysHeader -> Calendar.of(date, events)
            .withDefaultStyle(defaultStyle)
            .withShowMonthHeader(empty)
            .withShowWeekdaysHeader(Style.empty().bold().green());
        case SurroundingAndWeekdaysHeader -> Calendar.of(date, events)
            .withDefaultStyle(defaultStyle)
            .withShowMonthHeader(empty)
            .withShowSurrounding(Style.empty().dim())
            .withShowWeekdaysHeader(Style.empty().bold().green());
        case MonthHeader -> Calendar.of(date, events)
            .withDefaultStyle(defaultStyle)
            // Upstream sets show_month_header twice (default then bold green); the second wins.
            .withShowMonthHeader(Style.empty().bold().green());
        case MonthAndWeekdaysHeader -> Calendar.of(date, events)
            .withDefaultStyle(defaultStyle)
            .withShowMonthHeader(empty)
            .withShowWeekdaysHeader(Style.empty().bold().dim().lightYellow());
      };
      frame.renderWidget(calendar, area);
    }
  }

  /// Makes a list of dates for the current year.
  private static CalendarEventStore events(LocalDate selectedDate) {
    Style selected = Style.empty().withFg(Color.WHITE).withBg(Color.RED).withAddModifier(Modifier.BOLD);
    Style holiday = Style.empty().withFg(Color.RED).withAddModifier(Modifier.UNDERLINED);
    Style season = Style.empty().withFg(Color.GREEN).withBg(Color.BLACK).withAddModifier(Modifier.UNDERLINED);

    CalendarEventStore list = CalendarEventStore.today(
        Style.empty().withAddModifier(Modifier.BOLD).withBg(Color.BLUE));
    int y = selectedDate.getYear();

    // new year's
    list.add(LocalDate.of(y, Month.JANUARY, 1), holiday);
    // next new_year's for December "show surrounding"
    list.add(LocalDate.of(y + 1, Month.JANUARY, 1), holiday);
    // groundhog day
    list.add(LocalDate.of(y, Month.FEBRUARY, 2), holiday);
    // april fool's
    list.add(LocalDate.of(y, Month.APRIL, 1), holiday);
    // earth day
    list.add(LocalDate.of(y, Month.APRIL, 22), holiday);
    // star wars day
    list.add(LocalDate.of(y, Month.MAY, 4), holiday);
    // festivus
    list.add(LocalDate.of(y, Month.DECEMBER, 23), holiday);
    // new year's eve
    list.add(LocalDate.of(y, Month.DECEMBER, 31), holiday);

    // seasons
    list.add(LocalDate.of(y, Month.MARCH, 22), season);
    list.add(LocalDate.of(y, Month.JUNE, 21), season);
    list.add(LocalDate.of(y, Month.SEPTEMBER, 22), season);
    list.add(LocalDate.of(y, Month.DECEMBER, 21), season);

    // selected date
    list.add(selectedDate, selected);
    return list;
  }

}
