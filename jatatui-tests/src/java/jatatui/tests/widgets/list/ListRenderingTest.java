package jatatui.tests.widgets.list;

import static jatatui.tests._support.BufferAssertions.assertBufferEq;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.HorizontalAlignment;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import jatatui.core.text.Line;
import jatatui.core.text.Span;
import jatatui.widgets.block.Block;
import jatatui.widgets.list.HighlightSpacing;
import jatatui.widgets.list.List;
import jatatui.widgets.list.ListDirection;
import jatatui.widgets.list.ListItem;
import jatatui.widgets.list.ListState;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/// Tests for [List] rendering, ported from `ratatui-widgets/src/list/rendering.rs` (upstream
/// inline tests).
public class ListRenderingTest {

  // ---- Fixtures (mirror the rstest #[fixture] single_line_buf) ----

  private static Buffer singleLineBuf() {
    return Buffer.empty(new Rect(0, 0, 10, 1));
  }

  /// Helper: render a widget to an empty buffer with the default state.
  private static Buffer widget(List list, int width, int height) {
    Buffer buffer = Buffer.empty(new Rect(0, 0, width, height));
    list.render(buffer.area, buffer);
    return buffer;
  }

  /// Helper: render a widget to an empty buffer with a given state.
  private static Buffer statefulWidget(List list, ListState state, int width, int height) {
    Buffer buffer = Buffer.empty(new Rect(0, 0, width, height));
    list.render(buffer.area, buffer, state);
    return buffer;
  }

  // ---- Tests ----

  @Test
  public void empty_list() {
    Buffer buffer = singleLineBuf();
    ListState state = ListState.empty();
    List list = List.empty();
    state.selectFirst();
    list.render(buffer.area, buffer, state);
    assertEquals(Optional.empty(), state.selected());
  }

  @Test
  public void single_item() {
    Buffer buffer = singleLineBuf();
    ListState state = ListState.empty();

    List list = List.of(ListItem.of("Item 1"));
    state.selectFirst();
    list.render(buffer.area, buffer, state);
    assertEquals(Optional.of(0), state.selected());

    state.selectLast();
    list.render(buffer.area, buffer, state);
    assertEquals(Optional.of(0), state.selected());

    state.selectPrevious();
    list.render(buffer.area, buffer, state);
    assertEquals(Optional.of(0), state.selected());

    state.selectNext();
    list.render(buffer.area, buffer, state);
    assertEquals(Optional.of(0), state.selected());
  }

  @Test
  public void does_not_render_in_small_space() {
    java.util.List<ListItem> items = new java.util.ArrayList<>();
    items.add(ListItem.of("Item 0"));
    items.add(ListItem.of("Item 1"));
    items.add(ListItem.of("Item 2"));
    List list = List.of(items).withHighlightSymbol(">>");
    Buffer buffer = Buffer.empty(new Rect(0, 0, 15, 3));

    // Render into an area with 0 width.
    list.render(new Rect(0, 0, 0, 3), buffer);
    assertBufferEq(buffer, Buffer.empty(buffer.area));

    // Render into an area with 0 height.
    list.render(new Rect(0, 0, 15, 0), buffer);
    assertBufferEq(buffer, Buffer.empty(buffer.area));

    List listWithBlock =
        List.of(items).withHighlightSymbol(">>").withBlock(Block.bordered());
    // Render into an area with zero inner height after applying block borders.
    listWithBlock.render(new Rect(0, 0, 15, 2), buffer);
    Buffer expected =
        Buffer.withLines("┌─────────────┐", "└─────────────┘", "               ");
    assertBufferEq(buffer, expected);
  }

  // ---- combinations: split into separate test methods (the upstream test is a single fn with
  // many sub-cases via internal helpers). ----

