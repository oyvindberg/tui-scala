package jatatui.tests.widgets.reflow;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.layout.HorizontalAlignment;
import jatatui.core.style.Style;
import jatatui.core.text.Line;
import jatatui.core.text.StyledGrapheme;
import jatatui.core.text.Text;
import jatatui.widgets.reflow.LineComposer;
import jatatui.widgets.reflow.LineTruncator;
import jatatui.widgets.reflow.StyledLineInput;
import jatatui.widgets.reflow.WordWrapper;
import jatatui.widgets.reflow.WrappedLine;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/// Port of the inline `#[cfg(test)] mod tests` from
/// `submodules/ratatui/ratatui-widgets/src/reflow.rs`.
public class ReflowTest {

  /// Mirrors upstream's private `Composer` enum used by `run_composer`.
  sealed interface Composer {}

  record WordWrapperComposer(boolean trim) implements Composer {}

  record LineTruncatorComposer() implements Composer {}

  /// Result of [#runComposer], mirroring the `(Vec<String>, Vec<u16>, Vec<Alignment>)` upstream
  /// returns.
  record ComposerResult(
      List<String> lines, List<Integer> widths, List<HorizontalAlignment> alignments) {}

  static ComposerResult runComposer(Composer which, Text text, int textAreaWidth) {
    List<StyledLineInput> styledLines = new ArrayList<>();
    for (Line line : text) {
      List<StyledGrapheme> graphemes = new ArrayList<>();
      for (var span : line) {
        graphemes.addAll(span.styledGraphemes(Style.empty()));
      }
      styledLines.add(
          new StyledLineInput(
              graphemes.iterator(), line.alignment.orElse(HorizontalAlignment.Left)));
    }

    LineComposer composer =
        switch (which) {
          case WordWrapperComposer w ->
              new WordWrapper(styledLines.iterator(), textAreaWidth, w.trim());
          case LineTruncatorComposer t -> new LineTruncator(styledLines.iterator(), textAreaWidth);
        };

    List<String> lines = new ArrayList<>();
    List<Integer> widths = new ArrayList<>();
    List<HorizontalAlignment> alignments = new ArrayList<>();
    while (true) {
      Optional<WrappedLine> next = composer.nextLine();
      if (next.isEmpty()) break;
      WrappedLine w = next.get();
      StringBuilder sb = new StringBuilder();
      for (StyledGrapheme g : w.graphemes()) {
        sb.append(g.symbol);
      }
      String line = sb.toString();
      if (w.width() > textAreaWidth) {
        throw new AssertionError("width " + w.width() + " > " + textAreaWidth);
      }
      lines.add(line);
      widths.add(w.width());
      alignments.add(w.alignment());
    }
    return new ComposerResult(lines, widths, alignments);
  }

  static ComposerResult runComposer(Composer which, String text, int textAreaWidth) {
    return runComposer(which, Text.raw(text), textAreaWidth);
  }

  // ---- Tests ported from upstream ----

  @Test
  public void line_composer_one_line() {
    int width = 40;
    for (int i = 1; i < width; i++) {
      String text = "a".repeat(i);
      ComposerResult wordWrapper = runComposer(new WordWrapperComposer(true), text, width);
      ComposerResult lineTruncator = runComposer(new LineTruncatorComposer(), text, width);
      List<String> expected = List.of(text);
      assertEquals(expected, wordWrapper.lines());
      assertEquals(expected, lineTruncator.lines());
    }
  }

  @Test
  public void line_composer_short_lines() {
    int width = 20;
    String text = "abcdefg\nhijklmno\npabcdefg\nhijklmn\nopabcdefghijk\nlmnopabcd\n\n\nefghijklmno";
    ComposerResult wordWrapper = runComposer(new WordWrapperComposer(true), text, width);
    ComposerResult lineTruncator = runComposer(new LineTruncatorComposer(), text, width);

    List<String> wrapped = List.of(text.split("\n", -1));
    assertEquals(wrapped, wordWrapper.lines());
    assertEquals(wrapped, lineTruncator.lines());
  }

