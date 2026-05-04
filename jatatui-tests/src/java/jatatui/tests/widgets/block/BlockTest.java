package jatatui.tests.widgets.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.HorizontalAlignment;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import jatatui.core.symbols.Border;
import jatatui.core.symbols.Merge;
import jatatui.core.text.Line;
import jatatui.core.widgets.Widget;
import jatatui.tests._support.BufferAssertions;
import jatatui.widgets.Borders;
import jatatui.widgets.block.Block;
import jatatui.widgets.block.BorderType;
import jatatui.widgets.block.Padding;
import jatatui.widgets.block.TitlePosition;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.Test;

public class BlockTest {

  @Test
  public void create_with_all_borders() {
    Block block = Block.bordered();
    assertEquals(Borders.ALL, block.borders);
  }

  // ---- inner / borders parameterised cases ----

  static Stream<Arguments> innerBordersCases() {
    return Stream.of(
        Arguments.of(Borders.NONE, new Rect(0, 0, 0, 0), new Rect(0, 0, 0, 0)),
        Arguments.of(Borders.NONE, new Rect(0, 0, 1, 1), new Rect(0, 0, 1, 1)),
        Arguments.of(Borders.LEFT, new Rect(0, 0, 0, 0), new Rect(0, 0, 0, 0)),
        Arguments.of(Borders.LEFT, new Rect(0, 0, 0, 1), new Rect(0, 0, 0, 1)),
        Arguments.of(Borders.LEFT, new Rect(0, 0, 1, 1), new Rect(1, 0, 0, 1)),
        Arguments.of(Borders.LEFT, new Rect(0, 0, 2, 1), new Rect(1, 0, 1, 1)),
        Arguments.of(Borders.TOP, new Rect(0, 0, 0, 0), new Rect(0, 0, 0, 0)),
        Arguments.of(Borders.TOP, new Rect(0, 0, 1, 0), new Rect(0, 0, 1, 0)),
        Arguments.of(Borders.TOP, new Rect(0, 0, 1, 1), new Rect(0, 1, 1, 0)),
        Arguments.of(Borders.TOP, new Rect(0, 0, 1, 2), new Rect(0, 1, 1, 1)),
        Arguments.of(Borders.RIGHT, new Rect(0, 0, 0, 0), new Rect(0, 0, 0, 0)),
        Arguments.of(Borders.RIGHT, new Rect(0, 0, 0, 1), new Rect(0, 0, 0, 1)),
        Arguments.of(Borders.RIGHT, new Rect(0, 0, 1, 1), new Rect(0, 0, 0, 1)),
        Arguments.of(Borders.RIGHT, new Rect(0, 0, 2, 1), new Rect(0, 0, 1, 1)),
        Arguments.of(Borders.BOTTOM, new Rect(0, 0, 0, 0), new Rect(0, 0, 0, 0)),
        Arguments.of(Borders.BOTTOM, new Rect(0, 0, 1, 0), new Rect(0, 0, 1, 0)),
        Arguments.of(Borders.BOTTOM, new Rect(0, 0, 1, 1), new Rect(0, 0, 1, 0)),
        Arguments.of(Borders.BOTTOM, new Rect(0, 0, 1, 2), new Rect(0, 0, 1, 1)),
        Arguments.of(Borders.ALL, new Rect(0, 0, 0, 0), new Rect(0, 0, 0, 0)),
        Arguments.of(Borders.ALL, new Rect(0, 0, 1, 1), new Rect(1, 1, 0, 0)),
        Arguments.of(Borders.ALL, new Rect(0, 0, 2, 2), new Rect(1, 1, 0, 0)),
        Arguments.of(Borders.ALL, new Rect(0, 0, 3, 3), new Rect(1, 1, 1, 1)));
  }

  @ParameterizedTest
  @MethodSource("innerBordersCases")
  public void inner_takes_into_account_the_borders(Borders borders, Rect area, Rect expected) {
    Block block = Block.empty().withBorders(borders);
    assertEquals(expected, block.inner(area));
  }

  static Stream<Arguments> alignmentsForTitleInner() {
    return Stream.of(
        Arguments.of(HorizontalAlignment.Left),
        Arguments.of(HorizontalAlignment.Center),
        Arguments.of(HorizontalAlignment.Right));
  }

