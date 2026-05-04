package jatatui.widgets.list;

import jatatui.core.style.Style;
import jatatui.core.style.Stylize;
import jatatui.core.text.Line;
import jatatui.core.text.Span;
import jatatui.core.text.Text;
import java.util.List;
import java.util.Objects;

/// A single item in a [List].
///
/// The item's height is defined by the number of lines it contains. This can be queried using
/// [#height()]. Similarly, [#width()] will return the maximum width of all lines.
///
/// You can set the style of an item with [#withStyle(Style)] or using the [Stylize] trait.
/// This [Style] will be combined with the [Style] of the inner [Text]. The [Style] of the [Text]
/// will be added to the [Style] of the [ListItem].
///
/// You can also align a [ListItem] by aligning its underlying [Text] and [Line]s. For that,
/// see [Text#withAlignment(jatatui.core.layout.HorizontalAlignment)] and
/// [Line#withAlignment(jatatui.core.layout.HorizontalAlignment)]. On a multi-line text, one [Line]
/// can override the alignment by setting it explicitly.
///
/// Mirrors `ratatui_widgets::list::ListItem` (v0.30).
public final class ListItem implements Stylize<ListItem> {

  /// The textual content of this item.
  public final Text content;

  /// The style applied to the entire item.
  public final Style style;

  private ListItem(Text content, Style style) {
    this.content = content;
    this.style = style;
  }

  // ---- Construction ----

  /// Creates a new [ListItem] with the given [Text] content and the default style.
  public static ListItem of(Text content) {
    return new ListItem(content, Style.empty());
  }

  /// Creates a new [ListItem] from a string, parsed via [Text#raw(String)].
  ///
  /// Newlines split the string into multiple lines, mirroring upstream's `ListItem::new("a\nb")`.
  public static ListItem of(String content) {
    return new ListItem(Text.raw(content), Style.empty());
  }

  /// Creates a new [ListItem] from a single [Span].
  public static ListItem of(Span span) {
    return new ListItem(Text.from(span), Style.empty());
  }

  /// Creates a new [ListItem] from a single [Line].
  public static ListItem of(Line line) {
    return new ListItem(Text.from(line), Style.empty());
  }

  /// Creates a new [ListItem] from a list of [Line]s.
  public static ListItem ofLines(List<Line> lines) {
    return new ListItem(Text.fromLines(lines), Style.empty());
  }

  // ---- Fluent setters ----

  /// Returns a copy with the style replaced.
  public ListItem withStyle(Style style) {
    return new ListItem(content, style);
  }

  /// Returns a copy with the content replaced.
  public ListItem withContent(Text content) {
    return new ListItem(content, style);
  }

  // ---- Geometry ----

  /// Returns the item height (number of lines in [#content]).
  public int height() {
    return content.height();
  }

  /// Returns the maximum line width of [#content].
  public int width() {
    return content.width();
  }

  // ---- Styled / Stylize wiring ----

  @Override
  public Style style() {
    return style;
  }

  @Override
  public ListItem setStyle(Style style) {
    return withStyle(style);
  }

  // ---- Equality / hash / debug ----

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ListItem other)) return false;
    return content.equals(other.content) && style.equals(other.style);
  }

  @Override
  public int hashCode() {
    return Objects.hash(content, style);
  }

  @Override
  public String toString() {
    return "ListItem{content=" + content + ", style=" + style + "}";
  }
}
