package tui.widgets;

import java.util.Optional;
import tui.Buffer;
import tui.Grapheme;
import tui.Rect;
import tui.Style;
import tui.Symbols;
import tui.Widget;
import tui.internal.Ranges;

/// Display multiple bars in a single widgets
public final class BarChartWidget implements Widget {
  public final Optional<BlockWidget> block;
  public final int barWidth;
  public final int barGap;
  public final Symbols.bar.Set barSet;
  public final Style barStyle;
  public final Style valueStyle;
  public final Style labelStyle;
  public final Style style;
  public final LabelValue[] data;
  public final Optional<Integer> max;

  public BarChartWidget(
      Optional<BlockWidget> block,
      int barWidth,
      int barGap,
      Symbols.bar.Set barSet,
      Style barStyle,
      Style valueStyle,
      Style labelStyle,
      Style style,
      LabelValue[] data,
      Optional<Integer> max) {
    this.block = block;
    this.barWidth = barWidth;
    this.barGap = barGap;
    this.barSet = barSet;
    this.barStyle = barStyle;
    this.valueStyle = valueStyle;
    this.labelStyle = labelStyle;
    this.style = style;
    this.data = data;
    this.max = max;
  }

  public static BarChartWidget empty() {
    return new BarChartWidget(
        Optional.empty(),
        1,
        1,
        Symbols.bar.NINE_LEVELS,
        Style.DEFAULT,
        Style.DEFAULT,
        Style.DEFAULT,
        Style.DEFAULT,
        new LabelValue[0],
        Optional.empty());
  }

  public BarChartWidget withBlock(BlockWidget b) {
    return new BarChartWidget(
        Optional.of(b), barWidth, barGap, barSet, barStyle, valueStyle, labelStyle, style, data, max);
  }

  public BarChartWidget withBarWidth(int w) {
    return new BarChartWidget(
        block, w, barGap, barSet, barStyle, valueStyle, labelStyle, style, data, max);
  }

  public BarChartWidget withBarGap(int g) {
    return new BarChartWidget(
        block, barWidth, g, barSet, barStyle, valueStyle, labelStyle, style, data, max);
  }

  public BarChartWidget withBarSet(Symbols.bar.Set s) {
    return new BarChartWidget(
        block, barWidth, barGap, s, barStyle, valueStyle, labelStyle, style, data, max);
  }

  public BarChartWidget withBarStyle(Style s) {
    return new BarChartWidget(
        block, barWidth, barGap, barSet, s, valueStyle, labelStyle, style, data, max);
  }

  public BarChartWidget withValueStyle(Style s) {
    return new BarChartWidget(
        block, barWidth, barGap, barSet, barStyle, s, labelStyle, style, data, max);
  }

  public BarChartWidget withLabelStyle(Style s) {
    return new BarChartWidget(
        block, barWidth, barGap, barSet, barStyle, valueStyle, s, style, data, max);
  }

  public BarChartWidget withStyle(Style s) {
    return new BarChartWidget(
        block, barWidth, barGap, barSet, barStyle, valueStyle, labelStyle, s, data, max);
  }

  public BarChartWidget withData(LabelValue[] d) {
    return new BarChartWidget(
        block, barWidth, barGap, barSet, barStyle, valueStyle, labelStyle, style, d, max);
  }

  public BarChartWidget withMax(int m) {
    return new BarChartWidget(
        block, barWidth, barGap, barSet, barStyle, valueStyle, labelStyle, style, data, Optional.of(m));
  }

  @Override
  public void render(Rect area, Buffer buf) {
    buf.setStyle(area, style);

    Rect chartArea;
    if (block.isPresent()) {
      BlockWidget b = block.get();
      Rect innerArea = b.inner(area);
      b.render(area, buf);
      chartArea = innerArea;
    } else {
      chartArea = area;
    }

    if (chartArea.height() < 2) {
      return;
    }
    // v0.24.0 #525: avoid divide by zero on zero-width bars or empty data.
    if (chartArea.area() == 0 || data.length == 0 || barWidth == 0) {
      return;
    }

    int maxValue;
    if (max.isPresent()) {
      maxValue = max.get();
    } else {
      int m = 0;
      for (LabelValue lv : data) {
        if (lv.value() > m) m = lv.value();
      }
      maxValue = m;
    }

    int maxIndex = Math.min(chartArea.width() / (barWidth + barGap), data.length);

    // Mutable per-index decreasing values, with original labels.
    int[] data2 = new int[maxIndex];
    for (int i = 0; i < maxIndex; i++) {
      data2[i] = data[i].value() * (chartArea.height() - 1) * 8 / Math.max(maxValue, 1);
    }

    Ranges.revRange(
        0,
        chartArea.height() - 1,
        j -> {
          for (int i = 0; i < data2.length; i++) {
            int v = data2[i];
            String symbol =
                switch (v) {
                  case 0 -> barSet.empty();
                  case 1 -> barSet.oneEighth();
                  case 2 -> barSet.oneQuarter();
                  case 3 -> barSet.threeEighths();
                  case 4 -> barSet.half();
                  case 5 -> barSet.fiveEighths();
                  case 6 -> barSet.threeQuarters();
                  case 7 -> barSet.sevenEighths();
                  default -> barSet.full();
                };
            int idx = i;
            Ranges.range(
                0,
                barWidth,
                x -> buf.get(
                        chartArea.left() + idx * (barWidth + barGap) + x,
                        chartArea.top() + j)
                    .setSymbol(symbol)
                    .setStyle(barStyle));
            if (v > 8) {
              data2[i] = v - 8;
            } else {
              data2[i] = 0;
            }
          }
        });

    // Values to display on the bar (computed when the data is passed to the widget)
    Grapheme[] values = new Grapheme[maxIndex];
    for (int i = 0; i < maxIndex; i++) {
      values[i] = new Grapheme(Integer.toString(data[i].value()));
    }
    for (int i = 0; i < maxIndex; i++) {
      LabelValue lv = data[i];
      String label = lv.label();
      int value = lv.value();
      if (value != 0) {
        Grapheme valueLabel = values[i];
        int width = valueLabel.width();
        if (width < barWidth) {
          buf.setString(
              chartArea.left() + i * (barWidth + barGap) + (barWidth - width) / 2,
              chartArea.bottom() - 2,
              valueLabel.str,
              valueStyle);
        }
      }
      buf.setStringn(
          chartArea.left() + i * (barWidth + barGap),
          chartArea.bottom() - 1,
          label,
          barWidth,
          labelStyle);
    }
  }

  public record LabelValue(String label, int value) {}
}
