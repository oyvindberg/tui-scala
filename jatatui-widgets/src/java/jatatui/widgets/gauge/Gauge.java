package jatatui.widgets.gauge;

import jatatui.core.buffer.Buffer;
import jatatui.core.buffer.Cell;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.style.Style;
import jatatui.core.style.Styled;
import jatatui.core.style.Stylize;
import jatatui.core.symbols.Block;
import jatatui.core.text.Span;
import jatatui.core.widgets.Widget;
import java.util.Optional;

/// A widget to display a progress bar.
///
/// Mirrors `ratatui_widgets::gauge::Gauge` (v0.30).
///
/// A `Gauge` renders a bar filled according to the value given to [#withPercent(int)] or
/// [#withRatio(double)]. The bar width and height are defined by the [Rect] it is rendered in.
///
/// The associated label is always centered horizontally and vertically. If not set with
/// [#withLabel(Span)], the label is the percentage of the bar filled.
public final class Gauge implements Widget, Stylize<Gauge> {

  private final Optional<jatatui.widgets.block.Block> block;
  private final double ratio;
  private final Optional<Span> label;
  private final boolean useUnicode;
  private final Style style;
  private final Style gaugeStyle;

  private Gauge(
      Optional<jatatui.widgets.block.Block> block,
      double ratio,
      Optional<Span> label,
      boolean useUnicode,
      Style style,
      Style gaugeStyle) {
    this.block = block;
    this.ratio = ratio;
    this.label = label;
    this.useUnicode = useUnicode;
    this.style = style;
    this.gaugeStyle = gaugeStyle;
  }

  /// Returns a default `Gauge` (ratio=0, no block, no label, no unicode, default styles).
  public static Gauge empty() {
    return new Gauge(
        Optional.empty(), 0.0, Optional.empty(), false, Style.empty(), Style.empty());
  }

  // ---- Builder methods ----

  /// Surrounds the `Gauge` with a [Block].
  public Gauge withBlock(jatatui.widgets.block.Block block) {
    return new Gauge(Optional.of(block), ratio, label, useUnicode, style, gaugeStyle);
  }

  /// Sets the bar progression from a percentage. Throws [IllegalArgumentException] if `percent`
  /// is not between 0 and 100 inclusively.
  public Gauge withPercent(int percent) {
    if (percent < 0 || percent > 100) {
      throw new IllegalArgumentException(
          "Percentage should be between 0 and 100 inclusively.");
    }
    return new Gauge(block, percent / 100.0, label, useUnicode, style, gaugeStyle);
  }

  /// Sets the bar progression from a ratio (float). Throws [IllegalArgumentException] if `ratio`
  /// is not between 0 and 1 inclusively.
  public Gauge withRatio(double ratio) {
    if (!(ratio >= 0.0 && ratio <= 1.0)) {
      throw new IllegalArgumentException("Ratio should be between 0 and 1 inclusively.");
    }
    return new Gauge(block, ratio, label, useUnicode, style, gaugeStyle);
  }

  /// Sets the label to display in the center of the bar.
  public Gauge withLabel(Span label) {
    return new Gauge(block, ratio, Optional.of(label), useUnicode, style, gaugeStyle);
  }

  /// Sets the label from a string (uses default style).
  public Gauge withLabel(String label) {
    return withLabel(Span.raw(label));
  }

  /// Sets the widget style.
  public Gauge withStyle(Style style) {
    return new Gauge(block, ratio, label, useUnicode, style, gaugeStyle);
  }

  /// Sets the style of the bar.
  public Gauge withGaugeStyle(Style gaugeStyle) {
    return new Gauge(block, ratio, label, useUnicode, style, gaugeStyle);
  }

  /// Sets whether to use unicode characters to display the progress bar (8 extra fractional parts
  /// per cell).
  public Gauge withUseUnicode(boolean useUnicode) {
    return new Gauge(block, ratio, label, useUnicode, style, gaugeStyle);
  }

  // ---- Accessors ----

  public double ratio() {
    return ratio;
  }

  public Style gaugeStyle() {
    return gaugeStyle;
  }

  // ---- Stylize / Styled ----

  @Override
  public Style style() {
    return style;
  }

  @Override
  public Gauge setStyle(Style style) {
    return withStyle(style);
  }

  // ---- Widget render ----

  @Override
  public void render(Rect area, Buffer buf) {
    buf.setStyle(area, style);
    block.ifPresent(b -> b.render(area, buf));
    Rect inner = block.map(b -> b.inner(area)).orElse(area);
    renderGauge(inner, buf);
  }

  private void renderGauge(Rect gaugeArea, Buffer buf) {
    if (gaugeArea.isEmpty()) {
      return;
    }
    buf.setStyle(gaugeArea, gaugeStyle);

    // compute label value and its position; label is centered
    Span defaultLabel = Span.raw(((long) Math.round(ratio * 100.0)) + "%");
    Span effectiveLabel = label.orElse(defaultLabel);
    int clampedLabelWidth = Math.min(gaugeArea.width(), effectiveLabel.width());
    int labelCol = gaugeArea.left() + (gaugeArea.width() - clampedLabelWidth) / 2;
    int labelRow = gaugeArea.top() + gaugeArea.height() / 2;

    // the gauge will be filled proportionally to the ratio
    double filledWidth = gaugeArea.width() * ratio;
    int end =
        useUnicode
            ? gaugeArea.left() + (int) Math.floor(filledWidth)
            : gaugeArea.left() + (int) Math.round(filledWidth);
    Color gaugeFg = gaugeStyle.fg().orElse(Color.RESET);
    Color gaugeBg = gaugeStyle.bg().orElse(Color.RESET);
    for (int y = gaugeArea.top(); y < gaugeArea.bottom(); y++) {
      for (int x = gaugeArea.left(); x < end; x++) {
        Cell cell = buf.cellAt(x, y);
        if (x < labelCol || x > labelCol + clampedLabelWidth || y != labelRow) {
          cell.setSymbol(Block.FULL).setFg(gaugeFg).setBg(gaugeBg);
        } else {
          cell.setSymbol(" ").setFg(gaugeBg).setBg(gaugeFg);
        }
      }
      if (useUnicode && ratio < 1.0 && end >= gaugeArea.left() && end < gaugeArea.right()) {
        buf.cellAt(end, y).setSymbol(getUnicodeBlock(filledWidth - Math.floor(filledWidth)));
      }
    }
    // render the label
    buf.setSpan(labelCol, labelRow, effectiveLabel, clampedLabelWidth);
  }

  private static String getUnicodeBlock(double frac) {
    int n = (int) Math.round(frac * 8.0);
    return switch (n) {
      case 1 -> Block.ONE_EIGHTH;
      case 2 -> Block.ONE_QUARTER;
      case 3 -> Block.THREE_EIGHTHS;
      case 4 -> Block.HALF;
      case 5 -> Block.FIVE_EIGHTHS;
      case 6 -> Block.THREE_QUARTERS;
      case 7 -> Block.SEVEN_EIGHTHS;
      case 8 -> Block.FULL;
      default -> " ";
    };
  }
}
