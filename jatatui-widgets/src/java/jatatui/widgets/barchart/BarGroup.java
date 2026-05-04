package jatatui.widgets.barchart;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.HorizontalAlignment;
import jatatui.core.layout.Rect;
import jatatui.core.style.Style;
import jatatui.core.text.Line;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/// A group of bars to be shown by the [BarChart] widget.
///
/// A `BarGroup` is essentially a list of [Bar]s plus an optional centred label printed under
/// the group of bars. Mutators return a new `BarGroup`.
public final class BarGroup {

  /// Optional label printed under this group of bars.
  public final Optional<Line> label;

  /// The bars in the group (immutable copy of the input list).
  public final List<Bar> bars;

  private BarGroup(Optional<Line> label, List<Bar> bars) {
    this.label = label;
    this.bars = List.copyOf(bars);
  }

  // ---- Constructors ----

  /// Returns an empty `BarGroup` (no label, no bars).
  public static BarGroup empty() {
    return new BarGroup(Optional.empty(), List.of());
  }

  /// Creates a new `BarGroup` with the given bars and no label.
  public static BarGroup of(List<Bar> bars) {
    return new BarGroup(Optional.empty(), bars);
  }

  /// Creates a new `BarGroup` with the given bars and no label.
  public static BarGroup of(Bar... bars) {
    return new BarGroup(Optional.empty(), List.of(bars));
  }

  /// Creates a new `BarGroup` with the given label string and bars.
  public static BarGroup withLabel(String label, List<Bar> bars) {
    return new BarGroup(Optional.of(Line.from(label)), bars);
  }

  /// Creates a new `BarGroup` with the given [Line] label and bars.
  public static BarGroup withLabel(Line label, List<Bar> bars) {
    return new BarGroup(Optional.of(label), bars);
  }

  /// Creates a `BarGroup` from a list of `(label, value)` pairs (mirrors upstream
  /// `From<&[(&str, u64)]> for BarGroup`).
  public static BarGroup fromPairs(List<LabelledValue> entries) {
    List<Bar> bars = new ArrayList<>(entries.size());
    for (LabelledValue lv : entries) {
      bars.add(Bar.withLabel(lv.label(), lv.value()));
    }
    return new BarGroup(Optional.empty(), bars);
  }

  /// Convenience overload accepting varargs of [LabelledValue]s.
  public static BarGroup fromPairs(LabelledValue... entries) {
    return fromPairs(List.of(entries));
  }

  /// Domain-meaningful tuple replacing upstream's `(&str, u64)` pairs.
  public record LabelledValue(String label, long value) {
    public static LabelledValue of(String label, long value) {
      return new LabelledValue(label, value);
    }
  }

  // ---- Builders ----

  /// Returns a copy with the label set from a string.
  public BarGroup withLabel(String label) {
    return new BarGroup(Optional.of(Line.from(label)), bars);
  }

  /// Returns a copy with the label set from a [Line].
  public BarGroup withLabel(Line label) {
    return new BarGroup(Optional.of(label), bars);
  }

  /// Returns a copy with the bars replaced.
  public BarGroup withBars(List<Bar> bars) {
    return new BarGroup(label, bars);
  }

  /// Returns a copy with the bars replaced.
  public BarGroup withBars(Bar... bars) {
    return new BarGroup(label, List.of(bars));
  }

  // ---- Internal helpers ----

  /// The maximum bar value of this group, or empty if there are no bars.
  Optional<Long> max() {
    long m = Long.MIN_VALUE;
    boolean any = false;
    for (Bar b : bars) {
      if (b.value > m) {
        m = b.value;
        any = true;
      }
    }
    return any ? Optional.of(m) : Optional.empty();
  }

  void renderLabel(Buffer buf, Rect area, Style defaultLabelStyle) {
    label.ifPresent(line -> {
      int width = line.width();
      Rect adjusted;
      HorizontalAlignment alignment = line.alignment.orElse(HorizontalAlignment.Left);
      adjusted =
          switch (alignment) {
            case Center -> {
              int x = area.x() + saturatingSub(area.width(), width) / 2;
              yield new Rect(x, area.y(), width, area.height());
            }
            case Right -> {
              int x = area.x() + saturatingSub(area.width(), width);
              yield new Rect(x, area.y(), width, area.height());
            }
            case Left -> new Rect(area.x(), area.y(), width, area.height());
          };
      buf.setStyle(adjusted, defaultLabelStyle);
      buf.setLine(adjusted.x(), adjusted.y(), line, adjusted.width());
    });
  }

  private static int saturatingSub(int a, int b) {
    return a > b ? a - b : 0;
  }

  // ---- equals / hashCode ----

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof BarGroup other)) return false;
    return label.equals(other.label) && bars.equals(other.bars);
  }

  @Override
  public int hashCode() {
    return Objects.hash(label, bars);
  }

  @Override
  public String toString() {
    return "BarGroup{label=" + label + ", bars=" + bars + '}';
  }
}
