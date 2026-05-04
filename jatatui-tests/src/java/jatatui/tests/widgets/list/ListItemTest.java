package jatatui.tests.widgets.list;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import jatatui.core.text.Line;
import jatatui.core.text.Span;
import jatatui.core.text.Text;
import jatatui.widgets.list.ListItem;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/// Tests for [ListItem], ported from `ratatui-widgets/src/list/item.rs` (upstream tests).
public class ListItemTest {

  @Test
  public void new_from_str() {
    ListItem item = ListItem.of("Test item");
    assertEquals(Text.from("Test item"), item.content);
    assertEquals(Style.empty(), item.style);
  }

  @Test
  public void new_from_string() {
    // Java has no Cow/String distinction.
    ListItem item = ListItem.of(new String("Test item"));
    assertEquals(Text.from("Test item"), item.content);
    assertEquals(Style.empty(), item.style);
  }

  // upstream new_from_cow_str — N/A in Java (no Cow type).

  @Test
  public void new_from_span() {
    Span span = Span.styled("Test item", Style.empty().withFg(Color.BLUE));
    ListItem item = ListItem.of(span);
    assertEquals(Text.from(span), item.content);
    assertEquals(Style.empty(), item.style);
  }

  @Test
  public void new_from_spans() {
    Line spans =
        Line.from(
            Span.styled("Test ", Style.empty().withFg(Color.BLUE)),
            Span.styled("item", Style.empty().withFg(Color.RED)));
    ListItem item = ListItem.of(spans);
    assertEquals(Text.from(spans), item.content);
    assertEquals(Style.empty(), item.style);
  }

  @Test
  public void new_from_vec_spans() {
    List<Line> lines = new ArrayList<>();
    lines.add(
        Line.from(
            Span.styled("Test ", Style.empty().withFg(Color.BLUE)),
            Span.styled("item", Style.empty().withFg(Color.RED))));
    lines.add(
        Line.from(
            Span.styled("Second ", Style.empty().withFg(Color.GREEN)),
            Span.styled("line", Style.empty().withFg(Color.YELLOW))));
    ListItem item = ListItem.ofLines(lines);
    assertEquals(Text.fromLines(lines), item.content);
    assertEquals(Style.empty(), item.style);
  }

  @Test
  public void str_into_list_item() {
    String s = "Test item";
    ListItem item = ListItem.of(s);
    assertEquals(Text.from(s), item.content);
    assertEquals(Style.empty(), item.style);
  }

  @Test
  public void string_into_list_item() {
    String s = "Test item";
    ListItem item = ListItem.of(s);
    assertEquals(Text.from(s), item.content);
    assertEquals(Style.empty(), item.style);
  }

  @Test
  public void span_into_list_item() {
    Span s = Span.from("Test item");
    ListItem item = ListItem.of(s);
    assertEquals(Text.from(s), item.content);
    assertEquals(Style.empty(), item.style);
  }

  @Test
  public void vec_lines_into_list_item() {
    List<Line> lines = new ArrayList<>();
    lines.add(Line.raw("l1"));
    lines.add(Line.raw("l2"));
    ListItem item = ListItem.ofLines(lines);
    assertEquals(Text.fromLines(lines), item.content);
    assertEquals(Style.empty(), item.style);
  }

  @Test
  public void style() {
    ListItem item = ListItem.of("Test item").withStyle(Style.empty().withBg(Color.RED));
    assertEquals(Text.from("Test item"), item.content);
    assertEquals(Style.empty().withBg(Color.RED), item.style);
  }

  @Test
  public void height() {
    ListItem item = ListItem.of("Test item");
    assertEquals(1, item.height());

    ListItem multi = ListItem.of("Test item\nSecond line");
    assertEquals(2, multi.height());
  }

  @Test
  public void width() {
    ListItem item = ListItem.of("Test item");
    assertEquals(9, item.width());
  }

  @Test
  public void can_be_stylized() {
    Style expected =
        Style.empty()
            .withFg(Color.BLACK)
            .withBg(Color.WHITE)
            .withAddModifier(Modifier.BOLD)
            .withRemoveModifier(Modifier.DIM);
    assertEquals(expected, ListItem.of("").black().onWhite().bold().notDim().style);
  }
}