  @ParameterizedTest
  @MethodSource("alignmentsForTitleInner")
  public void inner_takes_into_account_the_title(HorizontalAlignment alignment) {
    Rect area = new Rect(0, 0, 0, 1);
    Rect expected = new Rect(0, 1, 0, 0);
    Block block = Block.empty().withTitle(Line.from("Test").withAlignment(alignment));
    assertEquals(expected, block.inner(area));
  }

  static Stream<Arguments> innerBorderAndTitleCases() {
    return Stream.of(
        Arguments.of(Block.empty().withTitleTop("Test").withBorders(Borders.TOP), new Rect(0, 1, 0, 1)),
        Arguments.of(Block.empty().withTitleTop("Test").withBorders(Borders.BOTTOM), new Rect(0, 1, 0, 0)),
        Arguments.of(Block.empty().withTitleBottom("Test").withBorders(Borders.TOP), new Rect(0, 1, 0, 0)),
        Arguments.of(Block.empty().withTitleBottom("Test").withBorders(Borders.BOTTOM), new Rect(0, 0, 0, 1)));
  }

  @ParameterizedTest
  @MethodSource("innerBorderAndTitleCases")
  public void inner_takes_into_account_border_and_title(Block block, Rect expected) {
    Rect area = new Rect(0, 0, 0, 2);
    assertEquals(expected, block.inner(area));
  }

  @Test
  public void has_title_at_position_takes_into_account_all_positioning_declarations() {
    // Drives the same logic via inner() since `has_title_at_position` is private upstream too.
    // We rely on inner() shrinking by 1 on the relevant side when a title is present there.
    Block none = Block.empty();
    assertEquals(new Rect(0, 0, 4, 2), none.inner(new Rect(0, 0, 4, 2)));

    Block top = Block.empty().withTitleTop("test");
    // top reserves 1 row at the top
    assertEquals(new Rect(0, 1, 4, 1), top.inner(new Rect(0, 0, 4, 2)));

    Block bottom = Block.empty().withTitleBottom("test");
    // bottom reserves 1 row at the bottom
    assertEquals(new Rect(0, 0, 4, 1), bottom.inner(new Rect(0, 0, 4, 2)));

    Block both = Block.empty().withTitleTop("test").withTitleBottom("test");
    assertEquals(new Rect(0, 1, 4, 0), both.inner(new Rect(0, 0, 4, 2)));
  }

  // ---- vertical_space / horizontal_space ----

  static Stream<Arguments> verticalSpaceBordersCases() {
    return Stream.of(
        Arguments.of(Borders.NONE, 0, 0),
        Arguments.of(Borders.TOP, 1, 0),
        Arguments.of(Borders.RIGHT, 0, 0),
        Arguments.of(Borders.BOTTOM, 0, 1),
        Arguments.of(Borders.LEFT, 0, 0),
        Arguments.of(Borders.TOP.or(Borders.RIGHT), 1, 0),
        Arguments.of(Borders.TOP.or(Borders.BOTTOM), 1, 1),
        Arguments.of(Borders.TOP.or(Borders.LEFT), 1, 0),
        Arguments.of(Borders.BOTTOM.or(Borders.RIGHT), 0, 1),
        Arguments.of(Borders.BOTTOM.or(Borders.LEFT), 0, 1),
        Arguments.of(Borders.LEFT.or(Borders.RIGHT), 0, 0));
  }

  @ParameterizedTest
  @MethodSource("verticalSpaceBordersCases")
  public void vertical_space_takes_into_account_borders(Borders borders, int top, int bottom) {
    Block block = Block.empty().withBorders(borders);
    Block.SpacePair sp = block.verticalSpace();
    assertEquals(new Block.SpacePair(top, bottom), sp);
  }

  static Stream<Arguments> verticalSpacePaddingCases() {
    return Stream.of(
        Arguments.of(Borders.TOP, new Padding(0, 0, 1, 0), 2, 0),
        Arguments.of(Borders.RIGHT, new Padding(0, 0, 1, 0), 1, 0),
        Arguments.of(Borders.BOTTOM, new Padding(0, 0, 1, 0), 1, 1),
        Arguments.of(Borders.LEFT, new Padding(0, 0, 1, 0), 1, 0),
        Arguments.of(Borders.TOP.or(Borders.BOTTOM), new Padding(100, 100, 4, 5), 5, 6),
        Arguments.of(Borders.NONE, new Padding(100, 100, 10, 13), 10, 13),
        Arguments.of(Borders.ALL, new Padding(100, 100, 1, 3), 2, 4));
  }

