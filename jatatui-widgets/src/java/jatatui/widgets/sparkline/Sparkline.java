package jatatui.widgets.sparkline;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Rect;
import jatatui.core.style.Style;
import jatatui.core.style.Stylize;
import jatatui.core.symbols.Bar;
import jatatui.core.symbols.Shade;
import jatatui.core.widgets.Widget;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/// Widget to render a sparkline over one or more lines.
///
/// Mirrors `ratatui_widgets::sparkline::Sparkline` (v0.30).
///
/// Each bar in a `Sparkline` represents a value from the provided dataset. The height of the bar
/// is determined by the value in the dataset.
public final class Sparkline implements Widget, Stylize<Sparkline> {

  private final Optional<jatatui.widgets.block.Block> block;
  private final Style style;
  private final Style absentValueStyle;
  private final String absentValueSymbol;
  private final List<SparklineBar> data;
  private final Optional<Long> max;
  private final Bar.Set barSet;
  private final RenderDirection direction;

  private Sparkline(
      Optional<jatatui.widgets.block.Block> block,
      Style style,
      Style absentValueStyle,
      String absentValueSymbol,
      List<SparklineBar> data,
      Optional<Long> max,
      Bar.Set barSet,
      RenderDirection direction) {
    this.block = block;
    this.style = style;
    this.absentValueStyle = absentValueStyle;
    this.absentValueSymbol = absentValueSymbol;
    this.data = List.copyOf(data);
    this.max = max;
    this.barSet = barSet;
    this.direction = direction;
  }

  /// Returns a default `Sparkline` (no block, default style, [Bar#NINE_LEVELS],
  /// [RenderDirection#LeftToRight], empty data).
  public static Sparkline empty() {
    return new Sparkline(
        Optional.empty(),
        Style.empty(),
        Style.empty(),
        Shade.EMPTY,
        List.of(),
        Optional.empty(),
        Bar.NINE_LEVELS,
        RenderDirection.LeftToRight);
  }

  // ---- Builder methods ----

  /// Wraps the sparkline with the given [jatatui.widgets.block.Block].
  public Sparkline withBlock(jatatui.widgets.block.Block block) {
    return new Sparkline(
        Optional.of(block),
        style,
        absentValueStyle,
        absentValueSymbol,
        data,
        max,
        barSet,
        direction);
  }

  /// Sets the style of the entire widget.
  public Sparkline withStyle(Style style) {
    return new Sparkline(
        block, style, absentValueStyle, absentValueSymbol, data, max, barSet, direction);
  }

  /// Sets the style to use for absent values.
  public Sparkline withAbsentValueStyle(Style absentValueStyle) {
    return new Sparkline(
        block, style, absentValueStyle, absentValueSymbol, data, max, barSet, direction);
  }

  /// Sets the symbol to use for absent values.
  public Sparkline withAbsentValueSymbol(String absentValueSymbol) {
    return new Sparkline(
        block, style, absentValueStyle, absentValueSymbol, data, max, barSet, direction);
  }

  /// Sets the dataset for the sparkline from a list of [SparklineBar]s.
  public Sparkline withData(List<SparklineBar> data) {
    return new Sparkline(
        block, style, absentValueStyle, absentValueSymbol, data, max, barSet, direction);
  }

  /// Sets the dataset from a `long` array.
  public Sparkline withDataLongs(long... values) {
    List<SparklineBar> bars = new ArrayList<>(values.length);
    for (long v : values) {
      bars.add(SparklineBar.of(v));
    }
    return withData(bars);
  }

  /// Sets the dataset from a list of optional values (empty entries become absent bars).
  public Sparkline withDataOptionals(List<Optional<Long>> values) {
    List<SparklineBar> bars = new ArrayList<>(values.size());
    for (Optional<Long> v : values) {
      bars.add(SparklineBar.of(v));
    }
    return withData(bars);
  }

  /// Sets the maximum value of bars. Every bar will be scaled accordingly.
  public Sparkline withMax(long max) {
    return new Sparkline(
        block,
        style,
        absentValueStyle,
        absentValueSymbol,
        data,
        Optional.of(max),
        barSet,
        direction);
  }