  @Test
  public void line_composer_long_word() {
    int width = 20;
    String text = "abcdefghijklmnopabcdefghijklmnopabcdefghijklmnopabcdefghijklmno";
    ComposerResult wordWrapper = runComposer(new WordWrapperComposer(true), text, width);
    ComposerResult lineTruncator = runComposer(new LineTruncatorComposer(), text, width);

    List<String> wrapped =
        List.of(
            text.substring(0, width),
            text.substring(width, width * 2),
            text.substring(width * 2, width * 3),
            text.substring(width * 3));
    assertEquals(
        wrapped,
        wordWrapper.lines(),
        "WordWrapper should detect the line cannot be broken on word boundary and break it at line"
            + " width limit.");
    assertEquals(List.of(text.substring(0, width)), lineTruncator.lines());
  }

  @Test
  public void line_composer_long_sentence() {
    int width = 20;
    String text =
        "abcd efghij klmnopabcd efgh ijklmnopabcdefg hijkl mnopab c d e f g h i j k l m n o";
    String textMultiSpace =
        "abcd efghij    klmnopabcd efgh     ijklmnopabcdefg hijkl mnopab c d e f g h i j k l m n o";
    ComposerResult wordWrapperSingleSpace = runComposer(new WordWrapperComposer(true), text, width);
    ComposerResult wordWrapperMultiSpace =
        runComposer(new WordWrapperComposer(true), textMultiSpace, width);
    ComposerResult lineTruncator = runComposer(new LineTruncatorComposer(), text, width);

    List<String> wordWrapped =
        List.of(
            "abcd efghij",
            "klmnopabcd efgh",
            "ijklmnopabcdefg",
            "hijkl mnopab c d e f",
            "g h i j k l m n o");
    assertEquals(wordWrapped, wordWrapperSingleSpace.lines());
    assertEquals(wordWrapped, wordWrapperMultiSpace.lines());
    assertEquals(List.of(text.substring(0, width)), lineTruncator.lines());
  }

  @Test
  public void line_composer_zero_width() {
    int width = 0;
    String text = "abcd efghij klmnopabcd efgh ijklmnopabcdefg hijkl mnopab ";
    ComposerResult wordWrapper = runComposer(new WordWrapperComposer(true), text, width);
    ComposerResult lineTruncator = runComposer(new LineTruncatorComposer(), text, width);

    List<String> expected = List.of();
    assertEquals(expected, wordWrapper.lines());
    assertEquals(expected, lineTruncator.lines());
  }

  @Test
  public void line_composer_max_line_width_of_1() {
    int width = 1;
    String text = "abcd efghij klmnopabcd efgh ijklmnopabcdefg hijkl mnopab ";
    ComposerResult wordWrapper = runComposer(new WordWrapperComposer(true), text, width);
    ComposerResult lineTruncator = runComposer(new LineTruncatorComposer(), text, width);

    List<String> expected = new ArrayList<>();
    BreakIterator boundary = BreakIterator.getCharacterInstance(Locale.getDefault());
    boundary.setText(text);
    int begin = boundary.first();
    int end = boundary.next();
    while (end != BreakIterator.DONE) {
      String g = text.substring(begin, end);
      // keep graphemes containing any non-whitespace char (mirrors upstream Rust filter)
      boolean anyNonWhite = false;
      for (int i = 0; i < g.length(); i++) {
        if (!Character.isWhitespace(g.charAt(i))) {
          anyNonWhite = true;
          break;
        }
      }
      if (anyNonWhite) expected.add(g);
      begin = end;
      end = boundary.next();
    }
    assertEquals(expected, wordWrapper.lines());
    assertEquals(List.of("a"), lineTruncator.lines());
  }