  @ParameterizedTest
  @MethodSource("verticalSpacePaddingCases")
  public void vertical_space_takes_into_account_padding(
      Borders borders, Padding padding, int top, int bottom) {
    Block block = Block.empty().withBorders(borders).withPadding(padding);
    assertEquals(new Block.SpacePair(top, bottom), block.verticalSpace());
  }

  @Test
  public void vertical_space_takes_into_account_titles() {
    Block block = Block.empty().withTitleTop("Test");
    assertEquals(new Block.SpacePair(1, 0), block.verticalSpace());
    Block block2 = Block.empty().withTitleBottom("Test");
    assertEquals(new Block.SpacePair(0, 1), block2.verticalSpace());
  }

  static Stream<Arguments> verticalSpaceBordersAndTitleCases() {
    return Stream.of(
        Arguments.of(Borders.TOP, TitlePosition.Top, 1, 0),
        Arguments.of(Borders.RIGHT, TitlePosition.Top, 1, 0),
        Arguments.of(Borders.BOTTOM, TitlePosition.Top, 1, 1),
        Arguments.of(Borders.LEFT, TitlePosition.Top, 1, 0),
        Arguments.of(Borders.TOP, TitlePosition.Bottom, 1, 1),
        Arguments.of(Borders.RIGHT, TitlePosition.Bottom, 0, 1),
        Arguments.of(Borders.BOTTOM, TitlePosition.Bottom, 0, 1),
        Arguments.of(Borders.LEFT, TitlePosition.Bottom, 0, 1));
  }

  @ParameterizedTest
  @MethodSource("verticalSpaceBordersAndTitleCases")
  public void vertical_space_takes_into_account_borders_and_title(
      Borders borders, TitlePosition pos, int top, int bottom) {
    Block block = Block.empty().withBorders(borders).withTitlePosition(pos).withTitle("Test");
    assertEquals(new Block.SpacePair(top, bottom), block.verticalSpace());
  }

  @Test
  public void horizontal_space_takes_into_account_borders() {
    Block block = Block.bordered();
    assertEquals(new Block.SpacePair(1, 1), block.horizontalSpace());
    block = Block.empty().withBorders(Borders.LEFT);
    assertEquals(new Block.SpacePair(1, 0), block.horizontalSpace());
    block = Block.empty().withBorders(Borders.RIGHT);
    assertEquals(new Block.SpacePair(0, 1), block.horizontalSpace());
  }

  @Test
  public void horizontal_space_takes_into_account_padding() {
    Block block = Block.empty().withPadding(new Padding(1, 1, 100, 100));
    assertEquals(new Block.SpacePair(1, 1), block.horizontalSpace());
    block = Block.empty().withPadding(new Padding(3, 5, 0, 0));
    assertEquals(new Block.SpacePair(3, 5), block.horizontalSpace());
    block = Block.empty().withPadding(new Padding(0, 1, 100, 100));
    assertEquals(new Block.SpacePair(0, 1), block.horizontalSpace());
    block = Block.empty().withPadding(new Padding(1, 0, 100, 100));
    assertEquals(new Block.SpacePair(1, 0), block.horizontalSpace());
  }

  static Stream<Arguments> horizontalSpaceBordersAndPaddingCases() {
    return Stream.of(
        Arguments.of(Block.bordered(), new Padding(1, 1, 1, 1), 2, 2),
        Arguments.of(Block.bordered(), new Padding(1, 0, 0, 0), 2, 1),
        Arguments.of(Block.bordered(), new Padding(0, 1, 0, 0), 1, 2),
        Arguments.of(Block.bordered(), new Padding(0, 0, 1, 0), 1, 1),
        Arguments.of(Block.bordered(), new Padding(0, 0, 0, 1), 1, 1),
        Arguments.of(Block.empty().withBorders(Borders.LEFT), new Padding(1, 0, 0, 0), 2, 0),
        Arguments.of(Block.empty().withBorders(Borders.LEFT), new Padding(0, 1, 0, 0), 1, 1),
        Arguments.of(Block.empty().withBorders(Borders.RIGHT), new Padding(0, 1, 0, 0), 0, 2),
        Arguments.of(Block.empty().withBorders(Borders.RIGHT), new Padding(1, 0, 0, 0), 1, 1));
  }

