package jatatui.core.buffer;

import jatatui.core.internal.Wcwidth;
import jatatui.core.layout.Position;
import jatatui.core.layout.Rect;
import jatatui.core.style.Style;
import jatatui.core.text.Line;
import jatatui.core.text.Span;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/// A buffer that maps to the desired content of the terminal after the draw call.
///
/// No widget in the library interacts directly with the terminal. Instead each of them is required
/// to draw their state to an intermediate buffer. It is basically a grid where each cell contains
/// a grapheme, a foreground color and a background color. This grid will then be used to output
/// the appropriate escape sequences and characters to draw the UI as the user has defined it.
///
/// Mirrors `ratatui_core::buffer::Buffer` (v0.30). Buffer is **mutable**: the area changes on
/// `resize`, and cells are mutated in-place during render.
public final class Buffer {

  /// The area represented by this buffer.
  public Rect area;

  /// The content of the buffer. Length is always equal to `area.width * area.height`.
  public Cell[] content;

  private Buffer(Rect area, Cell[] content) {
    this.area = area;
    this.content = content;
  }

  // ---- Construction ----

  /// Returns a Buffer with all cells set to the default cell.
  public static Buffer empty(Rect area) {
    return filled(area, Cell.empty());
  }

  /// Returns a Buffer with all cells initialized with the attributes of the given Cell.
  ///
  /// The given `cell` is cloned per slot — callers can keep mutating their own copy without
  /// affecting the buffer.
  public static Buffer filled(Rect area, Cell cell) {
    int size = (int) area.area();
    Cell[] content = new Cell[size];
    for (int i = 0; i < size; i++) {
      content[i] = cell.copy();
    }
    return new Buffer(area, content);
  }

  /// Returns a Buffer containing the given lines (raw strings).
  ///
  /// Each line becomes one row. The buffer's width is the maximum unicode display width of the
  /// supplied lines.
  public static Buffer withLines(String... lines) {
    Line[] arr = new Line[lines.length];
    for (int i = 0; i < lines.length; i++) {
      arr[i] = Line.raw(lines[i]);
    }
    return withLineObjects(arr);
  }

  /// Returns a Buffer containing the given pre-built lines.
  ///
  /// Same semantics as [#withLines(String...)], but the input is `Line` objects so the caller
  /// can supply per-span styling.
  public static Buffer withLineObjects(Line... lines) {
    int height = lines.length;
    int width = 0;
    for (Line line : lines) {
      int w = line.width();
      if (w > width) width = w;
    }
    Buffer buffer = empty(new Rect(0, 0, width, height));
    for (int y = 0; y < lines.length; y++) {
      buffer.setLine(0, y, lines[y], width);
    }
    return buffer;
  }

  /// Convenience overload accepting an iterable of lines.
  public static Buffer withLineObjects(Iterable<Line> lines) {
    List<Line> list = new ArrayList<>();
    lines.forEach(list::add);
    return withLineObjects(list.toArray(new Line[0]));
  }

  // ---- Content / area ----

  /// Returns the content of the buffer (a reference to the backing array — do not resize).
  public Cell[] content() {
    return content;
  }

  /// Returns the area covered by this buffer.
  public Rect area() {
    return area;
  }

  // ---- Coordinate / index helpers ----

  /// Returns the index in the content array for the given global (x, y) coordinates.
  ///
  /// Throws `IndexOutOfBoundsException` if the position is outside this buffer's area. This
  /// mirrors upstream's panic behaviour with a similar message.
  public int indexOf(int x, int y) {
    Optional<Integer> idx = indexOfOpt(new Position(x, y));
    if (idx.isEmpty()) {
      throw new IndexOutOfBoundsException(
          "index outside of buffer: the area is " + area + " but index is (" + x + ", " + y + ")");
    }
    return idx.get();
  }

  private Optional<Integer> indexOfOpt(Position position) {
    if (!area.contains(position)) {
      return Optional.empty();
    }
    int y = position.y() - area.y();
    int x = position.x() - area.x();
    int width = area.width();
    return Optional.of(y * width + x);
  }

