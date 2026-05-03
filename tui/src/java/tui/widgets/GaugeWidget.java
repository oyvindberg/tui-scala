package tui.widgets;

import java.util.Optional;
import tui.Buffer;
import tui.Color;
import tui.Rect;
import tui.Span;
import tui.Style;
import tui.Symbols;
import tui.Widget;
import tui.internal.Ranges;

/// A widget to display a task progress.
public final class GaugeWidget implements Widget {
  public final Optional<BlockWidget> block;
  public final Ratio ratio;
  public final Optional<Span> label;
  public final boolean useUnicode;
  public final Style style;
  public final Style gaugeStyle;

  public GaugeWidget(
      Optional<BlockWidget> block,
      Ratio ratio,
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

  public static GaugeWidget empty() {
    return new GaugeWidget(
        Optional.empty(), Ratio.Zero, Optional.empty(), false, Style.DEFAULT, Style.DEFAULT);
  }

  public GaugeWidget withBlock(BlockWidget b) {
    return new GaugeWidget(Optional.of(b), ratio, label, useUnicode, style, gaugeStyle);
  }

  public GaugeWidget withRatio(Ratio r) {
    return new GaugeWidget(block, r, label, useUnicode, style, gaugeStyle);
  }

  public GaugeWidget withLabel(Span l) {
    return new GaugeWidget(block, ratio, Optional.of(l), useUnicode, style, gaugeStyle);
  }

  public GaugeWidget withUseUnicode(boolean u) {
    return new GaugeWidget(block, ratio, label, u, style, gaugeStyle);
  }

  public GaugeWidget withStyle(Style s) {
    return new GaugeWidget(block, ratio, label, useUnicode, s, gaugeStyle);
  }

  public GaugeWidget withGaugeStyle(Style s) {
    return new GaugeWidget(block, ratio, label, useUnicode, style, s);
  }

  @Override
  public void render(Rect area, Buffer buf) {
    buf.setStyle(area, style);
    Rect gaugeArea;
    if (block.isPresent()) {
      BlockWidget b = block.get();
      Rect innerArea = b.inner(area);
      b.render(area, buf);
      gaugeArea = innerArea;
    } else {
      gaugeArea = area;
    }
    buf.setStyle(gaugeArea, gaugeStyle);
    if (gaugeArea.height() < 1) {
      return;
    }

    long pct = Math.round(ratio.value * 100.0);
    Span labelVal = label.orElseGet(() -> Span.nostyle(pct + "%"));
    int clampedLabelWidth = Math.min(gaugeArea.width(), labelVal.width());
    int labelCol = gaugeArea.left() + (gaugeArea.width() - clampedLabelWidth) / 2;
    int labelRow = gaugeArea.top() + gaugeArea.height() / 2;

    double filledWidth = (double) gaugeArea.width() * ratio.value;
    int end;
    if (useUnicode) {
      end = gaugeArea.left() + (int) Math.floor(filledWidth);
    } else {
      end = gaugeArea.left() + (int) Math.round(filledWidth);
    }
    Ranges.range(
        gaugeArea.top(),
        gaugeArea.bottom(),
        y -> {
          Ranges.range(
              gaugeArea.left(),
              end,
              x -> {
                // Use full block for the filled part of the gauge and spaces for the part
                // that's covered by the label. The bg/fg are swapped for the label part so
                // the label remains legible against the filled background.
                if (x < labelCol || x > labelCol + clampedLabelWidth || y != labelRow) {
                  buf.get(x, y)
                      .setSymbol(Symbols.block.FULL)
                      .setFg(gaugeStyle.fg().orElse(Color.Reset))
                      .setBg(gaugeStyle.bg().orElse(Color.Reset));
                } else {
                  buf.get(x, y)
                      .setSymbol(" ")
                      .setFg(gaugeStyle.bg().orElse(Color.Reset))
                      .setBg(gaugeStyle.fg().orElse(Color.Reset));
                }
              });
          if (useUnicode && ratio.value < 1.0) {
            buf.get(end, y).setSymbol(getUnicodeBlock(filledWidth % 1.0));
          }
        });

    buf.setSpan(labelCol, labelRow, labelVal, clampedLabelWidth);
  }

  public static String getUnicodeBlock(double frac) {
    long v = Math.round(frac * 8.0);
    if (v == 1) return Symbols.block.ONE_EIGHTH;
    if (v == 2) return Symbols.block.ONE_QUARTER;
    if (v == 3) return Symbols.block.THREE_EIGHTHS;
    if (v == 4) return Symbols.block.HALF;
    if (v == 5) return Symbols.block.FIVE_EIGHTHS;
    if (v == 6) return Symbols.block.THREE_QUARTERS;
    if (v == 7) return Symbols.block.SEVEN_EIGHTHS;
    if (v == 8) return Symbols.block.FULL;
    return " ";
  }

  public record Ratio(double value) {
    public Ratio {
      if (value < 0 || value > 1) {
        throw new IllegalArgumentException(value + " is not between 0 and 1");
      }
    }

    public static final Ratio Zero = new Ratio(0.0);

    public static Ratio percent(double value) {
      return new Ratio(value / 100);
    }
  }
}
