package jatatui.widgets.table;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Rect;
import jatatui.core.style.Style;
import jatatui.core.style.Stylize;
import jatatui.core.text.Text;

/// A [TableCell] contains the [Text] to be displayed in a [Row] of a [Table].
///
/// You can apply a [Style] to the cell using [#withStyle(Style)]. This will set the style for the
/// entire area of the cell. Any [Style] set on the [Text] content will be combined with the
/// [Style] of the cell by adding the [Style] of the [Text] content to the [Style] of the cell.
/// Styles set on the text content will only affect the content.
///
/// You can use [Text#withAlignment(jatatui.core.layout.HorizontalAlignment)] when creating a cell
/// to align its content.
///
/// `TableCell` implements [Stylize] which means you can use style shorthands from the [Stylize]
/// interface to set the style of the cell concisely.
///
/// Mirrors `ratatui_widgets::table::Cell` (v0.30). Renamed to `TableCell` in Java to avoid clashing
/// with [jatatui.core.buffer.Cell].
public record TableCell(Text content, Style style) implements Stylize<TableCell> {

  /// Creates a new [TableCell] with the default style.
  public static TableCell empty() {
    return new TableCell(Text.empty(), Style.empty());
  }

  /// Creates a new [TableCell] with the given content and the default style.
  public static TableCell of(Text content) {
    return new TableCell(content, Style.empty());
  }

  /// Convenience: creates a new [TableCell] from a string with the default style.
  public static TableCell of(String content) {
    return new TableCell(Text.from(content), Style.empty());
  }

  /// Returns a copy with the given content set.
  public TableCell withContent(Text content) {
    return new TableCell(content, style);
  }

  /// Returns a copy with the given content (built from a string) set.
  public TableCell withContent(String content) {
    return new TableCell(Text.from(content), style);
  }

  /// Returns a copy with the given style set, replacing the current style.
  public TableCell withStyle(Style style) {
    return new TableCell(content, style);
  }

  // ---- Styled / Stylize wiring ----

  @Override
  public TableCell setStyle(Style style) {
    return withStyle(style);
  }

  // ---- Rendering ----

  /// Renders this cell into the given area of the buffer.
  ///
  /// The cell's style is applied to the entire area, then the contained [Text] is rendered on top.
  void render(Rect area, Buffer buf) {
    buf.setStyle(area, style);
    TextRenderer.renderText(content, area, buf);
  }
}
