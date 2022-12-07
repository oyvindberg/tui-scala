package tui
package widgets

import tui.text.StyledGrapheme
import tui.widgets.reflow.{LineComposer, LineTruncator, WordWrapper}
import tui.widgets.reflowTests.Composer

object reflowTests {
  sealed trait Composer

  object Composer {

    case class WordWrapper(trim: Boolean) extends Composer

    case object LineTruncator extends Composer
  }
}

class reflowTests extends TuiTest {

  def run_composer(which: Composer, text: String, text_area_width: Int): (Array[String], Array[Int]) = {
    val style = Style.DEFAULT;
    val styled = UnicodeSegmentation.graphemes(text, true).map(g => StyledGrapheme(symbol = g, style));
    val composer: LineComposer =
      which match {
        case Composer.WordWrapper(trim) => WordWrapper(styled.iterator, text_area_width, trim)
        case Composer.LineTruncator     => LineTruncator(styled.iterator, text_area_width)
      }
    val lines = Array.newBuilder[String]
    val widths = Array.newBuilder[Int]
    var continue = true
    while (continue)
      composer.next_line() match {
        case None => continue = false
        case Some((styled, width)) =>
          val line = styled.map { case StyledGrapheme(symbol, _) => symbol.str }
          require(width <= text_area_width);
          lines += line.mkString("");
          widths += width;
      }
    (lines.result(), widths.result())
  }

  test("nine_composer_one_line") {
    val width = 40;
    ranges.range(1, width) { i =>
      val text = "a".repeat(i);
      val (word_wrapper, _) =
        run_composer(Composer.WordWrapper(trim = true), text, width);
      val (line_truncator, _) = run_composer(Composer.LineTruncator, text, width);
      val expected = Array(text);
      assert_eq(word_wrapper, expected);
      assert_eq(line_truncator, expected);
      ()
    }
    succeed
  }

  test("line_composer_short_lines") {
    val width = 20;
    val text =
      "abcdefg\nhijklmno\npabcdefg\nhijklmn\nopabcdefghijk\nlmnopabcd\n\n\nefghijklmno";
    val (word_wrapper, _) = run_composer(Composer.WordWrapper(trim = true), text, width);
    val (line_truncator, _) = run_composer(Composer.LineTruncator, text, width);

    val wrapped = text.split('\n')
    assert_eq(word_wrapper, wrapped);
    assert_eq(line_truncator, wrapped);
  }

  test("line_composer_long_word") {
    val width = 20;
    val text = "abcdefghijklmnopabcdefghijklmnopabcdefghijklmnopabcdefghijklmno";
    val (word_wrapper, _) =
      run_composer(Composer.WordWrapper(trim = true), text, width);
    val (line_truncator, _) = run_composer(Composer.LineTruncator, text, width);

    val wrapped = Array(
      text.substring(0, width),
      text.substring(width, width * 2),
      text.substring(width * 2, width * 3),
      text.substring(width * 3)
    )
    assert_eq(
      word_wrapper,
      wrapped,
      "WordWrapper should detect the line cannot be broken on word boundary and break it at line width limit."
    );
    assert_eq(line_truncator, Array(text.substring(0, width)));
  }

  test("line_composer_long_sentence") {
    val width = 20;
    val text =
      "abcd efghij klmnopabcd efgh ijklmnopabcdefg hijkl mnopab c d e f g h i j k l m n o";
    val text_multi_space =
      "abcd efghij    klmnopabcd efgh     ijklmnopabcdefg hijkl mnopab c d e f g h i j k l m n o";
    val (word_wrapper_single_space, _) =
      run_composer(Composer.WordWrapper(trim = true), text, width);
    val (word_wrapper_multi_space, _) = run_composer(
      Composer.WordWrapper(trim = true),
      text_multi_space,
      width
    );
    val (line_truncator, _) = run_composer(Composer.LineTruncator, text, width);

    val word_wrapped = Array(
      "abcd efghij",
      "klmnopabcd efgh",
      "ijklmnopabcdefg",
      "hijkl mnopab c d e f",
      "g h i j k l m n o"
    );
    assert_eq(word_wrapper_single_space, word_wrapped);
    assert_eq(word_wrapper_multi_space, word_wrapped);

    assert_eq(line_truncator, Array(text.substring(0, width)));
  }