  /// Returns the (global) coordinates of a cell given its index.
  ///
  /// Throws `IndexOutOfBoundsException` if the index is outside this buffer's content.
  public Position posOf(int index) {
    if (index < 0 || index >= content.length) {
      throw new IndexOutOfBoundsException(
          "Trying to get the coords of a cell outside the buffer: i="
              + index
              + " len="
              + content.length);
    }
    int width = area.width();
    int x = index % width + area.x();
    int y = index / width + area.y();
    return new Position(x, y);
  }

  // ---- Cell access ----

  /// Returns the [Cell] at the given position, or [Optional#empty()] if the position is outside
  /// the buffer's area.
  public Optional<Cell> cell(Position position) {
    return indexOfOpt(position).map(i -> content[i]);
  }

  /// Convenience overload taking `(x, y)` directly.
  public Optional<Cell> cell(int x, int y) {
    return cell(new Position(x, y));
  }

  /// Returns the [Cell] at the given position. Throws if the position is outside the buffer.
  ///
  /// Java has no operator overloading; this is the equivalent of upstream's `buf[(x, y)]`. Since
  /// `Cell` is mutable the returned reference can be used to mutate the buffer in place.
  public Cell cellAt(Position position) {
    return content[indexOf(position.x(), position.y())];
  }

  /// Convenience overload taking `(x, y)` directly.
  public Cell cellAt(int x, int y) {
    return content[indexOf(x, y)];
  }

  // ---- Mutation: text / style ----

  /// Print a string starting at the position (x, y).
  public void setString(int x, int y, String string, Style style) {
    setStringn(x, y, string, Integer.MAX_VALUE, style);
  }

  /// Print at most `maxWidth` columns of the string if enough space is available until the end of
  /// the line. Skips zero-width graphemes and control characters.
  ///
  /// Returns the position immediately after the last cell that was written (mirrors upstream's
  /// `(u16, u16)` return).
  public Position setStringn(int x, int y, String string, int maxWidth, Style style) {
    int clampedMax = maxWidth < 0 ? 0 : Math.min(maxWidth, Position.U16_MAX);
    int remainingWidth = Math.min(Math.max(area.right() - x, 0), clampedMax);

    BreakIterator iter = BreakIterator.getCharacterInstance(Locale.getDefault());
    iter.setText(string);
    int start = iter.first();
    int end = iter.next();
    int curX = x;
    while (end != BreakIterator.DONE) {
      String symbol = string.substring(start, end);
      start = end;
      end = iter.next();
      if (containsControl(symbol)) continue;
      int w = Wcwidth.width(symbol);
      if (w <= 0) continue;
      if (w > remainingWidth) break;
      remainingWidth -= w;
      cellAt(curX, y).setSymbol(symbol).setStyle(style);
      int nextSymbol = curX + w;
      curX += 1;
      // Reset following cells if the grapheme is multi-width (they would be hidden).
      while (curX < nextSymbol) {
        cellAt(curX, y).reset();
        curX += 1;
      }
    }
    return new Position(curX, y);
  }

  /// Print a line starting at the position (x, y).
  public Position setLine(int x, int y, Line line, int maxWidth) {
    int remainingWidth = maxWidth;
    int curX = x;
    for (Span span : line) {
      if (remainingWidth == 0) break;
      Position pos =
          setStringn(curX, y, span.content, remainingWidth, line.style.patch(span.style));
      int w = Math.max(0, pos.x() - curX);
      curX = pos.x();
      remainingWidth = Math.max(0, remainingWidth - w);
    }
    return new Position(curX, y);
  }

  /// Print a span starting at the position (x, y).
  public Position setSpan(int x, int y, Span span, int maxWidth) {
    return setStringn(x, y, span.content, maxWidth, span.style);
  }

  /// Set the style of all cells in the given area (intersected with this buffer's area).
  public void setStyle(Rect target, Style style) {
    Rect clipped = area.intersection(target);
    for (int y = clipped.top(); y < clipped.bottom(); y++) {
      for (int x = clipped.left(); x < clipped.right(); x++) {
        cellAt(x, y).setStyle(style);
      }
    }
  }

  // ---- Resize / reset / merge ----