  @Test
  public void line_composer_max_line_width_of_1_double_width_characters() {
    int width = 1;
    String text = "コンピュータ上で文字を扱う場合、典型的には文字\naaa\naによる通信を行う場合にその両端点では、";
    ComposerResult wordWrapper = runComposer(new WordWrapperComposer(true), text, width);
    ComposerResult lineTruncator = runComposer(new LineTruncatorComposer(), text, width);
    assertEquals(List.of("", "a", "a", "a", "a"), wordWrapper.lines());
    assertEquals(List.of("", "a", "a"), lineTruncator.lines());
  }

  @Test
  public void line_composer_word_wrapper_mixed_length() {
    int width = 20;
    String text = "abcd efghij klmnopabcdefghijklmnopabcdefghijkl mnopab cdefghi j klmno";
    ComposerResult wordWrapper = runComposer(new WordWrapperComposer(true), text, width);
    assertEquals(
        List.of(
            "abcd efghij", "klmnopabcdefghijklmn", "opabcdefghijkl", "mnopab cdefghi j", "klmno"),
        wordWrapper.lines());
  }

  @Test
  public void line_composer_double_width_chars() {
    int width = 20;
    String text = "コンピュータ上で文字を扱う場合、典型的には文字による通信を行う場合にその両端点では、";
    ComposerResult wordWrapper = runComposer(new WordWrapperComposer(true), text, width);
    ComposerResult lineTruncator = runComposer(new LineTruncatorComposer(), text, width);
    assertEquals(List.of("コンピュータ上で文字"), lineTruncator.lines());
    List<String> wrapped = List.of("コンピュータ上で文字", "を扱う場合、典型的に", "は文字による通信を行", "う場合にその両端点で", "は、");
    assertEquals(wrapped, wordWrapper.lines());
    assertEquals(List.of(width, width, width, width, 4), wordWrapper.widths());
  }

  @Test
  public void line_composer_leading_whitespace_removal() {
    int width = 20;
    String text = "AAAAAAAAAAAAAAAAAAAA    AAA";
    ComposerResult wordWrapper = runComposer(new WordWrapperComposer(true), text, width);
    ComposerResult lineTruncator = runComposer(new LineTruncatorComposer(), text, width);
    assertEquals(List.of("AAAAAAAAAAAAAAAAAAAA", "AAA"), wordWrapper.lines());
    assertEquals(List.of("AAAAAAAAAAAAAAAAAAAA"), lineTruncator.lines());
  }

  @Test
  public void line_composer_lots_of_spaces() {
    int width = 20;
    String text = "                                                                     ";
    ComposerResult wordWrapper = runComposer(new WordWrapperComposer(true), text, width);
    ComposerResult lineTruncator = runComposer(new LineTruncatorComposer(), text, width);
    assertEquals(List.of(""), wordWrapper.lines());
    assertEquals(List.of("                    "), lineTruncator.lines());
  }

  @Test
  public void line_composer_char_plus_lots_of_spaces() {
    int width = 20;
    String text = "a                                                                     ";
    ComposerResult wordWrapper = runComposer(new WordWrapperComposer(true), text, width);
    ComposerResult lineTruncator = runComposer(new LineTruncatorComposer(), text, width);
    assertEquals(List.of("a", ""), wordWrapper.lines());
    assertEquals(List.of("a                   "), lineTruncator.lines());
  }

  @Test
  public void line_composer_word_wrapper_double_width_chars_mixed_with_spaces() {
    int width = 20;
    String text = "コンピュ ータ上で文字を扱う場合、 典型的には文 字による 通信を行 う場合にその両端点では、";
    ComposerResult wordWrapper = runComposer(new WordWrapperComposer(true), text, width);
    assertEquals(
        List.of("コンピュ", "ータ上で文字を扱う場", "合、 典型的には文", "字による 通信を行", "う場合にその両端点で", "は、"),
        wordWrapper.lines());
    assertEquals(List.of(8, 20, 17, 17, 20, 4), wordWrapper.widths());
  }