  test("line_composer_zero_width") {
    val width = 0;
    val text = "abcd efghij klmnopabcd efgh ijklmnopabcdefg hijkl mnopab ";
    val (word_wrapper, _) = run_composer(Composer.WordWrapper(trim = true), text, width);
    val (line_truncator, _) = run_composer(Composer.LineTruncator, text, width);

    val expected: Array[String] = Array.empty;
    assert_eq(word_wrapper, expected);
    assert_eq(line_truncator, expected);
  }

  test("line_composer_max_line_width_of_1") {
    val width = 1;
    val text = "abcd efghij klmnopabcd efgh ijklmnopabcdefg hijkl mnopab ";
    val (word_wrapper, _) = run_composer(Composer.WordWrapper(trim = true), text, width);
    val (line_truncator, _) = run_composer(Composer.LineTruncator, text, width);

    val expected: Array[String] = UnicodeSegmentation
      .graphemes(text, true)
      .filter(g => g.str.exists(!_.isWhitespace))
      .map(_.str)
    assert_eq(word_wrapper, expected);
    assert_eq(line_truncator, Array("a"));
  }

  test("line_composer_max_line_width_of_1_double_width_characters") {
    val width = 1; // x
    val text = "コンピュータ上で文字を扱う場合、典型的には文字\naaaによる通信を行う場合にその両端点では、";
    val (word_wrapper, _) = run_composer(Composer.WordWrapper(trim = true), text, width);
    val (line_truncator, _) = run_composer(Composer.LineTruncator, text, width);
    assert_eq(word_wrapper, Array("", "a", "a", "a"));
    assert_eq(line_truncator, Array("", "a"));
  }

  /// Tests WordWrapper with words some of which exceed line length and some not.

  test("line_composer_word_wrapper_mixed_length") {
    val width = 20;
    val text = "abcd efghij klmnopabcdefghijklmnopabcdefghijkl mnopab cdefghi j klmno";
    val (word_wrapper, _) = run_composer(Composer.WordWrapper(trim = true), text, width);
    assert_eq(
      word_wrapper,
      Array(
        "abcd efghij",
        "klmnopabcdefghijklmn",
        "opabcdefghijkl",
        "mnopab cdefghi j",
        "klmno"
      )
    )
  }

  test("line_composer_double_width_chars") {
    val width = 20; // x
    val text = "コンピュータ上で文字を扱う場合、典型的には文字による通信を行う場合にその両端点では、";
    val (word_wrapper, word_wrapper_width) =
      run_composer(Composer.WordWrapper(trim = true), text, width);
    val (line_truncator, _) = run_composer(Composer.LineTruncator, text, width);
    assert_eq(line_truncator, Array("コンピュータ上で文字"));
    val wrapped = Array(
      "コンピュータ上で文字",
      "を扱う場合、典型的に",
      "は文字による通信を行",
      "う場合にその両端点で",
      "は、"
    );
    assert_eq(word_wrapper, wrapped);
    assert_eq(word_wrapper_width, Array(width, width, width, width, 4));
  }

  test("line_composer_leading_whitespace_removal") {
    val width = 20;
    val text = "AAAAAAAAAAAAAAAAAAAA    AAA";
    val (word_wrapper, _) = run_composer(Composer.WordWrapper(trim = true), text, width);
    val (line_truncator, _) = run_composer(Composer.LineTruncator, text, width);
    assert_eq(word_wrapper, Array("AAAAAAAAAAAAAAAAAAAA", "AAA"));
    assert_eq(line_truncator, Array("AAAAAAAAAAAAAAAAAAAA"));
  }

  /// Tests truncation of leading whitespace.