  @ParameterizedTest
  @MethodSource("horizontalSpaceBordersAndPaddingCases")
  public void horizontal_space_takes_into_account_borders_and_padding(
      Block block, Padding padding, int left, int right) {
    Block configured = block.withPadding(padding);
    assertEquals(new Block.SpacePair(left, right), configured.horizontalSpace());
  }

  @Test
  public void block_new() {
    Block block = Block.empty();
    assertEquals(List.of(), block.titles);
    assertEquals(Style.empty(), block.titlesStyle);
    assertEquals(HorizontalAlignment.Left, block.titlesAlignment);
    assertEquals(TitlePosition.Top, block.titlesPosition);
    assertEquals(Borders.NONE, block.borders);
    assertEquals(Style.empty(), block.borderStyle);
    assertEquals(BorderType.Plain.toBorderSet(), block.borderSet);
    assertEquals(Style.empty(), block.style);
    assertEquals(Padding.ZERO, block.padding);
    assertEquals(Merge.MergeStrategy.Replace, block.mergeBorders);
  }

  @Test
  public void style_can_be_set() {
    // mirrors upstream `style_into_works_from_user_view` — Java has no Into<Style>, so we just
    // assert that `withStyle` plumbs the style through.
    Block block = Block.empty().withStyle(Style.empty().red());
    assertEquals(Style.empty().red(), block.style);
  }

  @Test
  public void can_be_stylized() {
    Block block = Block.empty().black().onWhite().bold().notDim();
    Style expected =
        Style.empty()
            .withFg(Color.BLACK)
            .withBg(Color.WHITE)
            .withAddModifier(Modifier.BOLD)
            .withRemoveModifier(Modifier.DIM);
    assertEquals(expected, block.style);
  }

  // ---- Rendering ----

