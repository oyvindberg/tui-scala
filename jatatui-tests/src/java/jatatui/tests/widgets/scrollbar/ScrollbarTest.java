package jatatui.tests.widgets.scrollbar;

import static jatatui.tests._support.BufferAssertions.assertBufferEq;

import jatatui.core.buffer.Buffer;
import jatatui.core.internal.Wcwidth;
import jatatui.core.layout.Rect;
import jatatui.widgets.scrollbar.Scrollbar;
import jatatui.widgets.scrollbar.ScrollbarOrientation;
import jatatui.widgets.scrollbar.ScrollbarState;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class ScrollbarTest {

  private static Scrollbar scrollbarNoArrows() {
    return Scrollbar.of(ScrollbarOrientation.HorizontalTop)
        .withBeginSymbol(Optional.empty())
        .withEndSymbol(Optional.empty())
        .withTrackSymbol(Optional.of("-"))
        .withThumbSymbol("#");
  }

  static Stream<Arguments> simplest_cases() {
    return Stream.of(Arguments.of("#-", 0, 2), Arguments.of("-#", 1, 2));
  }

  @ParameterizedTest
  @MethodSource("simplest_cases")
  public void render_scrollbar_simplest(String expected, int position, int contentLength) {
    Buffer buf = Buffer.empty(new Rect(0, 0, Wcwidth.width(expected), 1));
    ScrollbarState state = ScrollbarState.of(contentLength).withPosition(position);
    scrollbarNoArrows().render(buf.area(), buf, state);
    assertBufferEq(buf, Buffer.withLines(expected));
  }

  static Stream<Arguments> simple_cases() {
    return Stream.of(
        Arguments.of("#####-----", 0, 10),
        Arguments.of("-#####----", 1, 10),
        Arguments.of("-#####----", 2, 10),
        Arguments.of("--#####---", 3, 10),
        Arguments.of("--#####---", 4, 10),
        Arguments.of("---#####--", 5, 10),
        Arguments.of("---#####--", 6, 10),
        Arguments.of("----#####-", 7, 10),
        Arguments.of("----#####-", 8, 10),
        Arguments.of("-----#####", 9, 10));
  }

  @ParameterizedTest
  @MethodSource("simple_cases")
  public void render_scrollbar_simple(String expected, int position, int contentLength) {
    Buffer buf = Buffer.empty(new Rect(0, 0, Wcwidth.width(expected), 1));
    ScrollbarState state = ScrollbarState.of(contentLength).withPosition(position);
    scrollbarNoArrows().render(buf.area(), buf, state);
    assertBufferEq(buf, Buffer.withLines(expected));
  }

  @Test
  public void render_scrollbar_nobar() {
    String expected = "          ";
    Buffer buf = Buffer.empty(new Rect(0, 0, Wcwidth.width(expected), 1));
    ScrollbarState state = ScrollbarState.of(0).withPosition(0);
    scrollbarNoArrows().render(buf.area(), buf, state);
    assertBufferEq(buf, Buffer.withLines(expected));
  }

  static Stream<Arguments> fullbar_cases() {
    return Stream.of(
        Arguments.of("##########", 0, 1),
        Arguments.of("#########-", 0, 2),
        Arguments.of("-#########", 1, 2));
  }

  @ParameterizedTest
  @MethodSource("fullbar_cases")
  public void render_scrollbar_fullbar(String expected, int position, int contentLength) {
    Buffer buf = Buffer.empty(new Rect(0, 0, Wcwidth.width(expected), 1));
    ScrollbarState state = ScrollbarState.of(contentLength).withPosition(position);
    scrollbarNoArrows().render(buf.area(), buf, state);
    assertBufferEq(buf, Buffer.withLines(expected));
  }

  static Stream<Arguments> with_symbols_cases() {
    return Stream.of(
        Arguments.of("<####---->", 0, 10),
        Arguments.of("<#####--->", 1, 10),
        Arguments.of("<-####--->", 2, 10),
        Arguments.of("<-####--->", 3, 10),
        Arguments.of("<--####-->", 4, 10),
        Arguments.of("<--####-->", 5, 10),
        Arguments.of("<---####->", 6, 10),
        Arguments.of("<---####->", 7, 10),
        Arguments.of("<---#####>", 8, 10),
        Arguments.of("<----####>", 9, 10),
        Arguments.of("<----####>", 10, 10),
        Arguments.of("<----####>", 15, 10),
        Arguments.of("<----####>", 500, 10));
  }

  @ParameterizedTest
  @MethodSource("with_symbols_cases")
  public void render_scrollbar_with_symbols(String expected, int position, int contentLength) {
    Buffer buf = Buffer.empty(new Rect(0, 0, Wcwidth.width(expected), 1));
    ScrollbarState state = ScrollbarState.of(contentLength).withPosition(position);
    Scrollbar.of(ScrollbarOrientation.HorizontalTop)
        .withBeginSymbol(Optional.of("<"))
        .withEndSymbol(Optional.of(">"))
        .withTrackSymbol(Optional.of("-"))
        .withThumbSymbol("#")
        .render(buf.area(), buf, state);
    assertBufferEq(buf, Buffer.withLines(expected));
  }

  static Stream<Arguments> double_cases() {
    return Stream.of(
        Arguments.of("█████═════", 0, 10),
        Arguments.of("═█████════", 1, 10),
        Arguments.of("═█████════", 2, 10),
        Arguments.of("══█████═══", 3, 10),
        Arguments.of("══█████═══", 4, 10),
        Arguments.of("═══█████══", 5, 10),
        Arguments.of("═══█████══", 6, 10),
        Arguments.of("════█████═", 7, 10),
        Arguments.of("════█████═", 8, 10),
        Arguments.of("═════█████", 9, 10),
        Arguments.of("═════█████", 100, 10));
  }

  @ParameterizedTest
  @MethodSource("double_cases")
  public void render_scrollbar_without_symbols(String expected, int position, int contentLength) {
    int size = Wcwidth.width(expected);
    Buffer buf = Buffer.empty(new Rect(0, 0, size, 1));
    ScrollbarState state = ScrollbarState.of(contentLength).withPosition(position);
    Scrollbar.of(ScrollbarOrientation.HorizontalBottom)
        .withBeginSymbol(Optional.empty())
        .withEndSymbol(Optional.empty())
        .render(buf.area(), buf, state);
    assertBufferEq(buf, Buffer.withLines(expected));
  }

  @ParameterizedTest
  @MethodSource("double_cases")
  public void render_scrollbar_horizontal_bottom(
      String expected, int position, int contentLength) {
    int size = Wcwidth.width(expected);
    Buffer buf = Buffer.empty(new Rect(0, 0, size, 2));
    ScrollbarState state = ScrollbarState.of(contentLength).withPosition(position);
    Scrollbar.of(ScrollbarOrientation.HorizontalBottom)
        .withBeginSymbol(Optional.empty())
        .withEndSymbol(Optional.empty())
        .render(buf.area(), buf, state);
    String emptyLine = " ".repeat(size);
    assertBufferEq(buf, Buffer.withLines(emptyLine, expected));
  }

  @ParameterizedTest
  @MethodSource("double_cases")
  public void render_scrollbar_horizontal_top(String expected, int position, int contentLength) {
    int size = Wcwidth.width(expected);
    Buffer buf = Buffer.empty(new Rect(0, 0, size, 2));
    ScrollbarState state = ScrollbarState.of(contentLength).withPosition(position);
    Scrollbar.of(ScrollbarOrientation.HorizontalTop)
        .withBeginSymbol(Optional.empty())
        .withEndSymbol(Optional.empty())
        .render(buf.area(), buf, state);
    String emptyLine = " ".repeat(size);
    assertBufferEq(buf, Buffer.withLines(expected, emptyLine));
  }

  @ParameterizedTest
  @MethodSource("with_symbols_cases")
  public void render_scrollbar_vertical_left(String expected, int position, int contentLength) {
    int size = Wcwidth.width(expected);
    Buffer buf = Buffer.empty(new Rect(0, 0, 5, size));
    ScrollbarState state = ScrollbarState.of(contentLength).withPosition(position);
    Scrollbar.of(ScrollbarOrientation.VerticalLeft)
        .withBeginSymbol(Optional.of("<"))
        .withEndSymbol(Optional.of(">"))
        .withTrackSymbol(Optional.of("-"))
        .withThumbSymbol("#")
        .render(buf.area(), buf, state);
    String[] lines = new String[size];
    for (int i = 0; i < size; i++) {
      lines[i] = expected.charAt(i) + "    ";
    }
    assertBufferEq(buf, Buffer.withLines(lines));
  }

  @ParameterizedTest
  @MethodSource("with_symbols_cases")
  public void render_scrollbar_vertical_right(String expected, int position, int contentLength) {
    int size = Wcwidth.width(expected);
    Buffer buf = Buffer.empty(new Rect(0, 0, 5, size));
    ScrollbarState state = ScrollbarState.of(contentLength).withPosition(position);
    Scrollbar.of(ScrollbarOrientation.VerticalRight)
        .withBeginSymbol(Optional.of("<"))
        .withEndSymbol(Optional.of(">"))
        .withTrackSymbol(Optional.of("-"))
        .withThumbSymbol("#")
        .render(buf.area(), buf, state);
    String[] lines = new String[size];
    for (int i = 0; i < size; i++) {
      lines[i] = "    " + expected.charAt(i);
    }
    assertBufferEq(buf, Buffer.withLines(lines));
  }

  static Stream<Arguments> custom_viewport_cases() {
    return Stream.of(
        Arguments.of("##--------", 0, 10),
        Arguments.of("-##-------", 1, 10),
        Arguments.of("--##------", 2, 10),
        Arguments.of("---##-----", 3, 10),
        Arguments.of("----#-----", 4, 10),
        Arguments.of("-----#----", 5, 10),
        Arguments.of("-----##---", 6, 10),
        Arguments.of("------##--", 7, 10),
        Arguments.of("-------##-", 8, 10),
        Arguments.of("--------##", 9, 10),
        Arguments.of("--------##", 10, 10));
  }

  @ParameterizedTest
  @MethodSource("custom_viewport_cases")
  public void custom_viewport_length(String expected, int position, int contentLength) {
    int size = Wcwidth.width(expected);
    Buffer buf = Buffer.empty(new Rect(0, 0, size, 1));
    ScrollbarState state =
        ScrollbarState.of(contentLength).withPosition(position).withViewportContentLength(2);
    scrollbarNoArrows().render(buf.area(), buf, state);
    assertBufferEq(buf, Buffer.withLines(expected));
  }

  static Stream<Arguments> small_track_cases() {
    return Stream.of(
        Arguments.of("#----", 0, 100),
        Arguments.of("#----", 10, 100),
        Arguments.of("-#---", 20, 100),
        Arguments.of("-#---", 30, 100),
        Arguments.of("--#--", 40, 100),
        Arguments.of("--#--", 50, 100),
        Arguments.of("---#-", 60, 100),
        Arguments.of("---#-", 70, 100),
        Arguments.of("----#", 80, 100),
        Arguments.of("----#", 90, 100),
        Arguments.of("----#", 100, 100));
  }

  @ParameterizedTest
  @MethodSource("small_track_cases")
  public void thumb_visible_on_very_small_track(
      String expected, int position, int contentLength) {
    int size = Wcwidth.width(expected);
    Buffer buf = Buffer.empty(new Rect(0, 0, size, 1));
    ScrollbarState state =
        ScrollbarState.of(contentLength).withPosition(position).withViewportContentLength(2);
    scrollbarNoArrows().render(buf.area(), buf, state);
    assertBufferEq(buf, Buffer.withLines(expected));
  }

  static Stream<Arguments> empty_area_cases() {
    return Stream.of(Arguments.of(10, 0), Arguments.of(0, 10));
  }

  @ParameterizedTest
  @MethodSource("empty_area_cases")
  public void do_not_render_with_empty_area(int width, int height) {
    Scrollbar scrollbar =
        Scrollbar.of(ScrollbarOrientation.VerticalRight)
            .withBeginSymbol(Optional.of("<"))
            .withEndSymbol(Optional.of(">"))
            .withTrackSymbol(Optional.of("-"))
            .withThumbSymbol("#");
    Rect zero = new Rect(0, 0, width, height);
    Buffer buf = Buffer.empty(new Rect(0, 0, 10, 10));
    ScrollbarState state = ScrollbarState.of(10);
    // Should not throw.
    scrollbar.render(zero, buf, state);
  }

  @ParameterizedTest
  @EnumSource(ScrollbarOrientation.class)
  public void render_in_minimal_buffer(ScrollbarOrientation orientation) {
    Buffer buf = Buffer.empty(new Rect(0, 0, 1, 1));
    Scrollbar scrollbar = Scrollbar.of(orientation);
    ScrollbarState state = ScrollbarState.of(10).withPosition(5);
    // Should not throw.
    scrollbar.render(buf.area(), buf, state);
    assertBufferEq(buf, Buffer.withLines(" "));
  }

  @ParameterizedTest
  @EnumSource(ScrollbarOrientation.class)
  public void render_in_zero_size_buffer(ScrollbarOrientation orientation) {
    Buffer buf = Buffer.empty(new Rect(0, 0, 0, 0));
    Scrollbar scrollbar = Scrollbar.of(orientation);
    ScrollbarState state = ScrollbarState.of(10).withPosition(5);
    // Should not throw.
    scrollbar.render(buf.area(), buf, state);
  }
}
