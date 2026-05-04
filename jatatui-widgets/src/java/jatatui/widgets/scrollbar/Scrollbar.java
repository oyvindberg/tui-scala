package jatatui.widgets.scrollbar;

import jatatui.core.buffer.Buffer;
import jatatui.core.internal.Wcwidth;
import jatatui.core.layout.Rect;
import jatatui.core.style.Style;
import jatatui.core.symbols.Scrollbar.Set;
import jatatui.core.widgets.StatefulWidget;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/// A widget to display a scrollbar.
///
/// Mirrors `ratatui_widgets::scrollbar::Scrollbar` (v0.30).
///
/// The following components of the scrollbar are customizable in symbol and style. Note the
/// scrollbar is represented horizontally but it can also be set vertically (which is actually the
/// default).
///
/// ```text
/// <--▮------->
/// ^  ^   ^   ^
/// │  │   │   └ end
/// │  │   └──── track
/// │  └──────── thumb
/// └─────────── begin
/// ```
///
/// You must specify the [ScrollbarState#contentLength] before rendering the [Scrollbar], or else
/// the scrollbar will render blank.
public final class Scrollbar implements StatefulWidget<ScrollbarState> {

  private final ScrollbarOrientation orientation;
  private final Style thumbStyle;
  private final String thumbSymbol;
  private final Style trackStyle;
  private final Optional<String> trackSymbol;
  private final Optional<String> beginSymbol;
  private final Style beginStyle;
  private final Optional<String> endSymbol;
  private final Style endStyle;

  private Scrollbar(
      ScrollbarOrientation orientation,
      Style thumbStyle,
      String thumbSymbol,
      Style trackStyle,
      Optional<String> trackSymbol,
      Optional<String> beginSymbol,
      Style beginStyle,
      Optional<String> endSymbol,
      Style endStyle) {
    this.orientation = orientation;
    this.thumbStyle = thumbStyle;
    this.thumbSymbol = thumbSymbol;
    this.trackStyle = trackStyle;
    this.trackSymbol = trackSymbol;
    this.beginSymbol = beginSymbol;
    this.beginStyle = beginStyle;
    this.endSymbol = endSymbol;
    this.endStyle = endStyle;
  }

  /// Creates a new scrollbar with the given orientation. The default symbol set is double
  /// vertical / double horizontal depending on orientation.
  public static Scrollbar of(ScrollbarOrientation orientation) {
    Set symbols =
        orientation.isVertical()
            ? jatatui.core.symbols.Scrollbar.DOUBLE_VERTICAL
            : jatatui.core.symbols.Scrollbar.DOUBLE_HORIZONTAL;
    return newWithSymbols(orientation, symbols);
  }

  /// Creates a vertical-right scrollbar (the upstream default).
  public static Scrollbar empty() {
    return of(ScrollbarOrientation.VerticalRight);
  }

  private static Scrollbar newWithSymbols(ScrollbarOrientation orientation, Set symbols) {
    return new Scrollbar(
        orientation,
        Style.empty(),
        symbols.thumb(),
        Style.empty(),
        Optional.of(symbols.track()),
        Optional.of(symbols.begin()),
        Style.empty(),
        Optional.of(symbols.end()),
        Style.empty());
  }

  // ---- Fluent setters ----

  /// Sets the orientation of the scrollbar.
  ///
  /// Resets the symbols to the double set matching the orientation (vertical or horizontal).
  public Scrollbar withOrientation(ScrollbarOrientation orientation) {
    Set symbols =
        orientation.isVertical()
            ? jatatui.core.symbols.Scrollbar.DOUBLE_VERTICAL
            : jatatui.core.symbols.Scrollbar.DOUBLE_HORIZONTAL;
    return new Scrollbar(
            orientation,
            thumbStyle,
            thumbSymbol,
            trackStyle,
            trackSymbol,
            beginSymbol,
            beginStyle,
            endSymbol,
            endStyle)
        .withSymbols(symbols);
  }

  /// Sets the orientation and symbols for the scrollbar from a [Set].
  public Scrollbar withOrientationAndSymbol(ScrollbarOrientation orientation, Set symbols) {
    return new Scrollbar(
            orientation,
            thumbStyle,
            thumbSymbol,
            trackStyle,
            trackSymbol,
            beginSymbol,
            beginStyle,
            endSymbol,
            endStyle)
        .withSymbols(symbols);
  }

  /// Sets the symbol that represents the thumb of the scrollbar.
  public Scrollbar withThumbSymbol(String thumbSymbol) {
    return new Scrollbar(
        orientation,
        thumbStyle,
        thumbSymbol,
        trackStyle,
        trackSymbol,
        beginSymbol,
        beginStyle,
        endSymbol,
        endStyle);
  }

  /// Sets the style on the scrollbar thumb.
  public Scrollbar withThumbStyle(Style thumbStyle) {
    return new Scrollbar(
        orientation,
        thumbStyle,
        thumbSymbol,
        trackStyle,
        trackSymbol,
        beginSymbol,
        beginStyle,
        endSymbol,
        endStyle);
  }

