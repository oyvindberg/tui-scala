package jatatui.tests.widgets.calendar;

import static jatatui.tests._support.BufferAssertions.assertBufferEq;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.style.Style;
import jatatui.widgets.block.Block;
import jatatui.widgets.block.Padding;
import jatatui.widgets.calendar.Calendar;
import jatatui.widgets.calendar.CalendarEventStore;
import java.time.LocalDate;
import java.time.Month;
import org.junit.jupiter.api.Test;

public class CalendarTest {

  @Test
  public void event_store() {
    LocalDate aDate = LocalDate.of(2023, Month.JANUARY, 1);
    Style aStyle = Style.empty();
    LocalDate bDate = LocalDate.of(2023, Month.JANUARY, 2);
    Style bStyle = Style.empty().withBg(Color.RED).withFg(Color.BLUE);
    CalendarEventStore s = CalendarEventStore.empty();
    s.add(bDate, bStyle);

    assertEquals(
        aStyle, s.getStyle(aDate), "Date not added to the styler should look up as Style::empty()");
    assertEquals(
        bStyle, s.getStyle(bDate), "Date added to styler should return the provided style");
  }

  @Test
  public void test_today() {
    // Should not throw.
    CalendarEventStore.today(Style.empty());
  }

  @Test
  public void render_in_minimal_buffer() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 1, 1));
    Calendar calendar =
        Calendar.of(LocalDate.of(1984, Month.JANUARY, 1), CalendarEventStore.empty());
    // Should not throw.
    calendar.render(buf.area(), buf);
    assertBufferEq(buf, Buffer.withLines(" "));
  }

  @Test
  public void render_in_zero_size_buffer() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 0, 0));
    Calendar calendar =
        Calendar.of(LocalDate.of(1984, Month.JANUARY, 1), CalendarEventStore.empty());
    // Should not throw.
    calendar.render(buf.area(), buf);
  }

  @Test
  public void calendar_width_reflects_grid_layout() {
    LocalDate date = LocalDate.of(2023, Month.JANUARY, 1);
    Calendar calendar = Calendar.of(date, CalendarEventStore.empty());
    assertEquals(21, calendar.width());
  }

  @Test
  public void calendar_height_counts_weeks_and_headers() {
    LocalDate date = LocalDate.of(2015, Month.FEBRUARY, 1);
    Calendar baseCalendar = Calendar.of(date, CalendarEventStore.empty());
    assertEquals(4, baseCalendar.height());

    Calendar decoratedCalendar =
        Calendar.of(date, CalendarEventStore.empty())
            .withShowMonthHeader(Style.empty())
            .withShowWeekdaysHeader(Style.empty());
    assertEquals(6, decoratedCalendar.height());
  }

  @Test
  public void calendar_dimensions_examples() {
    // Feb 2015 starts Sunday and spans 4 rows.
    LocalDate feb2015 = LocalDate.of(2015, Month.FEBRUARY, 1);
    Calendar cal = Calendar.of(feb2015, CalendarEventStore.empty());
    assertEquals(21, cal.width(), "4w base width");
    assertEquals(4, cal.height(), "Feb 2015 rows");

    cal =
        Calendar.of(feb2015, CalendarEventStore.empty())
            .withShowMonthHeader(Style.empty())
            .withShowWeekdaysHeader(Style.empty());
    assertEquals(6, cal.height(), "Headers add 2 rows");

    Block block = Block.bordered().withPadding(new Padding(2, 3, 1, 2));
    cal = Calendar.of(feb2015, CalendarEventStore.empty()).withBlock(block);
    assertEquals(28, cal.width(), "Padding widens width");
    assertEquals(9, cal.height(), "Padding grows height");

    // Feb 2024 starts Thursday and spans 5 rows.
    LocalDate feb2024 = LocalDate.of(2024, Month.FEBRUARY, 1);
    cal = Calendar.of(feb2024, CalendarEventStore.empty());
    assertEquals(21, cal.width(), "5w base width");
    assertEquals(5, cal.height(), "Feb 2024 rows");

    cal =
        Calendar.of(feb2024, CalendarEventStore.empty())
            .withShowMonthHeader(Style.empty())
            .withShowWeekdaysHeader(Style.empty());
    assertEquals(7, cal.height(), "Headers add 2 rows (5w)");

    cal = Calendar.of(feb2024, CalendarEventStore.empty()).withBlock(Block.bordered());
    assertEquals(23, cal.width(), "Border adds 2 cols");
    assertEquals(7, cal.height(), "Border adds 2 rows");

    // Apr 2023 starts Saturday and spans 6 rows.
    LocalDate apr2023 = LocalDate.of(2023, Month.APRIL, 1);
    cal = Calendar.of(apr2023, CalendarEventStore.empty());
    assertEquals(21, cal.width(), "6w base width");
    assertEquals(6, cal.height(), "Apr 2023 rows");

    cal =
        Calendar.of(apr2023, CalendarEventStore.empty())
            .withShowMonthHeader(Style.empty())
            .withShowWeekdaysHeader(Style.empty());
    assertEquals(8, cal.height(), "Headers add 2 rows (6w)");

    Block block2 = Block.bordered().withPadding(Padding.symmetric(1, 1));
    cal = Calendar.of(apr2023, CalendarEventStore.empty()).withBlock(block2);
    assertEquals(25, cal.width(), "Symmetric padding width");
    assertEquals(10, cal.height(), "Symmetric padding height");
  }

  @Test
  public void sunday_based_weeks_shapes() {
    LocalDate sundayStart = LocalDate.of(2015, Month.FEBRUARY, 11);
    LocalDate saturdayStart = LocalDate.of(2023, Month.APRIL, 9);
    LocalDate leapYear = LocalDate.of(2024, Month.FEBRUARY, 29);

    assertEquals(4, Calendar.sundayBasedWeeks(sundayStart));
    assertEquals(6, Calendar.sundayBasedWeeks(saturdayStart));
    assertEquals(5, Calendar.sundayBasedWeeks(leapYear));
  }
}
