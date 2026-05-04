package jatatui.widgets.barchart;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Direction;
import jatatui.core.layout.Rect;
import jatatui.core.style.Style;
import jatatui.core.style.Stylize;
import jatatui.core.symbols.Bar;
import jatatui.core.widgets.Widget;
import jatatui.widgets.block.Block;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/// A chart showing values as [Bar]s.
///
/// A `BarChart` is composed of a set of [jatatui.widgets.barchart.Bar] which can be added via
/// [#withGroup(BarGroup)]. Bars can be styled globally ([#withBarStyle(Style)]) or individually
/// ([jatatui.widgets.barchart.Bar#withStyle(Style)]).
///
/// The `BarChart` widget can also show groups of bars via [BarGroup]. A [BarGroup] is a set of
/// [jatatui.widgets.barchart.Bar], multiple can be added to a `BarChart` using
/// [#withGroup(BarGroup)] multiple times.
///
/// The chart can have a [Direction] (by default the bars are vertical). This is set using
/// [#withDirection(Direction)].
public final class BarChart implements Widget, Stylize<BarChart> {

  private final Optional<Block> block;
  private final int barWidth;
  private final int barGap;
  private final int groupGap;
  private final Bar.Set barSet;
  private final Style barStyle;
  private final Style valueStyle;
  private final Style labelStyle;
  private final Style style;
  private final List<BarGroup> data;
  private final Optional<Long> max;
  private final Direction direction;

  private BarChart(
      Optional<Block> block,
      int barWidth,
      int barGap,
      int groupGap,
      Bar.Set barSet,
      Style barStyle,
      Style valueStyle,
      Style labelStyle,
      Style style,
      List<BarGroup> data,
      Optional<Long> max,
      Direction direction) {
    this.block = block;
    this.barWidth = barWidth;
    this.barGap = barGap;
    this.groupGap = groupGap;
    this.barSet = barSet;
    this.barStyle = barStyle;
    this.valueStyle = valueStyle;
    this.labelStyle = labelStyle;
    this.style = style;
    this.data = List.copyOf(data);
    this.max = max;
    this.direction = direction;
  }

  // ---- Constructors ----

  /// Creates an empty `BarChart` (no data, vertical direction, default styles).
  public static BarChart empty() {
    return new BarChart(
        Optional.empty(),
        1,
        1,
        0,
        Bar.NINE_LEVELS,
        Style.empty(),
        Style.empty(),
        Style.empty(),
        Style.empty(),
        List.of(),
        Optional.empty(),
        Direction.Vertical);
  }

  /// Creates a new vertical `BarChart` widget with the given bars.
  public static BarChart of(List<jatatui.widgets.barchart.Bar> bars) {
    return empty().withGroup(BarGroup.of(bars));
  }

  /// Creates a new vertical `BarChart` widget with the given bars.
  public static BarChart of(jatatui.widgets.barchart.Bar... bars) {
    return of(List.of(bars));
  }

  /// Creates a new vertical `BarChart` (alias for [#of(List)]).
  public static BarChart vertical(List<jatatui.widgets.barchart.Bar> bars) {
    return of(bars);
  }

  /// Creates a new horizontal `BarChart` widget with the given bars.
  public static BarChart horizontal(List<jatatui.widgets.barchart.Bar> bars) {
    return new BarChart(
        Optional.empty(),
        1,
        1,
        0,
        Bar.NINE_LEVELS,
        Style.empty(),
        Style.empty(),
        Style.empty(),
        Style.empty(),
        List.of(BarGroup.of(bars)),
        Optional.empty(),
        Direction.Horizontal);
  }

  /// Creates a new `BarChart` from a list of pre-built [BarGroup]s.
  public static BarChart grouped(List<BarGroup> groups) {
    return new BarChart(
        Optional.empty(),
        1,
        1,
        0,
        Bar.NINE_LEVELS,
        Style.empty(),
        Style.empty(),
        Style.empty(),
        Style.empty(),
        new ArrayList<>(groups),
        Optional.empty(),
        Direction.Vertical);
  }