  private static void testCaseRender(ListItem[] items, String... expected) {
    List list = List.of(items).withHighlightSymbol(">>");
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 5));
    list.render(buffer.area, buffer);
    assertBufferEq(buffer, Buffer.withLines(expected));
  }

  private static void testCaseRenderStateful(
      ListItem[] items, Optional<Integer> selected, String... expected) {
    List list = List.of(items).withHighlightSymbol(">>");
    ListState state = ListState.empty().withSelected(selected);
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 5));
    list.render(buffer.area, buffer, state);
    assertBufferEq(buffer, Buffer.withLines(expected));
  }

  private static final ListItem[] EMPTY_ITEMS = new ListItem[] {};
  private static final ListItem[] SINGLE_ITEM = new ListItem[] {ListItem.of("Item 0")};
  private static final ListItem[] MULTIPLE_ITEMS =
      new ListItem[] {ListItem.of("Item 0"), ListItem.of("Item 1"), ListItem.of("Item 2")};
  private static final ListItem[] MULTI_LINE_ITEMS =
      new ListItem[] {
        ListItem.of("Item 0\nLine 2"), ListItem.of("Item 1"), ListItem.of("Item 2")
      };

  @Test
  public void combinations_empty_render() {
    testCaseRender(
        EMPTY_ITEMS, "          ", "          ", "          ", "          ", "          ");
  }

  @Test
  public void combinations_empty_stateful_none() {
    testCaseRenderStateful(
        EMPTY_ITEMS,
        Optional.empty(),
        "          ",
        "          ",
        "          ",
        "          ",
        "          ");
  }

  @Test
  public void combinations_empty_stateful_zero() {
    testCaseRenderStateful(
        EMPTY_ITEMS,
        Optional.of(0),
        "          ",
        "          ",
        "          ",
        "          ",
        "          ");
  }

  @Test
  public void combinations_single_render() {
    testCaseRender(
        SINGLE_ITEM, "Item 0    ", "          ", "          ", "          ", "          ");
  }

  @Test
  public void combinations_single_stateful_none() {
    testCaseRenderStateful(
        SINGLE_ITEM,
        Optional.empty(),
        "Item 0    ",
        "          ",
        "          ",
        "          ",
        "          ");
  }

  @Test
  public void combinations_single_stateful_zero() {
    testCaseRenderStateful(
        SINGLE_ITEM,
        Optional.of(0),
        ">>Item 0  ",
        "          ",
        "          ",
        "          ",
        "          ");
  }

  @Test
  public void combinations_single_stateful_one() {
    testCaseRenderStateful(
        SINGLE_ITEM,
        Optional.of(1),
        ">>Item 0  ",
        "          ",
        "          ",
        "          ",
        "          ");
  }

  @Test
  public void combinations_multiple_render() {
    testCaseRender(
        MULTIPLE_ITEMS,
        "Item 0    ",
        "Item 1    ",
        "Item 2    ",
        "          ",
        "          ");
  }

  @Test
  public void combinations_multiple_stateful_none() {
    testCaseRenderStateful(
        MULTIPLE_ITEMS,
        Optional.empty(),
        "Item 0    ",
        "Item 1    ",
        "Item 2    ",
        "          ",
        "          ");
  }

  @Test
  public void combinations_multiple_stateful_zero() {
    testCaseRenderStateful(
        MULTIPLE_ITEMS,
        Optional.of(0),
        ">>Item 0  ",
        "  Item 1  ",
        "  Item 2  ",
        "          ",
        "          ");
  }

  @Test
  public void combinations_multiple_stateful_one() {
    testCaseRenderStateful(
        MULTIPLE_ITEMS,
        Optional.of(1),
        "  Item 0  ",
        ">>Item 1  ",
        "  Item 2  ",
        "          ",
        "          ");
  }

  @Test
  public void combinations_multiple_stateful_three() {
    testCaseRenderStateful(
        MULTIPLE_ITEMS,
        Optional.of(3),
        "  Item 0  ",
        "  Item 1  ",
        ">>Item 2  ",
        "          ",
        "          ");
  }

  @Test
  public void combinations_multiline_render() {
    testCaseRender(
        MULTI_LINE_ITEMS,
        "Item 0    ",
        "Line 2    ",
        "Item 1    ",
        "Item 2    ",
        "          ");
  }

  @Test
  public void combinations_multiline_stateful_none() {
    testCaseRenderStateful(
        MULTI_LINE_ITEMS,
        Optional.empty(),
        "Item 0    ",
        "Line 2    ",
        "Item 1    ",
        "Item 2    ",
        "          ");
  }

  @Test
  public void combinations_multiline_stateful_zero() {
    testCaseRenderStateful(
        MULTI_LINE_ITEMS,
        Optional.of(0),
        ">>Item 0  ",
        "  Line 2  ",
        "  Item 1  ",
        "  Item 2  ",
        "          ");
  }

  @Test
  public void combinations_multiline_stateful_one() {
    testCaseRenderStateful(
        MULTI_LINE_ITEMS,
        Optional.of(1),
        "  Item 0  ",
        "  Line 2  ",
        ">>Item 1  ",
        "  Item 2  ",
        "          ");
  }

  @Test
  public void items() {
    List list = List.empty().withItems(java.util.List.of(
        ListItem.of("Item 0"), ListItem.of("Item 1"), ListItem.of("Item 2")));
    Buffer buffer = widget(list, 10, 5);
    Buffer expected =
        Buffer.withLines("Item 0    ", "Item 1    ", "Item 2    ", "          ", "          ");
    assertBufferEq(buffer, expected);
  }

  @Test
  public void empty_strings() {
    List list =
        List.ofStrings("Item 0", "", "", "Item 1", "Item 2")
            .withBlock(Block.bordered().withTitle("List"));
    Buffer buffer = widget(list, 10, 7);
    Buffer expected =
        Buffer.withLines(
            "┌List────┐",
            "│Item 0  │",
            "│        │",
            "│        │",
            "│Item 1  │",
            "│Item 2  │",
            "└────────┘");
    assertBufferEq(buffer, expected);
  }

  @Test
  public void block() {
    List list =
        List.ofStrings("Item 0", "Item 1", "Item 2").withBlock(Block.bordered().withTitle("List"));
    Buffer buffer = widget(list, 10, 7);
    Buffer expected =
        Buffer.withLines(
            "┌List────┐",
            "│Item 0  │",
            "│Item 1  │",
            "│Item 2  │",
            "│        │",
            "│        │",
            "└────────┘");
    assertBufferEq(buffer, expected);
  }

  @Test
  public void style() {
    List list = List.ofStrings("Item 0", "Item 1", "Item 2").withStyle(Style.empty().withFg(Color.RED));
    Buffer buffer = widget(list, 10, 5);
    Style red = Style.empty().withFg(Color.RED);
    Buffer expected =
        Buffer.withLineObjects(
            Line.styled("Item 0    ", red),
            Line.styled("Item 1    ", red),
            Line.styled("Item 2    ", red),
            Line.styled("          ", red),
            Line.styled("          ", red));
    assertBufferEq(buffer, expected);
  }

  @Test
  public void highlight_symbol_and_style() {
    List list =
        List.ofStrings("Item 0", "Item 1", "Item 2")
            .withHighlightSymbol(">>")
            .withHighlightStyle(Style.empty().withFg(Color.YELLOW));
    ListState state = ListState.empty();
    state.select(Optional.of(1));
    Buffer buffer = statefulWidget(list, state, 10, 5);
    Style yellow = Style.empty().withFg(Color.YELLOW);
    Buffer expected =
        Buffer.withLineObjects(
            Line.from("  Item 0  "),
            Line.styled(">>Item 1  ", yellow),
            Line.from("  Item 2  "),
            Line.from("          "),
            Line.from("          "));
    assertBufferEq(buffer, expected);
  }

  @Test
  public void highlight_symbol_style_and_style() {
    List list =
        List.ofStrings("Item 0", "Item 1", "Item 2")
            .withHighlightSymbol(Line.from(">>").red().bold())
            .withHighlightStyle(Style.empty().withFg(Color.YELLOW));
    ListState state = ListState.empty();
    state.select(Optional.of(1));
    Buffer buffer = statefulWidget(list, state, 10, 5);
    Style yellow = Style.empty().withFg(Color.YELLOW);
    Buffer expected =
        Buffer.withLineObjects(
            Line.from("  Item 0  "),
            Line.styled(">>Item 1  ", yellow),
            Line.from("  Item 2  "),
            Line.from("          "),
            Line.from("          "));
    expected.setStyle(
        new Rect(0, 1, 2, 1), Style.empty().withFg(Color.RED).withAddModifier(Modifier.BOLD));
    assertBufferEq(buffer, expected);
  }

  @Test
  public void highlight_spacing_default_when_selected_unselected() {
    List list = List.ofStrings("Item 0", "Item 1", "Item 2").withHighlightSymbol(">>");
    ListState state = ListState.empty();
    Buffer buffer = statefulWidget(list, state, 10, 5);
    Buffer expected =
        Buffer.withLines(
            "Item 0    ", "Item 1    ", "Item 2    ", "          ", "          ");
    assertBufferEq(buffer, expected);
  }

  @Test
  public void highlight_spacing_default_when_selected_selected() {
    List list = List.ofStrings("Item 0", "Item 1", "Item 2").withHighlightSymbol(">>");
    ListState state = ListState.empty();
    state.select(Optional.of(1));
    Buffer buffer = statefulWidget(list, state, 10, 5);
    Buffer expected =
        Buffer.withLines(
            "  Item 0  ", ">>Item 1  ", "  Item 2  ", "          ", "          ");
    assertBufferEq(buffer, expected);
  }

  @Test
  public void highlight_spacing_default_always_unselected() {
    List list =
        List.ofStrings("Item 0", "Item 1", "Item 2")
            .withHighlightSymbol(">>")
            .withHighlightSpacing(HighlightSpacing.Always);
    ListState state = ListState.empty();
    Buffer buffer = statefulWidget(list, state, 10, 5);
    Buffer expected =
        Buffer.withLines(
            "  Item 0  ", "  Item 1  ", "  Item 2  ", "          ", "          ");
    assertBufferEq(buffer, expected);
  }

  @Test
  public void highlight_spacing_default_always_selected() {
    List list =
        List.ofStrings("Item 0", "Item 1", "Item 2")
            .withHighlightSymbol(">>")
            .withHighlightSpacing(HighlightSpacing.Always);
    ListState state = ListState.empty();
    state.select(Optional.of(1));
    Buffer buffer = statefulWidget(list, state, 10, 5);
    Buffer expected =
        Buffer.withLines(
            "  Item 0  ", ">>Item 1  ", "  Item 2  ", "          ", "          ");
    assertBufferEq(buffer, expected);
  }

  @Test
  public void highlight_spacing_default_never_unselected() {
    List list =
        List.ofStrings("Item 0", "Item 1", "Item 2")
            .withHighlightSymbol(">>")
            .withHighlightSpacing(HighlightSpacing.Never);
    ListState state = ListState.empty();
    Buffer buffer = statefulWidget(list, state, 10, 5);
    Buffer expected =
        Buffer.withLines(
            "Item 0    ", "Item 1    ", "Item 2    ", "          ", "          ");
    assertBufferEq(buffer, expected);
  }

  @Test
  public void highlight_spacing_default_never_selected() {
    List list =
        List.ofStrings("Item 0", "Item 1", "Item 2")
            .withHighlightSymbol(">>")
            .withHighlightSpacing(HighlightSpacing.Never);
    ListState state = ListState.empty();
    state.select(Optional.of(1));
    Buffer buffer = statefulWidget(list, state, 10, 5);
    Buffer expected =
        Buffer.withLines(
            "Item 0    ", "Item 1    ", "Item 2    ", "          ", "          ");
    assertBufferEq(buffer, expected);
  }

  @Test
  public void repeat_highlight_symbol() {
    List list =
        List.of(
                ListItem.of("Item 0\nLine 2"),
                ListItem.of("Item 1"),
                ListItem.of("Item 2"))
            .withHighlightSymbol(Line.from(">>").red().bold())
            .withHighlightStyle(Style.empty().withFg(Color.YELLOW))
            .withRepeatHighlightSymbol(true);
    ListState state = ListState.empty();
    state.select(Optional.of(0));
    Buffer buffer = statefulWidget(list, state, 10, 5);
    Style yellow = Style.empty().withFg(Color.YELLOW);
    Buffer expected =
        Buffer.withLineObjects(
            Line.styled(">>Item 0  ", yellow),
            Line.styled(">>Line 2  ", yellow),
            Line.from("  Item 1  "),
            Line.from("  Item 2  "),
            Line.from("          "));
    expected.setStyle(
        new Rect(0, 0, 2, 2), Style.empty().withFg(Color.RED).withAddModifier(Modifier.BOLD));
    assertBufferEq(buffer, expected);
  }

  // ---- list_direction (rstest) ----

  static Stream<Arguments> listDirectionCases() {
    return Stream.of(
        Arguments.of(
            ListDirection.TopToBottom,
            new String[] {"Item 0    ", "Item 1    ", "Item 2    ", "          "}),
        Arguments.of(
            ListDirection.BottomToTop,
            new String[] {"          ", "Item 2    ", "Item 1    ", "Item 0    "}));
  }

  @ParameterizedTest
  @MethodSource("listDirectionCases")
  public void list_direction(ListDirection direction, String[] expected) {
    List list = List.ofStrings("Item 0", "Item 1", "Item 2").withDirection(direction);
    Buffer buffer = widget(list, 10, 4);
    assertBufferEq(buffer, Buffer.withLines(expected));
  }

  @Test
  public void truncate_items() {
    List list = List.ofStrings("Item 0", "Item 1", "Item 2", "Item 3", "Item 4");
    Buffer buffer = widget(list, 10, 3);
    Buffer expected = Buffer.withLines("Item 0    ", "Item 1    ", "Item 2    ");
    assertBufferEq(buffer, expected);
  }

  @Test
  public void offset_renders_shifted() {
    List list =
        List.ofStrings("Item 0", "Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6");
    ListState state = ListState.empty().withOffset(3);
    Buffer buffer = statefulWidget(list, state, 6, 3);
    Buffer expected = Buffer.withLines("Item 3", "Item 4", "Item 5");
    assertBufferEq(buffer, expected);
  }

  // ---- long_lines (rstest) ----

  static Stream<Arguments> longLinesCases() {
    return Stream.of(
        Arguments.of(
            Optional.<Integer>empty(),
            new String[] {"Item 0 with a v", "Item 1         ", "Item 2         "}),
        Arguments.of(
            Optional.of(0),
            new String[] {">>Item 0 with a", "  Item 1       ", "  Item 2       "}));
  }

  @ParameterizedTest
  @MethodSource("longLinesCases")
  public void long_lines(Optional<Integer> selected, String[] expected) {
    String[] items =
        new String[] {
          "Item 0 with a very long line that will be truncated", "Item 1", "Item 2"
        };
    List list = List.ofStrings(items).withHighlightSymbol(">>");
    ListState state = ListState.empty().withSelected(selected);
    Buffer buffer = statefulWidget(list, state, 15, 3);
    assertBufferEq(buffer, Buffer.withLines(expected));
  }

  @Test
  public void selected_item_ensures_selected_item_is_visible_when_offset_is_before_visible_range() {
    String[] items = {"Item 0", "Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6"};
    List list = List.ofStrings(items).withHighlightSymbol(">>");
    ListState state = ListState.empty().withSelected(Optional.of(1)).withOffset(3);
    Buffer buffer = statefulWidget(list, state, 10, 3);

    Buffer expected = Buffer.withLines(">>Item 1  ", "  Item 2  ", "  Item 3  ");
    assertBufferEq(buffer, expected);
    assertEquals(Optional.of(1), state.selected());
    assertEquals(1, state.offset(), "did not scroll the selected item into view");
  }

  @Test
  public void selected_item_ensures_selected_item_is_visible_when_offset_is_after_visible_range() {
    String[] items = {"Item 0", "Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6"};
    List list = List.ofStrings(items).withHighlightSymbol(">>");
    ListState state = ListState.empty().withSelected(Optional.of(6)).withOffset(3);
    Buffer buffer = statefulWidget(list, state, 10, 3);

    Buffer expected = Buffer.withLines("  Item 4  ", "  Item 5  ", ">>Item 6  ");
    assertBufferEq(buffer, expected);
    assertEquals(Optional.of(6), state.selected());
    assertEquals(4, state.offset(), "did not scroll the selected item into view");
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

  // ---- Alignment tests ----

  @Test
  public void with_alignment() {
    List list =
        List.of(
            ListItem.of(Line.from("Left").withAlignment(HorizontalAlignment.Left)),
            ListItem.of(Line.from("Center").withAlignment(HorizontalAlignment.Center)),
            ListItem.of(Line.from("Right").withAlignment(HorizontalAlignment.Right)));
    Buffer buffer = widget(list, 10, 4);
    Buffer expected = Buffer.withLines("Left      ", "  Center  ", "     Right", "          ");
    assertBufferEq(buffer, expected);
  }

  @Test
  public void alignment_odd_line_odd_area() {
    List list =
        List.of(
            ListItem.of(Line.from("Odd").withAlignment(HorizontalAlignment.Left)),
            ListItem.of(Line.from("Even").withAlignment(HorizontalAlignment.Center)),
            ListItem.of(Line.from("Width").withAlignment(HorizontalAlignment.Right)));
    Buffer buffer = widget(list, 7, 4);
    Buffer expected = Buffer.withLines("Odd    ", " Even  ", "  Width", "       ");
    assertBufferEq(buffer, expected);
  }

  @Test
  public void alignment_even_line_even_area() {
    List list =
        List.of(
            ListItem.of(Line.from("Odd").withAlignment(HorizontalAlignment.Left)),
            ListItem.of(Line.from("Even").withAlignment(HorizontalAlignment.Center)),
            ListItem.of(Line.from("Width").withAlignment(HorizontalAlignment.Right)));
    Buffer buffer = widget(list, 6, 4);
    Buffer expected = Buffer.withLines("Odd   ", " Even ", " Width", "      ");
    assertBufferEq(buffer, expected);
  }

  @Test
  public void alignment_odd_line_even_area() {
    List list =
        List.of(
            ListItem.of(Line.from("Odd").withAlignment(HorizontalAlignment.Left)),
            ListItem.of(Line.from("Even").withAlignment(HorizontalAlignment.Center)),
            ListItem.of(Line.from("Width").withAlignment(HorizontalAlignment.Right)));
    Buffer buffer = widget(list, 8, 4);
    Buffer expected = Buffer.withLines("Odd     ", "  Even  ", "   Width", "        ");
    assertBufferEq(buffer, expected);
  }

  @Test
  public void alignment_even_line_odd_area() {
    List list =
        List.of(
            ListItem.of(Line.from("Odd").withAlignment(HorizontalAlignment.Left)),
            ListItem.of(Line.from("Even").withAlignment(HorizontalAlignment.Center)),
            ListItem.of(Line.from("Width").withAlignment(HorizontalAlignment.Right)));
    Buffer buffer = widget(list, 6, 4);
    Buffer expected = Buffer.withLines("Odd   ", " Even ", " Width", "      ");
    assertBufferEq(buffer, expected);
  }

  @Test
  public void alignment_zero_line_width() {
    List list =
        List.of(
            ListItem.of(
                Line.from("This line has zero width").withAlignment(HorizontalAlignment.Center)));
    Buffer buffer = widget(list, 0, 2);
    assertBufferEq(buffer, Buffer.withLines("", ""));
  }

  @Test
  public void alignment_zero_area_width() {
    List list = List.of(ListItem.of(Line.from("Text").withAlignment(HorizontalAlignment.Left)));
    Buffer buffer = Buffer.empty(new Rect(0, 0, 4, 1));
    list.render(new Rect(0, 0, 4, 0), buffer);
    assertBufferEq(buffer, Buffer.withLines("    "));
  }

  @Test
  public void alignment_line_less_than_width() {
    List list = List.of(ListItem.of(Line.from("Small").withAlignment(HorizontalAlignment.Center)));
    Buffer buffer = widget(list, 10, 2);
    Buffer expected = Buffer.withLines("  Small   ", "          ");
    assertBufferEq(buffer, expected);
  }

  @Test
  public void alignment_line_equal_to_width() {
    List list = List.of(ListItem.of(Line.from("Exact").withAlignment(HorizontalAlignment.Left)));
    Buffer buffer = widget(list, 5, 2);
    assertBufferEq(buffer, Buffer.withLines("Exact", "     "));
  }

  @Test
  public void alignment_line_greater_than_width() {
    List list =
        List.of(ListItem.of(Line.from("Large line").withAlignment(HorizontalAlignment.Left)));
    Buffer buffer = widget(list, 5, 2);
    assertBufferEq(buffer, Buffer.withLines("Large", "     "));
  }

  // ---- with_padding (rstest with 7 cases) ----

  static Stream<Arguments> withPaddingCases() {
    return Stream.of(
        Arguments.of(
            4,
            2,
            0,
            Optional.of(2),
            new String[] {">> Item 2 ", "   Item 3 ", "   Item 4 ", "   Item 5 "}),
        Arguments.of(
            4,
            2,
            1,
            Optional.of(2),
            new String[] {"   Item 1 ", ">> Item 2 ", "   Item 3 ", "   Item 4 "}),
        Arguments.of(
            4,
            1,
            1,
            Optional.of(4),
            new String[] {"   Item 2 ", "   Item 3 ", ">> Item 4 ", "   Item 5 "}),
        Arguments.of(
            4,
            1,
            2,
            Optional.of(4),
            new String[] {"   Item 2 ", "   Item 3 ", ">> Item 4 ", "   Item 5 "}),
        Arguments.of(
            5,
            2,
            0,
            Optional.of(3),
            new String[] {
              "   Item 2 ", ">> Item 3 ", "   Item 4 ", "   Item 5 ", "          "
            }),
        Arguments.of(
            5,
            2,
            2,
            Optional.of(3),
            new String[] {"   Item 1 ", "   Item 2 ", ">> Item 3 ", "   Item 4 ", "   Item 5 "}),
        Arguments.of(
            4,
            0,
            4,
            Optional.of(1),
            new String[] {"   Item 0 ", ">> Item 1 ", "   Item 2 ", "   Item 3 "}));
  }

  @ParameterizedTest
  @MethodSource("withPaddingCases")
  public void with_padding(
      int renderHeight, int offset, int padding, Optional<Integer> selected, String[] expected) {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, renderHeight));
    ListState state = ListState.empty();
    state.setOffset(offset);
    state.select(selected);

    List list =
        List.ofStrings("Item 0", "Item 1", "Item 2", "Item 3", "Item 4", "Item 5")
            .withScrollPadding(padding)
            .withHighlightSymbol(">> ");
    list.render(buffer.area, buffer, state);
    assertBufferEq(buffer, Buffer.withLines(expected));
  }

  /// If there isn't enough room for the selected item and the requested padding, the list can
  /// jump up and down every frame if something isn't done about it. Make sure that doesn't happen.
  @Test
  public void padding_flicker() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 5));
    ListState state = ListState.empty();
    state.setOffset(2);
    state.select(Optional.of(4));

    String[] items = {
      "Item 0", "Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6", "Item 7"
    };
    List list = List.ofStrings(items).withScrollPadding(3).withHighlightSymbol(">> ");

    list.render(buffer.area, buffer, state);
    int offsetAfterRender = state.offset();

    list.render(buffer.area, buffer, state);
    assertEquals(offsetAfterRender, state.offset());
  }

  @Test
  public void padding_inconsistent_item_sizes() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 3));
    ListState state = ListState.empty().withOffset(0).withSelected(Optional.of(3));

    ListItem[] items = {
      ListItem.of("Item 0"),
      ListItem.of("Item 1"),
      ListItem.of("Item 2"),
      ListItem.of("Item 3"),
      ListItem.of("Item 4\nTest\nTest"),
      ListItem.of("Item 5"),
    };
    List list = List.of(items).withScrollPadding(1).withHighlightSymbol(">> ");

    list.render(buffer.area, buffer, state);

    String[] expected = {"   Item 1 ", "   Item 2 ", ">> Item 3 "};
    assertBufferEq(buffer, Buffer.withLines(expected));
  }

  /// Tests that when pushing back the first visible index value, an item that's too large isn't
  /// included.
  @Test
  public void padding_offset_pushback_break() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 4));
    ListState state = ListState.empty();
    state.setOffset(1);
    state.select(Optional.of(2));

    ListItem[] items = {
      ListItem.of("Item 0\nTest\nTest"),
      ListItem.of("Item 1"),
      ListItem.of("Item 2"),
      ListItem.of("Item 3"),
    };
    List list = List.of(items).withScrollPadding(2).withHighlightSymbol(">> ");
    list.render(buffer.area, buffer, state);

    Buffer expected =
        Buffer.withLines("   Item 1 ", ">> Item 2 ", "   Item 3 ", "          ");
    assertBufferEq(buffer, expected);
  }

  // ---- highlight_symbol_overflow (rstest, 3 cases) ----

  static Stream<Arguments> highlightSymbolOverflowCases() {
    return Stream.of(
        Arguments.of(">>>>", "Item1", ">>>>Item1 "), // enough space
        Arguments.of(">>>>>", "Item1", ">>>>>Item1"), // exact space
        Arguments.of(">>>>>>", "Item1", ">>>>>>Item")); // not enough space
  }

  @ParameterizedTest
  @MethodSource("highlightSymbolOverflowCases")
  public void highlight_symbol_overflow(String highlightSymbol, String item, String expected) {
    Buffer buffer = singleLineBuf();
    List list = List.ofStrings(item).withHighlightSymbol(highlightSymbol);
    ListState state = ListState.empty();
    state.select(Optional.of(0));
    list.render(buffer.area, buffer, state);
    assertBufferEq(buffer, Buffer.withLines(expected));
  }
}