  test("line_composer_lots_of_spaces") {
    val width = 20;
    val text = "                                                                     ";
    val (word_wrapper, _) = run_composer(Composer.WordWrapper(trim = true), text, width);
    val (line_truncator, _) = run_composer(Composer.LineTruncator, text, width);
    assert_eq(word_wrapper, Array(""));
    assert_eq(line_truncator, Array("                    "));
  }

  /// Tests an input starting with a letter, folowed by spaces - some of the behaviour is
  /// incidental.

  test("line_composer_char_plus_lots_of_spaces") {
    val width = 20;
    val text = "a                                                                     ";
    val (word_wrapper, _) = run_composer(Composer.WordWrapper(trim = true), text, width);
    val (line_truncator, _) = run_composer(Composer.LineTruncator, text, width);
    // What's happening below is: the first line gets consumed, trailing spaces discarded,
    // after 20 of which a word break occurs (probably shouldn't). The second line break
    // discards all whitespace. The result should probably be Array["a"] but it doesn't matter
    // that much.
    assert_eq(word_wrapper, Array("a", ""));
    assert_eq(line_truncator, Array("a                   "));
  }

  test("line_composer_word_wrapper_double_width_chars_mixed_with_spaces") {
    val width = 20;
    // Japanese seems not to use spaces but we should break on spaces anyway... We're using it
    // to test double-width chars.
    // You are more than welcome to add word boundary detection based of alterations of
    // hiragana and katakana...
    // This happens to also be a test case for mixed width because regular spaces are single width.
    val text = "コンピュ ータ上で文字を扱う場合、 典型的には文 字による 通信を行 う場合にその両端点では、";
    val (word_wrapper, word_wrapper_width) =
      run_composer(Composer.WordWrapper(trim = true), text, width);
    assert_eq(
      word_wrapper,
      Array(
        "コンピュ",
        "ータ上で文字を扱う場",
        "合、 典型的には文",
        "字による 通信を行",
        "う場合にその両端点で",
        "は、"
      )
    );
    // Odd-sized lines have a space in them.
    assert_eq(word_wrapper_width, Array(8, 20, 17, 17, 20, 4));
  }

  /// Ensure words separated by nbsp are wrapped as if they were a single one.

  test("line_composer_word_wrapper_nbsp") {
    val width = 20;
    val text = "AAAAAAAAAAAAAAA AAAA\u00a0AAA";
    val (word_wrapper, _) = run_composer(Composer.WordWrapper(trim = true), text, width);
    assert_eq(word_wrapper, Array("AAAAAAAAAAAAAAA", "AAAA\u00a0AAA"));

    // Ensure that if the character was a regular space, it would be wrapped differently.
    val text_space = text.replace("\u00a0", " ");
    val (word_wrapper_space, _) =
      run_composer(Composer.WordWrapper(trim = true), text_space, width);
    assert_eq(word_wrapper_space, Array("AAAAAAAAAAAAAAA AAAA", "AAA"));
  }

  test("line_composer_word_wrapper_preserve_indentation") {
    val width = 20;
    val text = "AAAAAAAAAAAAAAAAAAAA    AAA";
    val (word_wrapper, _) = run_composer(Composer.WordWrapper(trim = false), text, width);
    assert_eq(word_wrapper, Array("AAAAAAAAAAAAAAAAAAAA", "   AAA"));
  }

  test("line_composer_word_wrapper_preserve_indentation_with_wrap") {
    val width = 10;
    val text = "AAA AAA AAAAA AA AAAAAA\n B\n  C\n   D";
    val (word_wrapper, _) = run_composer(Composer.WordWrapper(trim = false), text, width);
    assert_eq(
      word_wrapper,
      Array("AAA AAA", "AAAAA AA", "AAAAAA", " B", "  C", "   D")
    );
  }

  test("line_composer_word_wrapper_preserve_indentation_lots_of_whitespace") {
    val width = 10;
    val text = "               4 Indent\n                 must wrap!";
    val (word_wrapper, _) = run_composer(Composer.WordWrapper(trim = false), text, width);
    assert_eq(
      word_wrapper,
      Array(
        "          ",
        "    4",
        "Indent",
        "          ",
        "      must",
        "wrap!"
      )
    );
  }
}