  /// Resize the buffer so the mapped area matches `area` and the buffer length equals
  /// `area.width * area.height`. New cells (if any) are initialized to empty.
  public void resize(Rect area) {
    int length = (int) area.area();
    if (content.length > length) {
      Cell[] truncated = new Cell[length];
      System.arraycopy(content, 0, truncated, 0, length);
      content = truncated;
    } else if (content.length < length) {
      Cell[] grown = new Cell[length];
      System.arraycopy(content, 0, grown, 0, content.length);
      for (int i = content.length; i < length; i++) {
        grown[i] = Cell.empty();
      }
      content = grown;
    }
    this.area = area;
  }

  /// Reset all cells in the buffer.
  public void reset() {
    for (Cell c : content) {
      c.reset();
    }
  }

  /// Merge another buffer into this one.
  ///
  /// The resulting area is the union of the two areas. The other buffer's cells overwrite ours
  /// where the areas overlap.
  public void merge(Buffer other) {
    Rect newArea = area.union(other.area);
    int newSize = (int) newArea.area();

    // Grow the content array to fit; new slots are empty cells.
    if (content.length < newSize) {
      Cell[] grown = new Cell[newSize];
      System.arraycopy(content, 0, grown, 0, content.length);
      for (int i = content.length; i < newSize; i++) {
        grown[i] = Cell.empty();
      }
      content = grown;
    }

    // Move original content to its new position (back-to-front to avoid clobbering).
    int size = (int) area.area();
    for (int i = size - 1; i >= 0; i--) {
      Position p = posOf(i);
      int k = (p.y() - newArea.y()) * newArea.width() + (p.x() - newArea.x());
      if (i != k) {
        content[k] = content[i];
        content[i] = Cell.empty();
      }
    }

    // Push content of the other buffer into this one (may erase previous data).
    int otherSize = (int) other.area.area();
    for (int i = 0; i < otherSize; i++) {
      Position p = other.posOf(i);
      int k = (p.y() - newArea.y()) * newArea.width() + (p.x() - newArea.x());
      content[k] = other.content[i].copy();
    }
    this.area = newArea;
  }

  // ---- Diff ----

  /// Builds a minimal sequence of coordinates and Cells necessary to update the UI from `this`
  /// to `other`.
  ///
  /// Mirrors upstream `Buffer::diff`. Returns a list of [BufferUpdate]s, each carrying a
  /// reference to a cell from `other` (do not mutate it).
  ///
  /// The diff handles multi-width graphemes:
  /// - Trailing cells of a wide grapheme are skipped in the diff (they would be hidden anyway).
  /// - For wide emoji presentation sequences containing VS16 (U+FE0F), the trailing cell is
  ///   explicitly emitted to work around terminals that fail to clear it on their own.
  public List<BufferUpdate> diff(Buffer other) {
    Cell[] previousBuffer = this.content;
    Cell[] nextBuffer = other.content;

    List<BufferUpdate> updates = new ArrayList<>();
    // Cells invalidated by drawing/replacing preceding multi-width characters.
    int invalidated = 0;
    // Cells from the current buffer to skip due to preceding multi-width characters taking their
    // place (the skipped cells should be blank anyway), or due to per-cell-skipping.
    int toSkip = 0;
    int max = Math.min(previousBuffer.length, nextBuffer.length);
    for (int i = 0; i < max; i++) {
      Cell current = nextBuffer[i];
      Cell previous = previousBuffer[i];
      if (!current.skip && (!current.equals(previous) || invalidated > 0) && toSkip == 0) {
        Position pos = other.posOf(i);
        updates.add(new BufferUpdate(pos.x(), pos.y(), current));

        // Wide-grapheme trailing-cell workaround for VS16 emoji sequences.
        String symbol = current.symbol();
        int cellWidth = Wcwidth.width(symbol);
        boolean containsVs16 = symbol.indexOf(0xFE0F) >= 0;
        if (cellWidth > 1 && containsVs16) {
          for (int k = 1; k < cellWidth; k++) {
            int j = i + k;
            if (j >= nextBuffer.length || j >= previousBuffer.length) break;
            Cell prevTrailing = previousBuffer[j];
            Cell nextTrailing = nextBuffer[j];
            if (!nextTrailing.skip && !prevTrailing.equals(nextTrailing)) {
              Position tp = other.posOf(j);
              updates.add(new BufferUpdate(tp.x(), tp.y(), nextTrailing));
            }
          }
        }
      }
      toSkip = Math.max(0, Wcwidth.width(current.symbol()) - 1);
      int affectedWidth =
          Math.max(Wcwidth.width(current.symbol()), Wcwidth.width(previous.symbol()));
      invalidated = Math.max(0, Math.max(affectedWidth, invalidated) - 1);
    }
    return updates;
  }

