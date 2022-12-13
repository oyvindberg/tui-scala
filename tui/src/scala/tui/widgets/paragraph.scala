package tui
package widgets

import tui.internal.saturating._
import tui.widgets.reflow.{LineComposer, LineTruncator, WordWrapper}

object paragraph {
  def get_line_offset(line_width: Int, text_area_width: Int, alignment: Alignment): Int =
    alignment match {
      case Alignment.Center => (text_area_width / 2).saturating_sub_unsigned(line_width / 2)
      case Alignment.Right  => text_area_width.saturating_sub_unsigned(line_width)
      case Alignment.Left   => 0
    }

  /// Describes how to wrap text across lines.
  ///
  /// ## Examples
  ///
  /// ```
  /// # use tui::widgets::{Paragraph, Wrap};
  /// # use tui::text::Text;
  /// let bullet_points = Text::from(r#"Some indented points:
  ///     - First thing goes here and is long so that it wraps
  ///     - Here is another point that is long enough to wrap"#);
  ///
  /// // With leading spaces trimmed (window width of 30 chars):
  /// Paragraph::new(bullet_points.clone()).wrap(Wrap { trim: true });
  /// // Some indented points:
  /// // - First thing goes here and is
  /// // long so that it wraps
  /// // - Here is another point that
  /// // is long enough to wrap
  ///
  /// // But without trimming, indentation is preserved:
  /// Paragraph::new(bullet_points).wrap(Wrap { trim: false });
  /// // Some indented points:
  /// //     - First thing goes here
  /// // and is long so that it wraps
  /// //     - Here is another point
  /// // that is long enough to wrap
  /// ```
  case class Wrap(
      /// Should leading whitespace be trimmed
      trim: Boolean
  )

  /// A widget to display some text.
  ///
  /// # Examples
  ///
  /// ```
  /// # use tui::text::{Text, Spans, Span};
  /// # use tui::widgets::{Block, Borders, Paragraph, Wrap};
  /// # use tui::style::{Style, Color, Modifier};
  /// # use tui::layout::{Alignment};
  /// let text = vec![
  ///     Spans::from(vec![
  ///         Span::raw("First"),
  ///         Span::styled("line",Style::DEFAULT.add_modifier(Modifier::ITALIC)),
  ///         Span::raw("."),
  ///     ]),
  ///     Spans::from(Span::styled("Second line", Style::DEFAULT.fg(Color::Red))),
  /// ];
  /// Paragraph::new(text)
  ///     .block(Block::default().title("Paragraph").borders(Borders::ALL))
  ///     .style(Style::DEFAULT.fg(Color::White).bg(Color::Black))
  ///     .alignment(Alignment::Center)
  ///     .wrap(Wrap { trim: true });
  /// ```
  case class Paragraph(
      /// A block to wrap the widget in
      block: Option[Block] = None,
      /// Widget style
      style: Style = Style.DEFAULT,
      /// How to wrap the text
      wrap: Option[Wrap] = None,
      /// The text to display
      text: Text,
      /// Scroll
      scroll: (Int, Int) = (0, 0),
      /// Alignment of the text
      alignment: Alignment = Alignment.Left
  ) extends Widget {

    def render(area: Rect, buf: Buffer): Unit = {
      buf.set_style(area, this.style)
      val text_area = block match {
        case Some(b) =>
          val inner_area = b.inner(area)
          b.render(area, buf)
          inner_area
        case None => area
      }

      if (text_area.height < 1) {
        return
      }

      val styled: Array[StyledGrapheme] =
        text.lines.flatMap { case Spans(spans: Array[Span]) =>
          spans.flatMap(span => span.styled_graphemes(this.style)) :+ StyledGrapheme(Grapheme("\n"), this.style)
        }

      val line_composer: LineComposer =
        wrap match {
          case Some(Wrap(trim)) => WordWrapper(styled.iterator, text_area.width, trim)
          case None =>
            val line_composer = LineTruncator(styled.iterator, text_area.width)
            alignment match {
              case Alignment.Left => line_composer.copy(horizontal_offset = this.scroll._2)
              case _              => line_composer
            }
        }

      var y = 0
      var continue = true
      while (continue)
        line_composer.next_line() match {
          case None => continue = false
          case Some((current_line, current_line_width)) =>
            if (y >= this.scroll._1) {
              var x = get_line_offset(current_line_width, text_area.width, this.alignment)
              current_line.foreach { case StyledGrapheme(symbol, style) =>
                // If the symbol is empty, the last char which rendered last time will
                // leave on the line. It's a quick fix.
                val newSymbol = if (symbol.str.isEmpty) " " else symbol.str

                buf
                  .get(text_area.left + x, text_area.top + y - this.scroll._1)
                  .set_symbol(newSymbol)
                  .set_style(style)

                x += symbol.width
              }
            }
            y += 1
            if (y >= text_area.height + this.scroll._1) {
              continue = false
            }
        }
    }
  }
}