  @Test
  public void title_top_bottom() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 11, 3));
    Block.bordered()
        .withTitleTop(Line.raw("A").leftAligned())
        .withTitleTop(Line.raw("B").centered())
        .withTitleTop(Line.raw("C").rightAligned())
        .withTitleBottom(Line.raw("D").leftAligned())
        .withTitleBottom(Line.raw("E").centered())
        .withTitleBottom(Line.raw("F").rightAligned())
        .render(buffer.area, buffer);
    Buffer expected =
        Buffer.withLines(
            "┌A───B───C┐",
            "│         │",
            "└D───E───F┘");
    BufferAssertions.assertBufferEq(buffer, expected);
  }

  static Stream<Arguments> titleAlignmentCases() {
    return Stream.of(
        Arguments.of(HorizontalAlignment.Left, "test    "),
        Arguments.of(HorizontalAlignment.Center, "  test  "),
        Arguments.of(HorizontalAlignment.Right, "    test"));
  }

  @ParameterizedTest
  @MethodSource("titleAlignmentCases")
  public void title_alignment(HorizontalAlignment alignment, String expectedRow) {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 8, 1));
    Block.empty().withTitleAlignment(alignment).withTitle("test").render(buffer.area, buffer);
    BufferAssertions.assertBufferEq(buffer, Buffer.withLines(expectedRow));
  }

  static Stream<Arguments> titleAlignmentOverrideCases() {
    return Stream.of(
        Arguments.of(HorizontalAlignment.Right, HorizontalAlignment.Left, "test    "),
        Arguments.of(HorizontalAlignment.Left, HorizontalAlignment.Center, "  test  "),
        Arguments.of(HorizontalAlignment.Center, HorizontalAlignment.Right, "    test"));
  }

  @ParameterizedTest
  @MethodSource("titleAlignmentOverrideCases")
  public void title_alignment_overrides_block_title_alignment(
      HorizontalAlignment blockAlignment, HorizontalAlignment titleAlignment, String expectedRow) {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 8, 1));
    Block.empty()
        .withTitleAlignment(blockAlignment)
        .withTitle(Line.from("test").withAlignment(titleAlignment))
        .render(buffer.area, buffer);
    BufferAssertions.assertBufferEq(buffer, Buffer.withLines(expectedRow));
  }

  @Test
  public void render_right_aligned_empty_title() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 15, 3));
    Block.empty()
        .withTitleAlignment(HorizontalAlignment.Right)
        .withTitle("")
        .render(buffer.area, buffer);
    BufferAssertions.assertBufferEq(buffer, Buffer.withLines("               ", "               ", "               "));
  }

  @Test
  public void title_position() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 4, 2));
    Block.empty()
        .withTitlePosition(TitlePosition.Bottom)
        .withTitle("test")
        .render(buffer.area, buffer);
    BufferAssertions.assertBufferEq(buffer, Buffer.withLines("    ", "test"));
  }

  @Test
  public void title_content_style() {
    for (HorizontalAlignment alignment :
        new HorizontalAlignment[] {
          HorizontalAlignment.Left, HorizontalAlignment.Center, HorizontalAlignment.Right
        }) {
      Buffer buffer = Buffer.empty(new Rect(0, 0, 4, 1));
      Block.empty()
          .withTitleAlignment(alignment)
          .withTitle(Line.from("test").yellow())
          .render(buffer.area, buffer);
      Buffer expected = Buffer.withLineObjects(Line.from("test").yellow());
      BufferAssertions.assertBufferEq(buffer, expected);
    }
  }

  @Test
  public void block_title_style() {
    for (HorizontalAlignment alignment :
        new HorizontalAlignment[] {
          HorizontalAlignment.Left, HorizontalAlignment.Center, HorizontalAlignment.Right
        }) {
      Buffer buffer = Buffer.empty(new Rect(0, 0, 4, 1));
      Block.empty()
          .withTitleAlignment(alignment)
          .withTitleStyle(Style.empty().yellow())
          .withTitle("test")
          .render(buffer.area, buffer);
      Buffer expected = Buffer.withLineObjects(Line.from("test").yellow());
      BufferAssertions.assertBufferEq(buffer, expected);
    }
  }

  @Test
  public void title_style_overrides_block_title_style() {
    for (HorizontalAlignment alignment :
        new HorizontalAlignment[] {
          HorizontalAlignment.Left, HorizontalAlignment.Center, HorizontalAlignment.Right
        }) {
      Buffer buffer = Buffer.empty(new Rect(0, 0, 4, 1));
      Block.empty()
          .withTitleAlignment(alignment)
          .withTitleStyle(Style.empty().green().onRed())
          .withTitle(Line.from("test").yellow())
          .render(buffer.area, buffer);
      Buffer expected = Buffer.withLineObjects(Line.from("test").yellow().onRed());
      BufferAssertions.assertBufferEq(buffer, expected);
    }
  }

  @Test
  public void title_border_style() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 3));
    Block.bordered()
        .withTitle("test")
        .withBorderStyle(Style.empty().yellow())
        .render(buffer.area, buffer);
    Buffer expected =
        Buffer.withLines(
            "┌test────┐",
            "│        │",
            "└────────┘");
    expected.setStyle(new Rect(0, 0, 10, 3), Style.empty().yellow());
    expected.setStyle(new Rect(1, 1, 8, 1), Style.reset());
    BufferAssertions.assertBufferEq(buffer, expected);
  }

  @Test
  public void render_plain_border() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 3));
    Block.bordered().withBorderType(BorderType.Plain).render(buffer.area, buffer);
    Buffer expected =
        Buffer.withLines(
            "┌────────┐",
            "│        │",
            "└────────┘");
    BufferAssertions.assertBufferEq(buffer, expected);
  }

  @Test
  public void render_rounded_border() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 3));
    Block.bordered().withBorderType(BorderType.Rounded).render(buffer.area, buffer);
    Buffer expected =
        Buffer.withLines(
            "╭────────╮",
            "│        │",
            "╰────────╯");
    BufferAssertions.assertBufferEq(buffer, expected);
  }

  @Test
  public void render_double_border() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 3));
    Block.bordered().withBorderType(BorderType.Double).render(buffer.area, buffer);
    Buffer expected =
        Buffer.withLines(
            "╔════════╗",
            "║        ║",
            "╚════════╝");
    BufferAssertions.assertBufferEq(buffer, expected);
  }

  @Test
  public void render_quadrant_inside() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 3));
    Block.bordered().withBorderType(BorderType.QuadrantInside).render(buffer.area, buffer);
    Buffer expected =
        Buffer.withLines(
            "▗▄▄▄▄▄▄▄▄▖",
            "▐        ▌",
            "▝▀▀▀▀▀▀▀▀▘");
    BufferAssertions.assertBufferEq(buffer, expected);
  }

  @Test
  public void render_border_quadrant_outside() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 3));
    Block.bordered().withBorderType(BorderType.QuadrantOutside).render(buffer.area, buffer);
    Buffer expected =
        Buffer.withLines(
            "▛▀▀▀▀▀▀▀▀▜",
            "▌        ▐",
            "▙▄▄▄▄▄▄▄▄▟");
    BufferAssertions.assertBufferEq(buffer, expected);
  }

  @Test
  public void render_solid_border() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 3));
    Block.bordered().withBorderType(BorderType.Thick).render(buffer.area, buffer);
    Buffer expected =
        Buffer.withLines(
            "┏━━━━━━━━┓",
            "┃        ┃",
            "┗━━━━━━━━┛");
    BufferAssertions.assertBufferEq(buffer, expected);
  }

  @Test
  public void render_light_double_dashed_border() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 3));
    Block.bordered().withBorderType(BorderType.LightDoubleDashed).render(buffer.area, buffer);
    Buffer expected =
        Buffer.withLines(
            "┌╌╌╌╌╌╌╌╌┐",
            "╎        ╎",
            "└╌╌╌╌╌╌╌╌┘");
    BufferAssertions.assertBufferEq(buffer, expected);
  }

  @Test
  public void render_heavy_double_dashed_border() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 3));
    Block.bordered().withBorderType(BorderType.HeavyDoubleDashed).render(buffer.area, buffer);
    Buffer expected =
        Buffer.withLines(
            "┏╍╍╍╍╍╍╍╍┓",
            "╏        ╏",
            "┗╍╍╍╍╍╍╍╍┛");
    BufferAssertions.assertBufferEq(buffer, expected);
  }

  @Test
  public void render_light_triple_dashed_border() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 3));
    Block.bordered().withBorderType(BorderType.LightTripleDashed).render(buffer.area, buffer);
    Buffer expected =
        Buffer.withLines(
            "┌┄┄┄┄┄┄┄┄┐",
            "┆        ┆",
            "└┄┄┄┄┄┄┄┄┘");
    BufferAssertions.assertBufferEq(buffer, expected);
  }

  @Test
  public void render_heavy_triple_dashed_border() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 3));
    Block.bordered().withBorderType(BorderType.HeavyTripleDashed).render(buffer.area, buffer);
    Buffer expected =
        Buffer.withLines(
            "┏┅┅┅┅┅┅┅┅┓",
            "┇        ┇",
            "┗┅┅┅┅┅┅┅┅┛");
    BufferAssertions.assertBufferEq(buffer, expected);
  }

  @Test
  public void render_light_quadruple_dashed_border() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 3));
    Block.bordered().withBorderType(BorderType.LightQuadrupleDashed).render(buffer.area, buffer);
    Buffer expected =
        Buffer.withLines(
            "┌┈┈┈┈┈┈┈┈┐",
            "┊        ┊",
            "└┈┈┈┈┈┈┈┈┘");
    BufferAssertions.assertBufferEq(buffer, expected);
  }

  @Test
  public void render_heavy_quadruple_dashed_border() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 3));
    Block.bordered().withBorderType(BorderType.HeavyQuadrupleDashed).render(buffer.area, buffer);
    Buffer expected =
        Buffer.withLines(
            "┏┉┉┉┉┉┉┉┉┓",
            "┋        ┋",
            "┗┉┉┉┉┉┉┉┉┛");
    BufferAssertions.assertBufferEq(buffer, expected);
  }

  @Test
  public void render_custom_border_set() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 3));
    Block.bordered()
        .withBorderSet(new Border.Set("1", "2", "3", "4", "L", "R", "T", "B"))
        .render(buffer.area, buffer);
    Buffer expected =
        Buffer.withLines(
            "1TTTTTTTT2",
            "L        R",
            "3BBBBBBBB4");
    BufferAssertions.assertBufferEq(buffer, expected);
  }

  static Stream<Arguments> mergeStrategyCases() {
    return Stream.of(
        Arguments.of(Merge.MergeStrategy.Replace),
        Arguments.of(Merge.MergeStrategy.Exact),
        Arguments.of(Merge.MergeStrategy.Fuzzy));
  }

  @ParameterizedTest
  @MethodSource("mergeStrategyCases")
  public void render_partial_borders(Merge.MergeStrategy strategy) {
    // ALL borders, all four sides
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 3));
    Block.empty()
        .withBorders(Borders.TOP.or(Borders.LEFT).or(Borders.RIGHT).or(Borders.BOTTOM))
        .withMergeBorders(strategy)
        .render(buffer.area, buffer);
    BufferAssertions.assertBufferEq(
        buffer,
        Buffer.withLines(
            "┌────────┐",
            "│        │",
            "└────────┘"));

    buffer = Buffer.empty(new Rect(0, 0, 10, 3));
    Block.empty().withBorders(Borders.TOP.or(Borders.LEFT)).withMergeBorders(strategy)
        .render(buffer.area, buffer);
    BufferAssertions.assertBufferEq(
        buffer,
        Buffer.withLines(
            "┌─────────",
            "│         ",
            "│         "));

    buffer = Buffer.empty(new Rect(0, 0, 10, 3));
    Block.empty().withBorders(Borders.TOP.or(Borders.RIGHT)).withMergeBorders(strategy)
        .render(buffer.area, buffer);
    BufferAssertions.assertBufferEq(
        buffer,
        Buffer.withLines(
            "─────────┐",
            "         │",
            "         │"));

    buffer = Buffer.empty(new Rect(0, 0, 10, 3));
    Block.empty().withBorders(Borders.BOTTOM.or(Borders.LEFT)).withMergeBorders(strategy)
        .render(buffer.area, buffer);
    BufferAssertions.assertBufferEq(
        buffer,
        Buffer.withLines(
            "│         ",
            "│         ",
            "└─────────"));

    buffer = Buffer.empty(new Rect(0, 0, 10, 3));
    Block.empty().withBorders(Borders.BOTTOM.or(Borders.RIGHT)).withMergeBorders(strategy)
        .render(buffer.area, buffer);
    BufferAssertions.assertBufferEq(
        buffer,
        Buffer.withLines(
            "         │",
            "         │",
            "─────────┘"));

    buffer = Buffer.empty(new Rect(0, 0, 10, 3));
    Block.empty().withBorders(Borders.TOP.or(Borders.BOTTOM)).withMergeBorders(strategy)
        .render(buffer.area, buffer);
    BufferAssertions.assertBufferEq(
        buffer,
        Buffer.withLines(
            "──────────",
            "          ",
            "──────────"));

    buffer = Buffer.empty(new Rect(0, 0, 10, 3));
    Block.empty().withBorders(Borders.LEFT.or(Borders.RIGHT)).withMergeBorders(strategy)
        .render(buffer.area, buffer);
    BufferAssertions.assertBufferEq(
        buffer,
        Buffer.withLines(
            "│        │",
            "│        │",
            "│        │"));
  }

  @Test
  public void left_titles() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 1));
    Block.empty().withTitle("L12").withTitle("L34").render(buffer.area, buffer);
    BufferAssertions.assertBufferEq(buffer, Buffer.withLines("L12 L34   "));
  }

  @Test
  public void left_titles_truncated() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 1));
    Block.empty().withTitle("L12345").withTitle("L67890").render(buffer.area, buffer);
    BufferAssertions.assertBufferEq(buffer, Buffer.withLines("L12345 L67"));
  }

  @Test
  public void center_titles() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 1));
    Block.empty()
        .withTitle(Line.from("C12").centered())
        .withTitle(Line.from("C34").centered())
        .render(buffer.area, buffer);
    BufferAssertions.assertBufferEq(buffer, Buffer.withLines(" C12 C34  "));
  }

  @Test
  public void center_titles_truncated() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 1));
    Block.empty()
        .withTitle(Line.from("C12345").centered())
        .withTitle(Line.from("C67890").centered())
        .render(buffer.area, buffer);
    BufferAssertions.assertBufferEq(buffer, Buffer.withLines("12345 C678"));
  }

  @Test
  public void right_titles() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 1));
    Block.empty()
        .withTitle(Line.from("R12").rightAligned())
        .withTitle(Line.from("R34").rightAligned())
        .render(buffer.area, buffer);
    BufferAssertions.assertBufferEq(buffer, Buffer.withLines("   R12 R34"));
  }

  @Test
  public void right_titles_truncated() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 1));
    Block.empty()
        .withTitle(Line.from("R12345").rightAligned())
        .withTitle(Line.from("R67890").rightAligned())
        .render(buffer.area, buffer);
    BufferAssertions.assertBufferEq(buffer, Buffer.withLines("345 R67890"));
  }

  @Test
  public void center_title_truncates_left_title() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 1));
    Block.empty()
        .withTitle("L1234")
        .withTitle(Line.from("C5678").centered())
        .render(buffer.area, buffer);
    BufferAssertions.assertBufferEq(buffer, Buffer.withLines("L1C5678   "));
  }

  @Test
  public void right_title_truncates_left_title() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 1));
    Block.empty()
        .withTitle("L12345")
        .withTitle(Line.from("R67890").rightAligned())
        .render(buffer.area, buffer);
    BufferAssertions.assertBufferEq(buffer, Buffer.withLines("L123R67890"));
  }

  @Test
  public void right_title_truncates_center_title() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 10, 1));
    Block.empty()
        .withTitle(Line.from("C12345").centered())
        .withTitle(Line.from("R67890").rightAligned())
        .render(buffer.area, buffer);
    BufferAssertions.assertBufferEq(buffer, Buffer.withLines("  C1R67890"));
  }

  @Test
  public void render_in_minimal_buffer() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 1, 1));
    Block.bordered()
        .withTitle("I'm too big for this buffer")
        .withPadding(Padding.uniform(10))
        .render(buffer.area, buffer);
    BufferAssertions.assertBufferEq(buffer, Buffer.withLines("┌"));
  }

  @Test
  public void render_in_zero_size_buffer() {
    Buffer buffer = Buffer.empty(new Rect(0, 0, 0, 0));
    // should not throw, even if the buffer has zero size
    Block.bordered()
        .withTitle("I'm too big for this buffer")
        .withPadding(Padding.uniform(10))
        .render(buffer.area, buffer);
    assertEquals(new Rect(0, 0, 0, 0), buffer.area);
  }

  // ---- BorderType ----

  @Test
  public void border_type_can_be_const() {
    final Border.Set plain = BorderType.borderSymbols(BorderType.Plain);
    assertEquals(Border.PLAIN, plain);
  }

  @Test
  public void border_type_to_string() {
    // Java enum.toString() returns the variant name, mirroring upstream `Display`.
    assertEquals("Plain", BorderType.Plain.toString());
    assertEquals("Rounded", BorderType.Rounded.toString());
    assertEquals("Double", BorderType.Double.toString());
    assertEquals("Thick", BorderType.Thick.toString());
    assertEquals("LightDoubleDashed", BorderType.LightDoubleDashed.toString());
    assertEquals("HeavyDoubleDashed", BorderType.HeavyDoubleDashed.toString());
    assertEquals("LightTripleDashed", BorderType.LightTripleDashed.toString());
    assertEquals("HeavyTripleDashed", BorderType.HeavyTripleDashed.toString());
    assertEquals("LightQuadrupleDashed", BorderType.LightQuadrupleDashed.toString());
    assertEquals("HeavyQuadrupleDashed", BorderType.HeavyQuadrupleDashed.toString());
  }

  @Test
  public void border_type_from_str() {
    assertEquals(BorderType.Plain, BorderType.valueOf("Plain"));
    assertEquals(BorderType.Rounded, BorderType.valueOf("Rounded"));
    assertEquals(BorderType.Double, BorderType.valueOf("Double"));
    assertEquals(BorderType.Thick, BorderType.valueOf("Thick"));
    assertEquals(BorderType.LightDoubleDashed, BorderType.valueOf("LightDoubleDashed"));
    assertEquals(BorderType.HeavyDoubleDashed, BorderType.valueOf("HeavyDoubleDashed"));
    assertEquals(BorderType.LightTripleDashed, BorderType.valueOf("LightTripleDashed"));
    assertEquals(BorderType.HeavyTripleDashed, BorderType.valueOf("HeavyTripleDashed"));
    assertEquals(BorderType.LightQuadrupleDashed, BorderType.valueOf("LightQuadrupleDashed"));
    assertEquals(BorderType.HeavyQuadrupleDashed, BorderType.valueOf("HeavyQuadrupleDashed"));
    boolean threw = false;
    try {
      BorderType.valueOf("");
    } catch (IllegalArgumentException ignored) {
      threw = true;
    }
    assertTrue(threw, "valueOf(\"\") should throw IllegalArgumentException");
  }

  // ---- Block as a Widget reference ----

  @Test
  public void block_implements_widget() {
    Widget w = Block.bordered();
    assertFalse(w == null);
  }
}
