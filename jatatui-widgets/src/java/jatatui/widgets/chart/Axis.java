package jatatui.widgets.chart;

import jatatui.core.layout.HorizontalAlignment;
import jatatui.core.style.Style;
import jatatui.core.style.Stylize;
import jatatui.core.text.Line;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/// An X or Y axis for the [Chart] widget.
///
/// An axis can have a [#title] which will be displayed at the end of the axis. For an X axis
/// this is the right; for a Y axis this is the top. Bounds and labels are configurable via
/// [#withBounds(Bounds)] and [#withLabels(List)] respectively. Mutators return a new `Axis`.
public final class Axis implements Stylize<Axis> {

  /// Bounds for the axis: `[lo, hi]`. Replaces upstream's `[f64; 2]` array per the project's
  /// "tuples get dedicated record types" rule.
  public record Bounds(double lo, double hi) {
    /// The default bounds: `[0.0, 0.0]`.
    public static final Bounds DEFAULT = new Bounds(0.0, 0.0);

    /// Returns the bounds as a `[lo, hi]` array.
    public double[] toArray() {
      return new double[] {lo, hi};
    }
  }

  /// Optional axis title.
  public final Optional<Line> title;

  /// Bounds for the axis (data points outside these limits will not be represented).
  public final Bounds bounds;

  /// Labels to display along the axis (left-to-right for X axis, bottom-to-top for Y axis).
  public final List<Line> labels;

  /// Style used to draw the axis itself.
  public final Style style;

  /// Alignment of the labels.
  public final HorizontalAlignment labelsAlignment;

  private Axis(
      Optional<Line> title,
      Bounds bounds,
      List<Line> labels,
      Style style,
      HorizontalAlignment labelsAlignment) {
    this.title = title;
    this.bounds = bounds;
    this.labels = List.copyOf(labels);
    this.style = style;
    this.labelsAlignment = labelsAlignment;
  }

  // ---- Constructors ----

  /// Returns a new empty `Axis` (no title, default bounds, no labels, default style, left
  /// alignment).
  public static Axis empty() {
    return new Axis(
        Optional.empty(), Bounds.DEFAULT, List.of(), Style.empty(), HorizontalAlignment.Left);
  }

  // ---- Builders ----

  /// Sets the axis title from a [Line].
  public Axis withTitle(Line title) {
    return new Axis(Optional.of(title), bounds, labels, style, labelsAlignment);
  }

  /// Sets the axis title from a string.
  public Axis withTitle(String title) {
    return withTitle(Line.from(title));
  }

  /// Sets the bounds.
  public Axis withBounds(Bounds bounds) {
    return new Axis(title, bounds, labels, style, labelsAlignment);
  }

  /// Convenience overload setting bounds from `lo`/`hi`.
  public Axis withBounds(double lo, double hi) {
    return withBounds(new Bounds(lo, hi));
  }

  /// Sets the labels.
  public Axis withLabels(List<Line> labels) {
    return new Axis(title, bounds, labels, style, labelsAlignment);
  }

  /// Convenience overload setting labels from strings (each becomes a [Line]).
  public Axis withLabels(String... labels) {
    List<Line> lines = new ArrayList<>(labels.length);
    for (String s : labels) lines.add(Line.from(s));
    return withLabels(lines);
  }

  /// Convenience overload setting labels as varargs of [Line].
  public Axis withLabels(Line... labels) {
    return withLabels(List.of(labels));
  }

  /// Sets the axis style.
  public Axis withStyle(Style style) {
    return new Axis(title, bounds, labels, style, labelsAlignment);
  }

  /// Sets the labels alignment.
  public Axis withLabelsAlignment(HorizontalAlignment alignment) {
    return new Axis(title, bounds, labels, style, alignment);
  }

  // ---- Stylize / Styled ----

  @Override
  public Style style() {
    return style;
  }

  @Override
  public Axis setStyle(Style style) {
    return withStyle(style);
  }

  // ---- equals / hashCode ----

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Axis other)) return false;
    return title.equals(other.title)
        && bounds.equals(other.bounds)
        && labels.equals(other.labels)
        && style.equals(other.style)
        && labelsAlignment == other.labelsAlignment;
  }

  @Override
  public int hashCode() {
    return Objects.hash(title, bounds, labels, style, labelsAlignment);
  }
}
