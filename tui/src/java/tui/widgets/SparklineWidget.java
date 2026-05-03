package tui.widgets;

import java.util.Optional;
import tui.Buffer;
import tui.Rect;
import tui.Style;
import tui.Symbols;
import tui.Widget;
import tui.internal.Ranges;

/// Widget to render a sparkline over one or more lines.
public final class SparklineWidget implements Widget {
  public final Optional<BlockWidget> block;
  public final Style style;
  public final int[] data;
  public final Optional<Integer> max;
  public final Symbols.bar.Set barSet;

  public SparklineWidget(
      Optional<BlockWidget> block,
      Style style,
      int[] data,
      Optional<Integer> max,
      Symbols.bar.Set barSet) {
    this.block = block;
    this.style = style;
    this.data = data;
    this.max = max;
    this.barSet = barSet;
  }

  public static SparklineWidget empty() {
    return new SparklineWidget(
        Optional.empty(), Style.DEFAULT, new int[0], Optional.empty(), Symbols.bar.NINE_LEVELS);
  }

  public SparklineWidget withBlock(BlockWidget b) {
    return new SparklineWidget(Optional.of(b), style, data, max, barSet);
  }

  public SparklineWidget withStyle(Style s) {
    return new SparklineWidget(block, s, data, max, barSet);
  }

  public SparklineWidget withData(int[] d) {
    return new SparklineWidget(block, style, d, max, barSet);
  }

  public SparklineWidget withMax(int m) {
    return new SparklineWidget(block, style, data, Optional.of(m), barSet);
  }

  public SparklineWidget withBarSet(Symbols.bar.Set s) {
    return new SparklineWidget(block, style, data, max, s);
  }

  @Override
  public void render(Rect area, Buffer buf) {
    Rect sparkArea;
    if (block.isPresent()) {
      BlockWidget b = block.get();
      Rect innerArea = b.inner(area);
      b.render(area, buf);
      sparkArea = innerArea;
    } else {
      sparkArea = area;
    }

    if (sparkArea.height() < 1) {
      return;
    }

    int maxValue;
    if (max.isPresent()) {
      maxValue = max.get();
    } else {
      int m = 1;
      boolean any = false;
      for (int v : data) {
        if (!any || v > m) {
          m = v;
          any = true;
        }
      }
      maxValue = any ? m : 1;
    }
    int maxIndex = Math.min(sparkArea.width(), data.length);
    int[] data2 = new int[maxIndex];
    for (int i = 0; i < maxIndex; i++) {
      if (maxValue != 0) {
        data2[i] = data[i] * sparkArea.height() * 8 / maxValue;
      } else {
        data2[i] = 0;
      }
    }

    Ranges.revRange(
        0,
        sparkArea.height(),
        j -> Ranges.range(
            0,
            data2.length,
            i -> {
              int d = data2[i];
              String symbol =
                  switch (d) {
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
              buf.get(sparkArea.left() + i, sparkArea.top() + j)
                  .setSymbol(symbol)
                  .setStyle(style);

              if (d > 8) {
                data2[i] -= 8;
              } else {
                data2[i] = 0;
              }
            }));
  }
}