  // ---- Equality / hash / debug ----

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Buffer other)) return false;
    if (!area.equals(other.area)) return false;
    if (content.length != other.content.length) return false;
    for (int i = 0; i < content.length; i++) {
      if (!content[i].equals(other.content[i])) return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hash(area, Arrays.hashCode(content));
  }

  /// Returns a debug representation of the buffer.
  ///
  /// Mirrors the Rust `Debug` impl format so test expectations transfer over. The format is:
  /// ```
  /// Buffer {
  ///     area: <area>,
  ///     content: [
  ///         "<row 0>", // hidden by multi-width symbols: [(idx, "<sym>"), ...]
  ///         ...
  ///     ],
  ///     styles: [
  ///         x: <x>, y: <y>, fg: <fg>, bg: <bg>, underline: <ul>, modifier: <mod>,
  ///         ...
  ///     ]
  /// }
  /// ```
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Buffer {\n    area: ").append(formatRect(area));
    if (area.isEmpty()) {
      sb.append("\n}");
      return sb.toString();
    }
    sb.append(",\n    content: [\n");

    record StyleEntry(int x, int y, Object fg, Object bg, Object underline, Object modifier) {}
    Optional<List<Object>> lastStyle = Optional.empty();
    List<StyleEntry> styles = new ArrayList<>();
    int width = area.width();
    int height = area.height();
    for (int y = 0; y < height; y++) {
      List<int[]> overwrittenIdx = new ArrayList<>();
      List<String> overwrittenSym = new ArrayList<>();
      int skip = 0;
      sb.append("        \"");
      for (int x = 0; x < width; x++) {
        Cell c = content[y * width + x];
        String sym = c.symbol();
        if (skip == 0) {
          sb.append(sym);
        } else {
          overwrittenIdx.add(new int[] {x});
          overwrittenSym.add(sym);
        }
        skip = Math.max(0, Math.max(skip, Wcwidth.width(sym)) - 1);
        List<Object> style = List.of((Object) c.fg, c.bg, c.underlineColor, c.modifier);
        if (lastStyle.isEmpty() || !lastStyle.get().equals(style)) {
          lastStyle = Optional.of(style);
          styles.add(new StyleEntry(x, y, c.fg, c.bg, c.underlineColor, c.modifier));
        }
      }
      sb.append("\",");
      if (!overwrittenIdx.isEmpty()) {
        sb.append(" // hidden by multi-width symbols: [");
        for (int k = 0; k < overwrittenIdx.size(); k++) {
          if (k > 0) sb.append(", ");
          sb.append("(")
              .append(overwrittenIdx.get(k)[0])
              .append(", \"")
              .append(overwrittenSym.get(k))
              .append("\")");
        }
        sb.append("]");
      }
      sb.append("\n");
    }
    sb.append("    ],\n    styles: [\n");
    for (StyleEntry s : styles) {
      sb.append("        x: ")
          .append(s.x)
          .append(", y: ")
          .append(s.y)
          .append(", fg: ")
          .append(s.fg)
          .append(", bg: ")
          .append(s.bg)
          .append(", underline: ")
          .append(s.underline)
          .append(", modifier: ")
          .append(s.modifier)
          .append(",\n");
    }
    sb.append("    ]\n}");
    return sb.toString();
  }

  private static String formatRect(Rect r) {
    return "Rect { x: "
        + r.x()
        + ", y: "
        + r.y()
        + ", width: "
        + r.width()
        + ", height: "
        + r.height()
        + " }";
  }

  // ---- Helpers ----

  private static boolean containsControl(String s) {
    int len = s.length();
    int i = 0;
    while (i < len) {
      int cp = s.codePointAt(i);
      if (Character.getType(cp) == Character.CONTROL) return true;
      i += Character.charCount(cp);
    }
    return false;
  }
}
