package jatatui.widgets.gauge;

import jatatui.core.buffer.Buffer;
import jatatui.core.buffer.Cell;
import jatatui.core.layout.Position;
import jatatui.core.layout.Rect;
import jatatui.core.style.Style;
import jatatui.core.style.Stylize;
import jatatui.core.symbols.Line;
import jatatui.core.widgets.Widget;
import java.util.Optional;

/// A compact widget to display a progress bar over a single thin line.
///
/// Mirrors `ratatui_widgets::gauge::LineGauge` (v0.30).
///
/// Unlike [Gauge], only the width can be defined by the rendering [Rect]. The height is always 1.
/// The associated label is always left-aligned. If not set with
// [#withLabel(jatatui.core.text.Line)]
/// the label is the percentage of the bar filled.
public final class LineGauge implements Widget, Stylize<LineGauge> {

  private final Optional<jatatui.widgets.block.Block> block;
  private final double ratio;
  private final Optional<jatatui.core.text.Line> label;
  private final Style style;
  private final String filledSymbol;
  private final String unfilledSymbol;
  private final Style filledStyle;
  private final Style unfilledStyle;

  private LineGauge(
      Optional<jatatui.widgets.block.Block> block,
      double ratio,
      Optional<jatatui.core.text.Line> label,
      Style style,
      String filledSymbol,
      String unfilledSymbol,
      Style filledStyle,
      Style unfilledStyle) {
    this.block = block;
    this.ratio = ratio;
    this.label = label;
    this.style = style;
    this.filledSymbol = filledSymbol;
    this.unfilledSymbol = unfilledSymbol;
    this.filledStyle = filledStyle;
    this.unfilledStyle = unfilledStyle;
  }

  /// Returns a default `LineGauge` (ratio=0, no block, no label, horizontal line for both
  /// filled and unfilled, default styles).
  public static LineGauge empty() {
    return new LineGauge(
        Optional.empty(),
        0.0,
        Optional.empty(),
        Style.empty(),
        Line.HORIZONTAL,
        Line.HORIZONTAL,
        Style.empty(),
        Style.empty());
  }

  // ---- Builder methods ----

  /// Surrounds the `LineGauge` with a [jatatui.widgets.block.Block].
  public LineGauge withBlock(jatatui.widgets.block.Block block) {
    return new LineGauge(
        Optional.of(block),
        ratio,
        label,
        style,
        filledSymbol,
        unfilledSymbol,
        filledStyle,
        unfilledStyle);
  }

  /// Sets the bar progression from a ratio (float). Throws [IllegalArgumentException] if `ratio`
  /// is not between 0 and 1 inclusively.
  public LineGauge withRatio(double ratio) {
    if (!(ratio >= 0.0 && ratio <= 1.0)) {
      throw new IllegalArgumentException("Ratio should be between 0 and 1 inclusively.");
    }
    return new LineGauge(
        block, ratio, label, style, filledSymbol, unfilledSymbol, filledStyle, unfilledStyle);
  }

  /// Sets the symbol for the filled part of the gauge.
  public LineGauge withFilledSymbol(String filledSymbol) {
    return new LineGauge(
        block, ratio, label, style, filledSymbol, unfilledSymbol, filledStyle, unfilledStyle);
  }

  /// Sets the symbol for the unfilled part of the gauge.
  public LineGauge withUnfilledSymbol(String unfilledSymbol) {
    return new LineGauge(
        block, ratio, label, style, filledSymbol, unfilledSymbol, filledStyle, unfilledStyle);
  }

  /// Sets the label to display.
  public LineGauge withLabel(jatatui.core.text.Line label) {
    return new LineGauge(
        block,
        ratio,
        Optional.of(label),
        style,
        filledSymbol,
        unfilledSymbol,
        filledStyle,
        unfilledStyle);
  }

  /// Sets the label from a string.
  public LineGauge withLabel(String label) {
    return withLabel(jatatui.core.text.Line.from(label));
  }

  /// Sets the widget style.
  public LineGauge withStyle(Style style) {
    return new LineGauge(
        block, ratio, label, style, filledSymbol, unfilledSymbol, filledStyle, unfilledStyle);
  }

  /// Sets the style of filled part of the bar.
  public LineGauge withFilledStyle(Style filledStyle) {
    return new LineGauge(
        block, ratio, label, style, filledSymbol, unfilledSymbol, filledStyle, unfilledStyle);
  }

  /// Sets the style of the unfilled part of the bar.
  public LineGauge withUnfilledStyle(Style unfilledStyle) {
    return new LineGauge(
        block, ratio, label, style, filledSymbol, unfilledSymbol, filledStyle, unfilledStyle);
  }

  // ---- Accessors ----

  public String filledSymbol() {
    return filledSymbol;
  }

  public String unfilledSymbol() {
    return unfilledSymbol;
  }

  public Style filledStyle() {
    return filledStyle;
  }

  public Style unfilledStyle() {
    return unfilledStyle;
  }

  // ---- Stylize / Styled ----

  @Override
  public Style style() {
    return style;
  }

  @Override
  public LineGauge setStyle(Style style) {
    return withStyle(style);
  }

  // ---- Widget render ----

  @Override
  public void render(Rect area, Buffer buf) {
    buf.setStyle(area, style);
    block.ifPresent(b -> b.render(area, buf));
    Rect gaugeArea = block.map(b -> b.inner(area)).orElse(area);
    if (gaugeArea.isEmpty()) {
      return;
    }

    jatatui.core.text.Line defaultLabel =
        jatatui.core.text.Line.from(String.format("%3.0f%%", ratio * 100.0));
    jatatui.core.text.Line effectiveLabel = label.orElse(defaultLabel);
    Position end =
        buf.setLine(gaugeArea.left(), gaugeArea.top(), effectiveLabel, gaugeArea.width());
    int col = end.x();
    int row = end.y();
    int start = col + 1;
    if (start >= gaugeArea.right()) {
      return;
    }

    int filledEnd = start + (int) Math.floor(Math.max(0, gaugeArea.right() - start) * ratio);
    for (int x = start; x < filledEnd; x++) {
      Cell cell = buf.cellAt(x, row);
      cell.setSymbol(filledSymbol).setStyle(filledStyle);
    }
    for (int x = filledEnd; x < gaugeArea.right(); x++) {
      Cell cell = buf.cellAt(x, row);
      cell.setSymbol(unfilledSymbol).setStyle(unfilledStyle);
    }
  }
}
