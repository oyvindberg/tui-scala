package jatatui.widgets.table;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.HorizontalAlignment;
import jatatui.core.layout.Position;
import jatatui.core.layout.Rect;
import jatatui.core.style.Style;
import jatatui.core.text.Line;
import jatatui.core.text.Span;
import jatatui.core.text.Text;
import java.util.Optional;

/// Internal helpers that render [Text] / [Line] / [Span] into a [Buffer].
///
/// Mirrors the Rust `impl Widget for &Text<'_>` / `impl Widget for &Line<'_>` /
/// `impl Widget for &Span<'_>` blocks from `ratatui_core::text`. They are needed by [TableCell] to
/// render cell content. Once the text types in `jatatui-core` implement a public `Widget`, this
/// helper becomes redundant and should be deleted.
final class TextRenderer {

  private TextRenderer() {}

  /// Renders `text` into the given `area` of `buf` using the same algorithm as upstream's
  /// `Widget for &Text<'_>`.
  static void renderText(Text text, Rect area, Buffer buf) {
    Rect clipped = buf.area().intersection(area);
    if (clipped.isEmpty()) return;
    buf.setStyle(clipped, text.style);
    int lineCount = text.lines.size();
    Rect.Rows rows = clipped.rows();
    int idx = 0;
    while (rows.hasNext() && idx < lineCount) {
      Rect lineArea = rows.next();
      Line line = text.lines.get(idx);
      renderLine(line, lineArea, buf, text.alignment);
      idx += 1;
    }
  }

  /// Renders `line` into the given `area` of `buf`. `parentAlignment` is used as a fallback when
  /// the line itself has no alignment.
  static void renderLine(
      Line line, Rect area, Buffer buf, Optional<HorizontalAlignment> parentAlignment) {
    Rect clipped = buf.area().intersection(area);
    if (clipped.isEmpty()) return;
    Rect singleLine = new Rect(clipped.x(), clipped.y(), clipped.width(), 1);
    int lineWidth = line.width();
    if (lineWidth == 0) return;

    buf.setStyle(singleLine, line.style);

    Optional<HorizontalAlignment> alignment =
        line.alignment.isPresent() ? line.alignment : parentAlignment;

    int areaWidth = singleLine.width();
    boolean canRenderComplete = lineWidth <= areaWidth;
    if (canRenderComplete) {
      int indent =
          switch (alignment.orElse(HorizontalAlignment.Left)) {
            case Center -> Math.max(0, areaWidth - lineWidth) / 2;
            case Right -> Math.max(0, areaWidth - lineWidth);
            case Left -> 0;
          };
      Rect indented =
          new Rect(singleLine.x() + indent, singleLine.y(), Math.max(0, areaWidth - indent), 1);
      renderSpans(line, indented, buf, 0);
    } else {
      int skip =
          switch (alignment.orElse(HorizontalAlignment.Left)) {
            case Center -> Math.max(0, lineWidth - areaWidth) / 2;
            case Right -> Math.max(0, lineWidth - areaWidth);
            case Left -> 0;
          };
      renderSpans(line, singleLine, buf, skip);
    }
  }

  /// Renders the spans of `line` into `area`, skipping the first `skipWidth` columns of content
  /// (used for centred / right-aligned overflow truncation).
  private static void renderSpans(Line line, Rect area, Buffer buf, int skipWidth) {
    int remainingSkip = skipWidth;
    int x = area.x();
    int right = area.right();
    int y = area.y();
    for (Span span : line.spans) {
      int spanWidth = span.width();
      if (remainingSkip >= spanWidth) {
        remainingSkip -= spanWidth;
        continue;
      }
      // Determine the visible portion of this span.
      Style mergedStyle = line.style.patch(span.style);
      String content;
      int contentWidthAvailable;
      if (remainingSkip == 0) {
        content = span.content;
        contentWidthAvailable = right - x;
      } else {
        // Truncate from the start by `remainingSkip` columns.
        TruncatedFromStart trunc = unicodeTruncateStart(span.content, spanWidth - remainingSkip);
        // If the first grapheme had to be cut in the middle the area gets indented to compensate.
        int firstOffset = (spanWidth - remainingSkip) - trunc.actualWidth();
        x += firstOffset;
        if (x >= right) return;
        content = trunc.content();
        contentWidthAvailable = right - x;
        remainingSkip = 0;
      }
      if (contentWidthAvailable <= 0) return;
      Position after = buf.setStringn(x, y, content, contentWidthAvailable, mergedStyle);
      x = after.x();
      if (x >= right) return;
    }
  }

  /// Truncate `content` from the start so that at most `maxWidth` columns remain. Returns the
  /// truncated string and its actual unicode width (which may be less than `maxWidth` if a wide
  /// grapheme straddles the boundary).
  private static TruncatedFromStart unicodeTruncateStart(String content, int maxWidth) {
    int len = content.length();
    int totalWidth = jatatui.core.internal.Wcwidth.width(content);
    if (totalWidth <= maxWidth) return new TruncatedFromStart(content, totalWidth);
    java.text.BreakIterator iter =
        java.text.BreakIterator.getCharacterInstance(java.util.Locale.getDefault());
    iter.setText(content);
    int start = iter.first();
    int end = iter.next();
    int accumulated = 0;
    int sliceStart = 0;
    int sliceWidth = 0;
    while (end != java.text.BreakIterator.DONE) {
      String g = content.substring(start, end);
      int w = jatatui.core.internal.Wcwidth.width(g);
      int remaining = totalWidth - accumulated;
      if (remaining <= maxWidth) {
        // Found the cut point - everything from this grapheme onwards fits.
        sliceStart = start;
        sliceWidth = remaining;
        break;
      }
      accumulated += w;
      start = end;
      end = iter.next();
    }
    if (end == java.text.BreakIterator.DONE) {
      // All graphemes scanned without finding a fit (shouldn't happen given the early return).
      return new TruncatedFromStart("", 0);
    }
    return new TruncatedFromStart(content.substring(sliceStart, len), sliceWidth);
  }

  /// Result of [#unicodeTruncateStart(String, int)].
  private record TruncatedFromStart(String content, int actualWidth) {}
}