  // ---- Builders ----

  /// Adds a group of bars to the chart. Empty groups (no bars) are ignored.
  public BarChart withGroup(BarGroup group) {
    if (group.bars.isEmpty()) {
      return this;
    }
    List<BarGroup> next = new ArrayList<>(data.size() + 1);
    next.addAll(data);
    next.add(group);
    return new BarChart(
        block,
        barWidth,
        barGap,
        groupGap,
        barSet,
        barStyle,
        valueStyle,
        labelStyle,
        style,
        next,
        max,
        direction);
  }

  /// Adds a group of bars built from a list of `(label, value)` pairs.
  public BarChart withData(List<BarGroup.LabelledValue> entries) {
    return withGroup(BarGroup.fromPairs(entries));
  }

  /// Adds a group of bars built from a list of `(label, value)` pairs.
  public BarChart withData(BarGroup.LabelledValue... entries) {
    return withGroup(BarGroup.fromPairs(entries));
  }

  /// Surrounds the [BarChart] with a [Block].
  public BarChart withBlock(Block block) {
    return new BarChart(
        Optional.of(block),
        barWidth,
        barGap,
        groupGap,
        barSet,
        barStyle,
        valueStyle,
        labelStyle,
        style,
        data,
        max,
        direction);
  }

  /// Sets the value necessary for a bar to reach the maximum height.
  public BarChart withMax(long max) {
    return new BarChart(
        block,
        barWidth,
        barGap,
        groupGap,
        barSet,
        barStyle,
        valueStyle,
        labelStyle,
        style,
        data,
        Optional.of(max),
        direction);
  }

  /// Sets the default style of every bar (per-bar styles are layered on top).
  public BarChart withBarStyle(Style style) {
    return new BarChart(
        block,
        barWidth,
        barGap,
        groupGap,
        barSet,
        style,
        valueStyle,
        labelStyle,
        this.style,
        data,
        max,
        direction);
  }

  /// Sets the bar width (or height for horizontal bars).
  public BarChart withBarWidth(int width) {
    return new BarChart(
        block,
        width,
        barGap,
        groupGap,
        barSet,
        barStyle,
        valueStyle,
        labelStyle,
        style,
        data,
        max,
        direction);
  }

  /// Sets the gap between each bar.
  public BarChart withBarGap(int gap) {
    return new BarChart(
        block,
        barWidth,
        gap,
        groupGap,
        barSet,
        barStyle,
        valueStyle,
        labelStyle,
        style,
        data,
        max,
        direction);
  }

  /// Sets the bar symbol set used for displaying the bars.
  public BarChart withBarSet(Bar.Set barSet) {
    return new BarChart(
        block,
        barWidth,
        barGap,
        groupGap,
        barSet,
        barStyle,
        valueStyle,
        labelStyle,
        style,
        data,
        max,
        direction);
  }

  /// Sets the default style of the values printed on the bars.
  public BarChart withValueStyle(Style style) {
    return new BarChart(
        block,
        barWidth,
        barGap,
        groupGap,
        barSet,
        barStyle,
        style,
        labelStyle,
        this.style,
        data,
        max,
        direction);
  }

  /// Sets the default style of the labels under the bars and groups.
  public BarChart withLabelStyle(Style style) {
    return new BarChart(
        block,
        barWidth,
        barGap,
        groupGap,
        barSet,
        barStyle,
        valueStyle,
        style,
        this.style,
        data,
        max,
        direction);
  }

  /// Sets the gap between [BarGroup]s.
  public BarChart withGroupGap(int gap) {
    return new BarChart(
        block,
        barWidth,
        barGap,
        gap,
        barSet,
        barStyle,
        valueStyle,
        labelStyle,
        style,
        data,
        max,
        direction);
  }

