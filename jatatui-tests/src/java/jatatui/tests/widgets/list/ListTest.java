package jatatui.tests.widgets.list;

import static jatatui.tests._support.BufferAssertions.assertBufferEq;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import jatatui.core.text.Line;
import jatatui.core.text.Span;
import jatatui.core.text.Text;
import jatatui.widgets.list.HighlightSpacing;
import jatatui.widgets.list.List;
import jatatui.widgets.list.ListItem;
import jatatui.widgets.list.ListState;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/// Tests for [List], ported from the inline tests in `ratatui-widgets/src/list.rs`.
///
/// Tests dealing with rendering live in [ListRenderingTest] (the upstream split is
/// `list.rs` vs `list/rendering.rs`).
public class ListTest {

  @Test
  public void collect_list_from_iterator() {
    // Upstream uses `(0..3).map(|i| format!("Item{i}")).collect::<List>()`. Java has no
    // `FromIterator` blanket; the equivalent is to construct the items explicitly via
    // `ListItem.of(...)` and pass to `List.of(...)`.
    List collected = List.ofStrings("Item0", "Item1", "Item2");
    List expected = List.ofStrings("Item0", "Item1", "Item2");
    assertEquals(expected, collected);
  }

  @Test
  public void can_be_stylized() {
    Style expected =
        Style.empty()
            .withFg(Color.BLACK)
            .withBg(Color.WHITE)
            .withAddModifier(Modifier.BOLD)
            .withRemoveModifier(Modifier.DIM);
    assertEquals(expected, List.empty().black().onWhite().bold().notDim().style);
  }

