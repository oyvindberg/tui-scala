package jatatui.widgets.barchart;

import jatatui.core.buffer.Buffer;
import jatatui.core.internal.Wcwidth;
import jatatui.core.layout.Rect;
import jatatui.core.style.Style;
import jatatui.core.style.Stylize;
import jatatui.core.text.Line;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

/// A bar to be shown by the [BarChart] widget.
///
/// A `Bar` has three styled components:
/// - the bar body itself (rendered with `style`),
/// - the value (or [#textValue], if set) printed inside the bar (rendered with `valueStyle`),
/// - the label printed under the bar (rendered with the bar chart's label style; the label
///   itself can carry styling via [Line]).
///
/// All fields are immutable; mutators return a new `Bar`.
public final class Bar implements Stylize<Bar> {

  /// Value to display on the bar.
  public final long value;

  /// Optional label printed under the bar.
  public final Optional<Line> label;

  /// Style for the bar body.
  public final Style style;

  /// Style of the value printed inside the bar.
  public final Style valueStyle;

  /// Optional `textValue` shown on the bar instead of the actual value.
  public final Optional<String> textValue;

  private Bar(
      long value, Optional<Line> label, Style style, Style valueStyle, Optional<String> textValue) {
    this.value = value;
    this.label = label;
    this.style = style;
    this.valueStyle = valueStyle;
    this.textValue = textValue;
  }

  // ---- Constructors ----

  /// Returns a new empty `Bar` (value = 0, no label, no text value, default styles).
  public static Bar empty() {
    return new Bar(0, Optional.empty(), Style.empty(), Style.empty(), Optional.empty());
  }

  /// Creates a new `Bar` with the given value (no label, no text value, default styles).
  public static Bar of(long value) {
    return new Bar(value, Optional.empty(), Style.empty(), Style.empty(), Optional.empty());
  }

  /// Creates a new `Bar` with the given label string and value.
  public static Bar withLabel(String label, long value) {
    return new Bar(
        value, Optional.of(Line.from(label)), Style.empty(), Style.empty(), Optional.empty());
  }

  /// Creates a new `Bar` with the given [Line] label and value.
  public static Bar withLabel(Line label, long value) {
    return new Bar(value, Optional.of(label), Style.empty(), Style.empty(), Optional.empty());
  }

  // ---- Builders ----

  /// Returns a copy with the value set.
  public Bar withValue(long value) {
    return new Bar(value, label, style, valueStyle, textValue);
  }

  /// Returns a copy with the label set from a string.
  public Bar withLabel(String label) {
    return new Bar(value, Optional.of(Line.from(label)), style, valueStyle, textValue);
  }

  /// Returns a copy with the label set from a [Line].
  public Bar withLabel(Line label) {
    return new Bar(value, Optional.of(label), style, valueStyle, textValue);
  }

  /// Returns a copy with the bar style set.
  public Bar withStyle(Style style) {
    return new Bar(value, label, style, valueStyle, textValue);
  }

  /// Returns a copy with the value style set.
  public Bar withValueStyle(Style valueStyle) {
    return new Bar(value, label, style, valueStyle, textValue);
  }

  /// Returns a copy with the text value set.
  public Bar withTextValue(String textValue) {
    return new Bar(value, label, style, valueStyle, Optional.of(textValue));
  }

  // ---- Styled / Stylize wiring ----

  @Override
  public Style style() {
    return style;
  }

  @Override
  public Bar setStyle(Style style) {
    return withStyle(style);
  }

  // ---- Rendering helpers (package-private) ----

  /// Render the value of the bar across two styles.
  ///
  /// [#textValue] is used if set, otherwise the value is converted to string. If the text width
  /// is greater than the bar length, the first part is rendered using `valueStyle` (over the
  /// bar) and the second part is rendered using the bar background style (outside the bar).
  void renderValueWithDifferentStyles(
      Buffer buf, Rect area, int barLength, Style defaultValueStyle, Style barStyle) {
    String value = Long.toString(this.value);
    String text = textValue.orElse(value);

    if (text.isEmpty()) {
      return;
    }

    Style style = defaultValueStyle.patch(this.valueStyle);
    // Render the first part with the default value style.
    buf.setStringn(area.x(), area.y(), text, barLength, style);
    // Render the second part with the bar style if the text is longer than the bar.
    // Upstream uses `text.len()` (UTF-8 byte length) for the comparison and split.
    byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
    if (textBytes.length > barLength) {
      // Find the last UTF-8 byte boundary at or before barLength.
      int boundaryBytes = 0;
      int i = 0;
      int len = text.length();
      while (i < len) {
        int cp = text.codePointAt(i);
        int cpLen = Character.charCount(cp);
        String chunk = text.substring(i, i + cpLen);
        int chunkBytes = chunk.getBytes(StandardCharsets.UTF_8).length;
        if (boundaryBytes + chunkBytes > barLength) {
          break;
        }
        boundaryBytes += chunkBytes;
        i += cpLen;
      }
      String first = text.substring(0, i);
      String second = text.substring(i);
      Style style2 = barStyle.patch(this.style);
      int firstByteLen = first.getBytes(StandardCharsets.UTF_8).length;
      int remainingWidth = Math.max(0, area.width() - firstByteLen);
      buf.setStringn(area.x() + firstByteLen, area.y(), second, remainingWidth, style2);
    }
  }

  /// Render the value (in the centre, vertical bar mode).
  void renderValue(Buffer buf, int maxWidth, int x, int y, Style defaultValueStyle, long ticks) {
    if (this.value != 0) {
      final long ticksPerLine = 8L;
      String value = Long.toString(this.value);
      String valueLabel = textValue.orElse(value);
      int width = Wcwidth.width(valueLabel);
      // If we have enough space or the ticks are >= 1 cell (8) print the value.
      if (width < maxWidth || (width == maxWidth && ticks >= ticksPerLine)) {
        // Mirror upstream which uses `value_label.len()` (UTF-8 byte length) for the offset.
        int byteLen = valueLabel.getBytes(StandardCharsets.UTF_8).length;
        int offset = saturatingSub(maxWidth, byteLen) >> 1;
        buf.setString(x + offset, y, valueLabel, defaultValueStyle.patch(this.valueStyle));
      }
    }
  }

  /// Render the bar label (centred under the bar, vertical bar mode).
  void renderLabel(Buffer buf, int maxWidth, int x, int y, Style defaultLabelStyle) {
    int labelWidth = label.map(Line::width).orElse(0);
    int width = Math.min(labelWidth, maxWidth);
    Rect area = new Rect(x + saturatingSub(maxWidth, width) / 2, y, width, 1);
    buf.setStyle(area, defaultLabelStyle);
    label.ifPresent(l -> buf.setLine(area.x(), area.y(), l, area.width()));
  }

  private static int saturatingSub(int a, int b) {
    return a > b ? a - b : 0;
  }

  // ---- equals / hashCode ----

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Bar other)) return false;
    return value == other.value
        && label.equals(other.label)
        && style.equals(other.style)
        && valueStyle.equals(other.valueStyle)
        && textValue.equals(other.textValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value, label, style, valueStyle, textValue);
  }

  @Override
  public String toString() {
    return "Bar{value="
        + value
        + ", label="
        + label
        + ", style="
        + style
        + ", valueStyle="
        + valueStyle
        + ", textValue="
        + textValue
        + '}';
  }
}