  /// Sets the style of the entire chart.
  public BarChart withStyle(Style style) {
    return new BarChart(
        block,
        barWidth,
        barGap,
        groupGap,
        barSet,
        barStyle,
        valueStyle,
        labelStyle,
        style,
        data,
        max,
        direction);
  }

  /// Sets the direction of the bars (defaults to [Direction#Vertical]).
  public BarChart withDirection(Direction direction) {
    return new BarChart(
        block,
        barWidth,
        barGap,
        groupGap,
        barSet,
        barStyle,
        valueStyle,
        labelStyle,
        style,
        data,
        max,
        direction);
  }

  // ---- Accessors (for tests) ----

  public Optional<Block> block() {
    return block;
  }

  public int barWidth() {
    return barWidth;
  }

  public int barGap() {
    return barGap;
  }

  public int groupGap() {
    return groupGap;
  }

  public Bar.Set barSet() {
    return barSet;
  }

  public Style barStyle() {
    return barStyle;
  }

  public Style valueStyle() {
    return valueStyle;
  }

  public Style labelStyle() {
    return labelStyle;
  }

  public List<BarGroup> data() {
    return data;
  }

  public Optional<Long> max() {
    return max;
  }

  public Direction direction() {
    return direction;
  }

  // ---- Stylize / Styled ----

  @Override
  public Style style() {
    return style;
  }

  @Override
  public BarChart setStyle(Style style) {
    return withStyle(style);
  }

  // ---- Widget rendering ----

  @Override
  public void render(Rect area, Buffer buf) {
    buf.setStyle(area, style);
    block.ifPresent(b -> b.render(area, buf));
    Rect inner = block.map(b -> b.inner(area)).orElse(area);

    if (inner.isEmpty() || data.isEmpty() || barWidth == 0) {
      return;
    }

    switch (direction) {
      case Horizontal -> renderHorizontal(buf, inner);
      case Vertical -> renderVertical(buf, inner);
    }
  }

  // ---- Layout helpers ----

  /// Returns the visible bars length in ticks. A cell contains 8 ticks.
  ///
  /// `availableSpace` is used to calculate how many bars can fit in the space.
  /// `barMaxLength` is the maximal length a bar can take.
  private List<List<Long>> groupTicks(int availableSpace, int barMaxLength) {
    long maxValue = maximumDataValue();
    List<List<Long>> result = new ArrayList<>();
    int space = availableSpace;
    for (BarGroup group : data) {
      if (space == 0) {
        break;
      }
      int nBars = group.bars.size();
      int groupWidth = nBars * barWidth + saturatingSub(nBars, 1) * barGap;

      Optional<Integer> nBarsToTake;
      if (space > groupWidth) {
        space = saturatingSub(space, groupWidth + groupGap + barGap);
        nBarsToTake = Optional.of(nBars);
      } else {
        int maxBars = (space + barGap) / (barWidth + barGap);
        if (maxBars > 0) {
          space = 0;
          nBarsToTake = Optional.of(maxBars);
        } else {
          nBarsToTake = Optional.empty();
        }
      }

      if (nBarsToTake.isPresent()) {
        int n = nBarsToTake.get();
        List<Long> ticks = new ArrayList<>(n);
        for (int i = 0; i < n && i < group.bars.size(); i++) {
          long ticksValue = group.bars.get(i).value * (long) barMaxLength * 8L / maxValue;
          ticks.add(ticksValue);
        }
        result.add(ticks);
      } else {
        // can't fit any bar from this group; matches Rust `scan` returning None which terminates.
        break;
      }
    }
    return result;
  }

  private record LabelInfo(boolean groupLabelVisible, boolean barLabelVisible, int height) {}