  @Test
  public void no_style() {
    Text text = Text.from("Item 1");
    List list =
        List.of(ListItem.of(text))
            .withHighlightSymbol(">>")
            .withHighlightSpacing(HighlightSpacing.Always);
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 1));

    list.render(buffer.area, buffer, ListState.empty());

    assertBufferEq(buffer, Buffer.withLines("  Item 1  "));
  }

  @Test
  public void styled_text() {
    Text text = Text.from("Item 1").bold();
    List list =
        List.of(ListItem.of(text))
            .withHighlightSymbol(">>")
            .withHighlightSpacing(HighlightSpacing.Always);
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 1));

    list.render(buffer.area, buffer, ListState.empty());

    Buffer expected =
        Buffer.withLineObjects(
            Line.from(Span.raw("  "), Span.styled("Item 1  ", Style.empty().withAddModifier(Modifier.BOLD))));
    assertBufferEq(buffer, expected);
  }

  @Test
  public void styled_list_item() {
    Text text = Text.from("Item 1");
    // Avoid the Stylize methods so the style is set directly (not patched).
    ListItem item = ListItem.of(text).withStyle(Style.empty().withAddModifier(Modifier.ITALIC));
    List list =
        List.of(item).withHighlightSymbol(">>").withHighlightSpacing(HighlightSpacing.Always);
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 1));

    list.render(buffer.area, buffer, ListState.empty());

    Buffer expected =
        Buffer.withLineObjects(
            Line.from(
                Span.styled("  Item 1  ", Style.empty().withAddModifier(Modifier.ITALIC))));
    assertBufferEq(buffer, expected);
  }

  @Test
  public void styled_text_and_list_item() {
    Text text = Text.from("Item 1").bold();
    ListItem item = ListItem.of(text).withStyle(Style.empty().withAddModifier(Modifier.ITALIC));
    List list =
        List.of(item).withHighlightSymbol(">>").withHighlightSpacing(HighlightSpacing.Always);
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 1));

    list.render(buffer.area, buffer, ListState.empty());

    Style italic = Style.empty().withAddModifier(Modifier.ITALIC);
    Style boldItalic =
        Style.empty().withAddModifier(Modifier.BOLD).withAddModifier(Modifier.ITALIC);
    Buffer expected =
        Buffer.withLineObjects(
            Line.from(Span.styled("  ", italic), Span.styled("Item 1  ", boldItalic)));
    assertBufferEq(buffer, expected);
  }

  @Test
  public void styled_highlight() {
    Text text = Text.from("Item 1").bold();
    ListItem item = ListItem.of(text).withStyle(Style.empty().withAddModifier(Modifier.ITALIC));
    ListState state = ListState.empty().withSelected(Optional.of(0));
    List list =
        List.of(item).withHighlightSymbol(">>").withHighlightStyle(Style.empty().withFg(Color.RED));

    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 1));
    list.render(buffer.area, buffer, state);

    Style italicRed =
        Style.empty().withAddModifier(Modifier.ITALIC).withFg(Color.RED);
    Style boldItalicRed =
        Style.empty()
            .withAddModifier(Modifier.BOLD)
            .withAddModifier(Modifier.ITALIC)
            .withFg(Color.RED);
    Buffer expected =
        Buffer.withLineObjects(
            Line.from(Span.styled(">>", italicRed), Span.styled("Item 1  ", boldItalicRed)));
    assertBufferEq(buffer, expected);
  }

  @Test
  public void style_inheritance() {
    Modifier bold = Modifier.BOLD;
    Modifier italic = Modifier.ITALIC;
    ListItem[] items =
        new ListItem[] {
          ListItem.of(Text.raw("Item 1")), // no style
          ListItem.of(Text.styled("Item 2", Style.empty().withAddModifier(bold))), // affects only the text
          ListItem.of(Text.raw("Item 3")).withStyle(Style.empty().withAddModifier(italic)), // entire line
          ListItem.of(Text.styled("Item 4", Style.empty().withAddModifier(bold)))
              .withStyle(Style.empty().withAddModifier(italic)),
          ListItem.of(Text.styled("Item 5", Style.empty().withAddModifier(bold)))
              .withStyle(Style.empty().withAddModifier(italic)),
        };
    ListState state = ListState.empty().withSelected(Optional.of(4));
    List list =
        List.of(items)
            .withHighlightSymbol(">>")
            .withHighlightStyle(Style.empty().withFg(Color.RED))
            .withStyle(Style.empty().withBg(Color.BLUE));

    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 5));
    list.render(buffer.area, buffer, state);

    Style onBlue = Style.empty().withBg(Color.BLUE);
    Style boldOnBlue = Style.empty().withAddModifier(bold).withBg(Color.BLUE);
    Style italicOnBlue = Style.empty().withAddModifier(italic).withBg(Color.BLUE);
    Style boldItalicOnBlue =
        Style.empty().withAddModifier(bold).withAddModifier(italic).withBg(Color.BLUE);
    Style italicRedOnBlue =
        Style.empty().withAddModifier(italic).withFg(Color.RED).withBg(Color.BLUE);
    Style boldItalicRedOnBlue =
        Style.empty()
            .withAddModifier(bold)
            .withAddModifier(italic)
            .withFg(Color.RED)
            .withBg(Color.BLUE);

    Buffer expected =
        Buffer.withLineObjects(
            Line.from(Span.styled("  Item 1  ", onBlue)),
            Line.from(Span.styled("  ", onBlue), Span.styled("Item 2  ", boldOnBlue)),
            Line.from(Span.styled("  Item 3  ", italicOnBlue)),
            Line.from(
                Span.styled("  ", italicOnBlue), Span.styled("Item 4  ", boldItalicOnBlue)),
            Line.from(
                Span.styled(">>", italicRedOnBlue),
                Span.styled("Item 5  ", boldItalicRedOnBlue)));
    assertBufferEq(buffer, expected);
  }

  @Test
  public void render_in_minimal_buffer() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 1, 1));
    ListState state = ListState.empty().withSelected(Optional.empty());
    List list = List.of(ListItem.of("Item 1"), ListItem.of("Item 2"), ListItem.of("Item 3"));
    // Should not throw, even if the buffer is too small to render everything.
    list.render(buffer.area, buffer, state);
    assertBufferEq(buffer, Buffer.withLines("I"));
  }

  @Test
  public void render_in_zero_size_buffer() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 0, 0));
    ListState state = ListState.empty().withSelected(Optional.empty());
    List list = List.of(ListItem.of("Item 1"), ListItem.of("Item 2"), ListItem.of("Item 3"));
    // Should not throw.
    list.render(buffer.area, buffer, state);
  }
}
