package jatatui.widgets.paragraph;

/// Scroll offset for a [Paragraph].
///
/// `y` is the number of lines to scroll vertically; `x` is the number of cells to scroll
/// horizontally. The scroll offset is applied after the text is wrapped and aligned.
///
/// Note: this matches the upstream tuple `(Vertical, Horizontal)` ordering used by
/// `Paragraph::scroll` in `ratatui_widgets::paragraph` — the `y` (vertical) field comes first.
/// Internally upstream stores a `Position`; here we model it as a dedicated record per the
/// playbook rule that tuples get a domain-meaningful name.
public record Scroll(int y, int x) {

  /// The zero scroll offset.
  public static final Scroll ZERO = new Scroll(0, 0);
}