  private LabelInfo labelInfo(int availableHeight) {
    if (availableHeight == 0) {
      return new LabelInfo(false, false, 0);
    }
    boolean barLabelVisible = false;
    for (BarGroup g : data) {
      for (jatatui.widgets.barchart.Bar b : g.bars) {
        if (b.label.isPresent()) {
          barLabelVisible = true;
          break;
        }
      }
      if (barLabelVisible) break;
    }

    if (availableHeight == 1 && barLabelVisible) {
      return new LabelInfo(false, true, 1);
    }

    boolean groupLabelVisible = false;
    for (BarGroup g : data) {
      if (g.label.isPresent()) {
        groupLabelVisible = true;
        break;
      }
    }
    int height = (groupLabelVisible ? 1 : 0) + (barLabelVisible ? 1 : 0);
    return new LabelInfo(groupLabelVisible, barLabelVisible, height);
  }

  private void renderHorizontal(Buffer buf, Rect area) {
    // Get the longest label width.
    int labelSize = 0;
    for (BarGroup group : data) {
      for (jatatui.widgets.barchart.Bar bar : group.bars) {
        if (bar.label.isPresent()) {
          int w = bar.label.get().width();
          if (w > labelSize) labelSize = w;
        }
      }
    }

    int labelX = area.x();
    int margin = labelSize != 0 ? 1 : 0;
    Rect barsArea =
        new Rect(
            area.x() + labelSize + margin,
            area.y(),
            saturatingSub(saturatingSub(area.width(), labelSize), margin),
            area.height());

    List<List<Long>> ticks = groupTicks(barsArea.height(), barsArea.width());

    int barY = barsArea.top();
    for (int g = 0; g < ticks.size(); g++) {
      List<Long> ticksVec = ticks.get(g);
      BarGroup group = data.get(g);
      for (int b = 0; b < ticksVec.size(); b++) {
        long t = ticksVec.get(b);
        jatatui.widgets.barchart.Bar bar = group.bars.get(b);
        int barLength = (int) (t / 8L);
        Style currentBarStyle = barStyle.patch(bar.style);

        for (int y = 0; y < barWidth; y++) {
          int by = barY + y;
          for (int x = 0; x < barsArea.width(); x++) {
            String symbol = (x < barLength) ? barSet.full() : barSet.empty();
            buf.cellAt(barsArea.left() + x, by).setSymbol(symbol).setStyle(currentBarStyle);
          }
        }

        Rect barValueArea =
            new Rect(barsArea.x(), barY + (barWidth >> 1), barsArea.width(), barsArea.height());

        // label
        if (bar.label.isPresent()) {
          buf.setLine(labelX, barValueArea.top(), bar.label.get(), labelSize);
        }

        bar.renderValueWithDifferentStyles(buf, barValueArea, barLength, valueStyle, barStyle);

        barY += barGap + barWidth;
      }

      // If group_gap is zero, then there is no place to print the group label.
      // Also check the group label is still inside the visible area.
      int labelY = barY - barGap;
      if (groupGap > 0 && labelY < barsArea.bottom()) {
        Rect labelRect = new Rect(barsArea.x(), labelY, barsArea.width(), barsArea.height());
        group.renderLabel(buf, labelRect, labelStyle);
        barY += groupGap;
      }
    }
  }

  private void renderVertical(Buffer buf, Rect area) {
    LabelInfo labelInfo = labelInfo(saturatingSub(area.height(), 1));

    Rect barsArea =
        new Rect(
            area.x(), area.y(), area.width(), saturatingSub(area.height(), labelInfo.height()));

    List<List<Long>> ticks = groupTicks(barsArea.width(), barsArea.height());
    renderVerticalBars(barsArea, buf, ticks);
    renderLabelsAndValues(area, buf, labelInfo, ticks);
  }

