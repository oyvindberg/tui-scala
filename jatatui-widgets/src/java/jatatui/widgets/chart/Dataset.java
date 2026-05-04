package jatatui.widgets.chart;

import jatatui.core.style.Style;
import jatatui.core.style.Stylize;
import jatatui.core.symbols.Marker;
import jatatui.core.text.Line;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/// A group of data points to be drawn on a [Chart].
///
/// A dataset can be [#withName(Line)] (only named datasets show in the legend), and configured
/// with [#withData(List)], [#withMarker(Marker)], and [#withGraphType(GraphType)]. Mutators
/// return a new `Dataset`.
public final class Dataset implements Stylize<Dataset> {

  /// Optional name (used in the legend if shown).
  public final Optional<Line> name;

  /// Data points (immutable list).
  public final List<Point> data;

  /// Symbol used for each point.
  public final Marker marker;

  /// Graph type (scatter / line / bar).
  public final GraphType graphType;

  /// Style used to plot this dataset.
  public final Style style;

  private Dataset(Optional<Line> name, List<Point> data, Marker marker, GraphType graphType, Style style) {
    this.name = name;
    this.data = List.copyOf(data);
    this.marker = marker;
    this.graphType = graphType;
    this.style = style;
  }

  // ---- Constructors ----

  /// Returns an empty dataset (no name, no data, default marker, scatter, default style).
  public static Dataset empty() {
    return new Dataset(
        Optional.empty(),
        List.of(),
        Marker.defaultMarker(),
        GraphType.defaultType(),
        Style.empty());
  }

  // ---- Builders ----

  /// Sets the dataset name from a [Line].
  public Dataset withName(Line name) {
    return new Dataset(Optional.of(name), data, marker, graphType, style);
  }

  /// Sets the dataset name from a string.
  public Dataset withName(String name) {
    return withName(Line.from(name));
  }

  /// Sets the dataset's data points.
  public Dataset withData(List<Point> data) {
    return new Dataset(name, data, marker, graphType, style);
  }

  /// Sets the dataset's data points.
  public Dataset withData(Point... data) {
    return withData(List.of(data));
  }

  /// Sets the marker symbol.
  public Dataset withMarker(Marker marker) {
    return new Dataset(name, data, marker, graphType, style);
  }

  /// Sets the graph type.
  public Dataset withGraphType(GraphType graphType) {
    return new Dataset(name, data, marker, graphType, style);
  }

  /// Sets the style.
  public Dataset withStyle(Style style) {
    return new Dataset(name, data, marker, graphType, style);
  }

  // ---- Stylize / Styled ----

  @Override
  public Style style() {
    return style;
  }

  @Override
  public Dataset setStyle(Style style) {
    return withStyle(style);
  }

  // ---- equals / hashCode ----

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Dataset other)) return false;
    return name.equals(other.name)
        && data.equals(other.data)
        && marker == other.marker
        && graphType == other.graphType
        && style.equals(other.style);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, data, marker, graphType, style);
  }
}
