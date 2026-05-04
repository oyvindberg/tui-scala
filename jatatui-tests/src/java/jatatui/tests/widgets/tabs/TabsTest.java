package jatatui.tests.widgets.tabs;

import static jatatui.tests._support.BufferAssertions.assertBufferEq;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Rect;
import jatatui.core.style.Style;
import jatatui.core.text.Line;
import jatatui.core.text.Span;
import jatatui.widgets.block.Block;
import jatatui.widgets.tabs.Tabs;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class TabsTest {

  private static void testCase(Tabs tabs, Rect area, Buffer expected) {
    Buffer buf = Buffer.empty(area);
    tabs.render(area, buf);
    assertBufferEq(buf, expected);
  }

  @Test
  public void new_() {
    Tabs tabs = Tabs.ofStrings("Tab1", "Tab2", "Tab3", "Tab4");
    assertEquals(
        List.of(Line.from("Tab1"), Line.from("Tab2"), Line.from("Tab3"), Line.from("Tab4")),
        tabs.titles());
    assertEquals(Optional.of(0), tabs.selected());
    assertEquals(Style.empty(), tabs.style());
    assertEquals(Tabs.DEFAULT_HIGHLIGHT_STYLE, tabs.highlightStyle());
    assertEquals(Span.raw("│"), tabs.divider());
    assertEquals(Line.from(" "), tabs.paddingLeft());
    assertEquals(Line.from(" "), tabs.paddingRight());
  }

  @Test
  public void default_() {
    Tabs tabs = Tabs.empty();
    assertEquals(List.of(), tabs.titles());
    assertEquals(Optional.empty(), tabs.selected());
  }

  @Test
  public void select_into() {
    Tabs tabs = Tabs.ofStrings("Tab1", "Tab2", "Tab3", "Tab4");
    assertEquals(Optional.of(2), tabs.withSelected(2).selected());
    assertEquals(Optional.empty(), tabs.withoutSelected().selected());
    assertEquals(Optional.of(1), tabs.withSelected(1).selected());
  }

  @Test
  public void select_before_titles() {
    Tabs tabs = Tabs.empty().withSelected(1).withTitlesStrings("Tab1", "Tab2");
    assertEquals(Optional.of(1), tabs.selected());
  }

  @Test
  public void render_new() {
    Tabs tabs = Tabs.ofStrings("Tab1", "Tab2", "Tab3", "Tab4");
    Buffer expected = Buffer.withLines(" Tab1 │ Tab2 │ Tab3 │ Tab4    ");
    expected.setStyle(new Rect(1, 0, 4, 1), Tabs.DEFAULT_HIGHLIGHT_STYLE);
    testCase(tabs, new Rect(0, 0, 30, 1), expected);
  }

  @Test
  public void render_no_padding() {
    Tabs tabs = Tabs.ofStrings("Tab1", "Tab2", "Tab3", "Tab4").withPadding("", "");
    Buffer expected = Buffer.withLines("Tab1│Tab2│Tab3│Tab4           ");
    expected.setStyle(new Rect(0, 0, 4, 1), Tabs.DEFAULT_HIGHLIGHT_STYLE);
    testCase(tabs, new Rect(0, 0, 30, 1), expected);
  }

  @Test
  public void render_left_padding() {
    Tabs tabs = Tabs.ofStrings("Tab1", "Tab2", "Tab3", "Tab4").withPaddingLeft("---");
    Buffer expected = Buffer.withLines("---Tab1 │---Tab2 │---Tab3 │---Tab4      ");
    expected.setStyle(new Rect(3, 0, 4, 1), Tabs.DEFAULT_HIGHLIGHT_STYLE);
    testCase(tabs, new Rect(0, 0, 40, 1), expected);
  }

  @Test
  public void render_right_padding() {
    Tabs tabs = Tabs.ofStrings("Tab1", "Tab2", "Tab3", "Tab4").withPaddingRight("++");
    Buffer expected = Buffer.withLines(" Tab1++│ Tab2++│ Tab3++│ Tab4++         ");
    expected.setStyle(new Rect(1, 0, 4, 1), Tabs.DEFAULT_HIGHLIGHT_STYLE);
    testCase(tabs, new Rect(0, 0, 40, 1), expected);
  }

  @Test
  public void render_more_padding() {
    Tabs tabs = Tabs.ofStrings("Tab1", "Tab2", "Tab3", "Tab4").withPadding("---", "++");
    Buffer expected = Buffer.withLines("---Tab1++│---Tab2++│---Tab3++│");
    expected.setStyle(new Rect(3, 0, 4, 1), Tabs.DEFAULT_HIGHLIGHT_STYLE);
    testCase(tabs, new Rect(0, 0, 30, 1), expected);
  }

  @Test
  public void render_with_block() {
    Tabs tabs =
        Tabs.ofStrings("Tab1", "Tab2", "Tab3", "Tab4")
            .withBlock(Block.bordered().withTitle("Tabs"));
    Buffer expected =
        Buffer.withLines(
            "┌Tabs────────────────────────┐",
            "│ Tab1 │ Tab2 │ Tab3 │ Tab4  │",
            "└────────────────────────────┘");
    expected.setStyle(new Rect(2, 1, 4, 1), Tabs.DEFAULT_HIGHLIGHT_STYLE);
    testCase(tabs, new Rect(0, 0, 30, 3), expected);
  }

  @Test
  public void render_divider() {
    Tabs tabs = Tabs.ofStrings("Tab1", "Tab2", "Tab3", "Tab4").withDivider("--");
    Buffer expected = Buffer.withLines(" Tab1 -- Tab2 -- Tab3 -- Tab4 ");
    expected.setStyle(new Rect(1, 0, 4, 1), Tabs.DEFAULT_HIGHLIGHT_STYLE);
    testCase(tabs, new Rect(0, 0, 30, 1), expected);
  }

  @Test
  public void can_be_stylized() {
    Tabs styled = Tabs.ofStrings("").black().onWhite().bold().notItalic();
    Style expected = Style.empty().black().onWhite().bold().notItalic();
    assertEquals(expected, styled.style());
  }

  @Test
  public void render_in_minimal_buffer() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 1, 1));
    Tabs tabs = Tabs.ofStrings("Tab1", "Tab2", "Tab3", "Tab4").withSelected(1).withDivider("|");
    // Should not throw.
    tabs.render(buf.area(), buf);
    assertBufferEq(buf, Buffer.withLines(" "));
  }

  @Test
  public void render_in_zero_size_buffer() {
    Buffer buf = Buffer.empty(new Rect(0, 0, 0, 0));
    Tabs tabs = Tabs.ofStrings("Tab1", "Tab2", "Tab3", "Tab4").withSelected(1).withDivider("|");
    // Should not throw.
    tabs.render(buf.area(), buf);
  }

  @Test
  public void unicode_width_basic() {
    Tabs tabs = Tabs.ofStrings("A", "BB", "CCC");
    // " A │ BB │ CCC " — 14 columns
    assertEquals(14, tabs.width());
  }

  @Test
  public void unicode_width_no_padding() {
    Tabs tabs = Tabs.ofStrings("A", "BB", "CCC").withPadding("", "");
    // "A│BB│CCC" — 8 columns
    assertEquals(8, tabs.width());
  }

  @Test
  public void unicode_width_custom_divider_and_padding() {
    Tabs tabs = Tabs.ofStrings("A", "BB", "CCC").withDivider("--").withPadding("X", "YY");
    // "XAYY--XBBYY--XCCCYY" — 19 columns
    assertEquals(19, tabs.width());
  }

  @Test
  public void unicode_width_empty_titles() {
    Tabs tabs = Tabs.empty();
    assertEquals(0, tabs.width());
  }
}