  private void renderVerticalBars(Rect area, Buffer buf, List<List<Long>> groupTicks) {
    int barX = area.left();
    for (int g = 0; g < groupTicks.size(); g++) {
      List<Long> ticksVec = groupTicks.get(g);
      BarGroup group = data.get(g);
      for (int b = 0; b < ticksVec.size(); b++) {
        long ticks = ticksVec.get(b);
        jatatui.widgets.barchart.Bar bar = group.bars.get(b);
        long t = ticks;
        for (int j = area.height() - 1; j >= 0; j--) {
          String symbol;
          if (t == 0) symbol = barSet.empty();
          else if (t == 1) symbol = barSet.oneEighth();
          else if (t == 2) symbol = barSet.oneQuarter();
          else if (t == 3) symbol = barSet.threeEighths();
          else if (t == 4) symbol = barSet.half();
          else if (t == 5) symbol = barSet.fiveEighths();
          else if (t == 6) symbol = barSet.threeQuarters();
          else if (t == 7) symbol = barSet.sevenEighths();
          else symbol = barSet.full();

          Style currentBarStyle = barStyle.patch(bar.style);
          for (int x = 0; x < barWidth; x++) {
            buf.cellAt(barX + x, area.top() + j).setSymbol(symbol).setStyle(currentBarStyle);
          }
          t = saturatingSubLong(t, 8L);
        }
        barX += barGap + barWidth;
      }
      barX += groupGap;
    }
  }

  /// Returns the maximum data value, always >= 1.
  private long maximumDataValue() {
    long m;
    if (max.isPresent()) {
      m = max.get();
    } else {
      m = 0;
      for (BarGroup g : data) {
        Optional<Long> gm = g.max();
        if (gm.isPresent() && gm.get() > m) m = gm.get();
      }
    }
    return Math.max(m, 1L);
  }

  private void renderLabelsAndValues(
      Rect area, Buffer buf, LabelInfo labelInfo, List<List<Long>> groupTicks) {
    int barX = area.left();
    int barY = area.bottom() - labelInfo.height() - 1;
    for (int g = 0; g < data.size() && g < groupTicks.size(); g++) {
      BarGroup group = data.get(g);
      List<Long> ticksVec = groupTicks.get(g);
      if (group.bars.isEmpty()) {
        continue;
      }
      // print group labels under the bars
      if (labelInfo.groupLabelVisible()) {
        int labelMaxWidth = ticksVec.size() * (barWidth + barGap) - barGap;
        Rect groupArea = new Rect(barX, area.bottom() - 1, labelMaxWidth, 1);
        group.renderLabel(buf, groupArea, labelStyle);
      }

      // print the bar values and labels
      for (int b = 0; b < group.bars.size() && b < ticksVec.size(); b++) {
        jatatui.widgets.barchart.Bar bar = group.bars.get(b);
        long ticks = ticksVec.get(b);
        if (labelInfo.barLabelVisible()) {
          bar.renderLabel(buf, barWidth, barX, barY + 1, labelStyle);
        }
        bar.renderValue(buf, barWidth, barX, barY, valueStyle, ticks);
        barX += barGap + barWidth;
      }
      barX += groupGap;
    }
  }

  // ---- equals / hashCode ----

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof BarChart other)) return false;
    return barWidth == other.barWidth
        && barGap == other.barGap
        && groupGap == other.groupGap
        && block.equals(other.block)
        && barSet.equals(other.barSet)
        && barStyle.equals(other.barStyle)
        && valueStyle.equals(other.valueStyle)
        && labelStyle.equals(other.labelStyle)
        && style.equals(other.style)
        && data.equals(other.data)
        && max.equals(other.max)
        && direction == other.direction;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        block,
        barWidth,
        barGap,
        groupGap,
        barSet,
        barStyle,
        valueStyle,
        labelStyle,
        style,
        data,
        max,
        direction);
  }

  private static int saturatingSub(int a, int b) {
    long r = (long) a - (long) b;
    if (r < 0) return 0;
    return (int) r;
  }

  private static long saturatingSubLong(long a, long b) {
    long r = a - b;
    return r < 0 ? 0 : r;
  }
}