  /// Clears the maximum value (max from the dataset will be used).
  public Sparkline withoutMax() {
    return new Sparkline(
        block,
        style,
        absentValueStyle,
        absentValueSymbol,
        data,
        Optional.empty(),
        barSet,
        direction);
  }

  /// Sets the characters used to display the bars.
  public Sparkline withBarSet(Bar.Set barSet) {
    return new Sparkline(
        block, style, absentValueStyle, absentValueSymbol, data, max, barSet, direction);
  }

  /// Sets the direction of the sparkline.
  public Sparkline withDirection(RenderDirection direction) {
    return new Sparkline(
        block, style, absentValueStyle, absentValueSymbol, data, max, barSet, direction);
  }

  // ---- Accessors ----

  public List<SparklineBar> data() {
    return data;
  }

  // ---- Stylize / Styled ----

  @Override
  public Style style() {
    return style;
  }

  @Override
  public Sparkline setStyle(Style style) {
    return withStyle(style);
  }

  // ---- Widget render ----

  @Override
  public void render(Rect area, Buffer buf) {
    block.ifPresent(b -> b.render(area, buf));
    Rect inner = block.map(b -> b.inner(area)).orElse(area);
    renderSparkline(inner, buf);
  }

  private void renderSparkline(Rect sparkArea, Buffer buf) {
    if (sparkArea.isEmpty()) {
      return;
    }
    long maxHeight =
        max.orElseGet(
            () -> {
              long m = 0;
              boolean any = false;
              for (SparklineBar b : data) {
                if (b.value.isPresent()) {
                  long v = b.value.get();
                  if (!any || v > m) {
                    m = v;
                    any = true;
                  }
                }
              }
              return any ? m : 1L;
            });

    int maxIndex = Math.min(sparkArea.width(), data.size());

    for (int i = 0; i < maxIndex; i++) {
      SparklineBar item = data.get(i);
      int x =
          switch (direction) {
            case LeftToRight -> sparkArea.left() + i;
            case RightToLeft -> sparkArea.right() - i - 1;
          };

      long height;
      Optional<String> symbol;
      Optional<Style> itemStyle;
      if (item.value.isPresent()) {
        long value = item.value.get();
        height = (maxHeight == 0) ? 0L : (value * (long) sparkArea.height() * 8L) / maxHeight;
        symbol = Optional.empty();
        itemStyle = item.style;
      } else {
        height = (long) sparkArea.height() * 8L;
        symbol = Optional.of(absentValueSymbol);
        itemStyle = Optional.of(absentValueStyle);
      }

      // render the item from top to bottom
      for (int j = sparkArea.height() - 1; j >= 0; j--) {
        String sym = symbol.orElse(symbolForHeight(height));
        if (height > 8) {
          height -= 8;
        } else {
          height = 0;
        }
        buf.cellAt(x, sparkArea.top() + j)
            .setSymbol(sym)
            .setStyle(style.patch(itemStyle.orElse(Style.empty())));
      }
    }
  }

  private String symbolForHeight(long height) {
    if (height <= 0) return barSet.empty();
    if (height == 1) return barSet.oneEighth();
    if (height == 2) return barSet.oneQuarter();
    if (height == 3) return barSet.threeEighths();
    if (height == 4) return barSet.half();
    if (height == 5) return barSet.fiveEighths();
    if (height == 6) return barSet.threeQuarters();
    if (height == 7) return barSet.sevenEighths();
    return barSet.full();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Sparkline other)) return false;
    return Objects.equals(block, other.block)
        && style.equals(other.style)
        && absentValueStyle.equals(other.absentValueStyle)
        && absentValueSymbol.equals(other.absentValueSymbol)
        && data.equals(other.data)
        && max.equals(other.max)
        && barSet.equals(other.barSet)
        && direction == other.direction;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        block, style, absentValueStyle, absentValueSymbol, data, max, barSet, direction);
  }
}