  /// Sets the symbol that represents the track of the scrollbar.
  public Scrollbar withTrackSymbol(Optional<String> trackSymbol) {
    return new Scrollbar(
        orientation,
        thumbStyle,
        thumbSymbol,
        trackStyle,
        trackSymbol,
        beginSymbol,
        beginStyle,
        endSymbol,
        endStyle);
  }

  /// Sets the style that is used for the track of the scrollbar.
  public Scrollbar withTrackStyle(Style trackStyle) {
    return new Scrollbar(
        orientation,
        thumbStyle,
        thumbSymbol,
        trackStyle,
        trackSymbol,
        beginSymbol,
        beginStyle,
        endSymbol,
        endStyle);
  }

  /// Sets the symbol that represents the beginning of the scrollbar.
  public Scrollbar withBeginSymbol(Optional<String> beginSymbol) {
    return new Scrollbar(
        orientation,
        thumbStyle,
        thumbSymbol,
        trackStyle,
        trackSymbol,
        beginSymbol,
        beginStyle,
        endSymbol,
        endStyle);
  }

  /// Sets the style that is used for the beginning of the scrollbar.
  public Scrollbar withBeginStyle(Style beginStyle) {
    return new Scrollbar(
        orientation,
        thumbStyle,
        thumbSymbol,
        trackStyle,
        trackSymbol,
        beginSymbol,
        beginStyle,
        endSymbol,
        endStyle);
  }

  /// Sets the symbol that represents the end of the scrollbar.
  public Scrollbar withEndSymbol(Optional<String> endSymbol) {
    return new Scrollbar(
        orientation,
        thumbStyle,
        thumbSymbol,
        trackStyle,
        trackSymbol,
        beginSymbol,
        beginStyle,
        endSymbol,
        endStyle);
  }

  /// Sets the style that is used for the end of the scrollbar.
  public Scrollbar withEndStyle(Style endStyle) {
    return new Scrollbar(
        orientation,
        thumbStyle,
        thumbSymbol,
        trackStyle,
        trackSymbol,
        beginSymbol,
        beginStyle,
        endSymbol,
        endStyle);
  }

  /// Sets the symbols used for the various parts of the scrollbar from a [Set].
  ///
  /// Only sets `beginSymbol`, `endSymbol` and `trackSymbol` if they already contain a value.
  /// If they were set to empty explicitly, this function will respect that choice.
  public Scrollbar withSymbols(Set symbols) {
    return new Scrollbar(
        orientation,
        thumbStyle,
        symbols.thumb(),
        trackStyle,
        trackSymbol.isPresent() ? Optional.of(symbols.track()) : trackSymbol,
        beginSymbol.isPresent() ? Optional.of(symbols.begin()) : beginSymbol,
        beginStyle,
        endSymbol.isPresent() ? Optional.of(symbols.end()) : endSymbol,
        endStyle);
  }

  /// Sets the style used for the various parts of the scrollbar.
  public Scrollbar withStyle(Style style) {
    return new Scrollbar(
        orientation,
        style,
        thumbSymbol,
        style,
        trackSymbol,
        beginSymbol,
        style,
        endSymbol,
        style);
  }

  // ---- Accessors (test-friendly) ----

  public ScrollbarOrientation orientation() {
    return orientation;
  }

  public String thumbSymbol() {
    return thumbSymbol;
  }

  public Optional<String> trackSymbol() {
    return trackSymbol;
  }

  public Optional<String> beginSymbol() {
    return beginSymbol;
  }

  public Optional<String> endSymbol() {
    return endSymbol;
  }

  public Style thumbStyle() {
    return thumbStyle;
  }

  public Style trackStyle() {
    return trackStyle;
  }

  public Style beginStyle() {
    return beginStyle;
  }

  public Style endStyle() {
    return endStyle;
  }

  // ---- Render ----

  @Override
  public void render(Rect area, Buffer buf, ScrollbarState state) {
    if (state.contentLength == 0 || trackLengthExcludingArrowHeads(area) == 0) {
      return;
    }
    Optional<Rect> scrollbarArea = scrollbarArea(area);
    if (scrollbarArea.isEmpty()) {
      return;
    }
    Rect bar = scrollbarArea.get();

    List<Optional<SymbolStyle>> barSymbols = barSymbols(bar, state);
    // Iterator over (column rows). For vertical: bar is 1-wide N-tall; for horizontal: bar is
    // 1-tall M-wide. `bar.columns().flat_map(Rect::rows)` in Rust yields column-major positions.
    int idx = 0;
    Rect.Columns columns = bar.columns();
    while (columns.hasNext()) {
      Rect col = columns.next();
      Rect.Rows rows = col.rows();
      while (rows.hasNext()) {
        Rect row = rows.next();
        if (idx >= barSymbols.size()) break;
        Optional<SymbolStyle> entry = barSymbols.get(idx);
        idx++;
        if (entry.isPresent()) {
          SymbolStyle ss = entry.get();
          buf.setString(row.x(), row.y(), ss.symbol(), ss.style());
        }
      }
    }
  }