  @Test
  public void line_composer_word_wrapper_nbsp() {
    int width = 20;
    String text = "AAAAAAAAAAAAAAA AAAA AAA";
    ComposerResult wordWrapper = runComposer(new WordWrapperComposer(true), text, width);
    assertEquals(List.of("AAAAAAAAAAAAAAA", "AAAA AAA"), wordWrapper.lines());
    assertEquals(List.of(15, 8), wordWrapper.widths());

    // Ensure that if the character was a regular space, it would be wrapped differently.
    String textSpace = text.replace(' ', ' ');
    ComposerResult wordWrapperSpace = runComposer(new WordWrapperComposer(true), textSpace, width);
    assertEquals(List.of("AAAAAAAAAAAAAAA AAAA", "AAA"), wordWrapperSpace.lines());
    assertEquals(List.of(20, 3), wordWrapperSpace.widths());
  }

  @Test
  public void line_composer_word_wrapper_preserve_indentation() {
    int width = 20;
    String text = "AAAAAAAAAAAAAAAAAAAA    AAA";
    ComposerResult wordWrapper = runComposer(new WordWrapperComposer(false), text, width);
    assertEquals(List.of("AAAAAAAAAAAAAAAAAAAA", "   AAA"), wordWrapper.lines());
  }

  @Test
  public void line_composer_word_wrapper_preserve_indentation_with_wrap() {
    int width = 10;
    String text = "AAA AAA AAAAA AA AAAAAA\n B\n  C\n   D";
    ComposerResult wordWrapper = runComposer(new WordWrapperComposer(false), text, width);
    assertEquals(
        List.of("AAA AAA", "AAAAA AA", "AAAAAA", " B", "  C", "   D"), wordWrapper.lines());
  }

  @Test
  public void line_composer_word_wrapper_preserve_indentation_lots_of_whitespace() {
    int width = 10;
    String text = "               4 Indent\n                 must wrap!";
    ComposerResult wordWrapper = runComposer(new WordWrapperComposer(false), text, width);
    assertEquals(
        List.of("          ", "    4", "Indent", "          ", "      must", "wrap!"),
        wordWrapper.lines());
  }

  @Test
  public void line_composer_zero_width_at_end() {
    int width = 3;
    String line = "foo​";
    ComposerResult wordWrapper = runComposer(new WordWrapperComposer(true), line, width);
    ComposerResult lineTruncator = runComposer(new LineTruncatorComposer(), line, width);
    assertEquals(List.of("foo"), wordWrapper.lines());
    assertEquals(List.of("foo​"), lineTruncator.lines());
  }

  @Test
  public void line_composer_preserves_line_alignment() {
    int width = 20;
    Text textInput =
        Text.from(
            Line.from("Something that is left aligned.").withAlignment(HorizontalAlignment.Left),
            Line.from("This is right aligned and half short.")
                .withAlignment(HorizontalAlignment.Right),
            Line.from("This should sit in the center.").withAlignment(HorizontalAlignment.Center));

    ComposerResult wordWrapper = runComposer(new WordWrapperComposer(true), textInput, width);
    ComposerResult lineTruncator = runComposer(new LineTruncatorComposer(), textInput, width);

    assertEquals(
        List.of(
            HorizontalAlignment.Left,
            HorizontalAlignment.Left,
            HorizontalAlignment.Right,
            HorizontalAlignment.Right,
            HorizontalAlignment.Right,
            HorizontalAlignment.Center,
            HorizontalAlignment.Center),
        wordWrapper.alignments());
    assertEquals(
        List.of(HorizontalAlignment.Left, HorizontalAlignment.Right, HorizontalAlignment.Center),
        lineTruncator.alignments());
  }

  @Test
  public void line_composer_zero_width_white_space() {
    int width = 3;
    String line = "foo​bar";
    ComposerResult wordWrapper = runComposer(new WordWrapperComposer(true), line, width);
    assertEquals(List.of("foo", "bar"), wordWrapper.lines());
  }
}