  /// Returns a list of (symbol, style) pairs for each cell in the scrollbar area. Entries are
  /// [Optional#empty()] for cells that should not be drawn (e.g. an absent track symbol leaves the
  /// underlying cell untouched). Cells at begin/end positions are entirely omitted (no entry) when
  /// the corresponding symbol is absent — matching upstream's `iter::once(begin).flatten()` which
  /// drops the entry rather than emitting a blank cell.
  private List<Optional<SymbolStyle>> barSymbols(Rect area, ScrollbarState state) {
    PartLengths parts = partLengths(area, state);

    List<Optional<SymbolStyle>> out = new ArrayList<>();

    // begin symbol — completely omitted if absent
    if (beginSymbol.isPresent()) {
      out.add(Optional.of(new SymbolStyle(beginSymbol.get(), beginStyle)));
    }
    // track start — `Optional.empty()` if track symbol is absent (cell is untouched)
    Optional<SymbolStyle> trackEntry = trackSymbol.map(s -> new SymbolStyle(s, trackStyle));
    for (int i = 0; i < parts.trackStartLen(); i++) {
      out.add(trackEntry);
    }
    // thumb
    SymbolStyle thumbEntry = new SymbolStyle(thumbSymbol, thumbStyle);
    for (int i = 0; i < parts.thumbLen(); i++) {
      out.add(Optional.of(thumbEntry));
    }
    // track end
    for (int i = 0; i < parts.trackEndLen(); i++) {
      out.add(trackEntry);
    }
    // end symbol — completely omitted if absent
    if (endSymbol.isPresent()) {
      out.add(Optional.of(new SymbolStyle(endSymbol.get(), endStyle)));
    }

    return out;
  }

  /// Returns the lengths of the parts of a scrollbar.
  private PartLengths partLengths(Rect area, ScrollbarState state) {
    double trackLength = trackLengthExcludingArrowHeads(area);
    double viewportLength = viewportLength(state, area);

    double maxPosition = Math.max(0, state.contentLength - 1);
    double startPosition = clamp(state.position, 0.0, maxPosition);
    double maxViewportPosition = maxPosition + viewportLength;
    double endPosition = startPosition + viewportLength;

    double thumbStartD = startPosition * trackLength / maxViewportPosition;
    double thumbEndD = endPosition * trackLength / maxViewportPosition;

    int thumbStart = (int) clamp(Math.round(thumbStartD), 0.0, trackLength - 1.0);
    int thumbEnd = (int) clamp(Math.round(thumbEndD), 0.0, trackLength);

    int thumbLength = Math.max(1, Math.max(0, thumbEnd - thumbStart));
    int trackEndLength = Math.max(0, ((int) trackLength) - (thumbStart + thumbLength));

    return new PartLengths(thumbStart, thumbLength, trackEndLength);
  }

  private static double clamp(double v, double lo, double hi) {
    if (hi < lo) return lo;
    return Math.max(lo, Math.min(hi, v));
  }

  private Optional<Rect> scrollbarArea(Rect area) {
    return switch (orientation) {
      case VerticalLeft -> {
        Rect.Columns it = area.columns();
        yield it.hasNext() ? Optional.of(it.next()) : Optional.empty();
      }
      case VerticalRight -> {
        Rect.Columns it = area.columns();
        yield it.nextBack();
      }
      case HorizontalTop -> {
        Rect.Rows it = area.rows();
        yield it.hasNext() ? Optional.of(it.next()) : Optional.empty();
      }
      case HorizontalBottom -> {
        Rect.Rows it = area.rows();
        yield it.nextBack();
      }
    };
  }

  /// Calculates length of the track excluding the arrow heads.
  private int trackLengthExcludingArrowHeads(Rect area) {
    int startLen = beginSymbol.map(Wcwidth::width).orElse(0);
    int endLen = endSymbol.map(Wcwidth::width).orElse(0);
    int arrowsLen = startLen + endLen;
    if (orientation.isVertical()) {
      return Math.max(0, area.height() - arrowsLen);
    } else {
      return Math.max(0, area.width() - arrowsLen);
    }
  }

  private int viewportLength(ScrollbarState state, Rect area) {
    if (state.viewportContentLength != 0) {
      return state.viewportContentLength;
    } else if (orientation.isVertical()) {
      return area.height();
    } else {
      return area.width();
    }
  }

  /// Lengths of the start track, thumb, and end track parts of a scrollbar.
  private record PartLengths(int trackStartLen, int thumbLen, int trackEndLen) {}

  /// A symbol and its style.
  private record SymbolStyle(String symbol, Style style) {}
}
