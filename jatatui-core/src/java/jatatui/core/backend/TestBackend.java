package jatatui.core.backend;

import jatatui.core.buffer.Buffer;
import jatatui.core.buffer.BufferUpdate;
import jatatui.core.buffer.Cell;
import jatatui.core.internal.Wcwidth;
import jatatui.core.layout.Position;
import jatatui.core.layout.Rect;
import jatatui.core.layout.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/// A [Backend] used for integration testing — renders to an in-memory [Buffer].
///
/// Mirrors upstream `ratatui_core::backend::TestBackend` (v0.30). Although many integration and
/// unit tests in ratatui are written using this back-end, it is preferable to write unit tests for
/// widgets directly against the buffer rather than using this back-end. This back-end is intended
/// for integration tests that test the entire terminal UI.
public final class TestBackend implements Backend {

  private static final Size WINDOW_PIXEL_SIZE = new Size(640, 480);
  private static final int U16_MAX = 65_535;

  private Buffer buffer;
  private Buffer scrollback;
  private boolean cursor;
  private int posX;
  private int posY;

  /// Creates a new `TestBackend` with the specified width and height.
  public TestBackend(int width, int height) {
    this.buffer = Buffer.empty(new Rect(0, 0, width, height));
    this.scrollback = Buffer.empty(new Rect(0, 0, width, 0));
    this.cursor = false;
    this.posX = 0;
    this.posY = 0;
  }

  /// Creates a new `TestBackend` with the specified lines as the initial screen state.
  ///
  /// The back-end's screen size is determined from the initial lines.
  public static TestBackend withLines(String... lines) {
    Buffer buffer = Buffer.withLines(lines);
    Buffer scrollback = Buffer.empty(new Rect(0, 0, buffer.area().width(), 0));
    TestBackend t = new TestBackend(buffer.area().width(), buffer.area().height());
    t.buffer = buffer;
    t.scrollback = scrollback;
    return t;
  }

  /// Returns a reference to the internal buffer of the `TestBackend`.
  public Buffer buffer() {
    return buffer;
  }

  /// Returns a reference to the internal scrollback buffer of the `TestBackend`.
  ///
  /// The scrollback buffer represents the part of the screen that is currently hidden from view,
  /// but that could be accessed by scrolling back in the terminal's history. The scrollback buffer
  /// starts out empty. Lines are appended when they scroll off the top of the main buffer (this
  /// happens when lines are appended to the bottom of the main buffer using [#appendLines(int)]).
  ///
  /// The scrollback buffer has a maximum height of `u16::MAX`. If lines are appended to the bottom
  /// when it is at its maximum height, a corresponding number of lines is removed from the top.
  public Buffer scrollback() {
    return scrollback;
  }

  /// Resizes the `TestBackend` to the specified width and height.
  public void resize(int width, int height) {
    buffer.resize(new Rect(0, 0, width, height));
    int scrollbackHeight = scrollback.area().height();
    scrollback.resize(new Rect(0, 0, width, scrollbackHeight));
  }

  // ---- Display ----

  @Override
  public String toString() {
    return bufferView(buffer);
  }

  /// Returns a string representation of the given buffer for debugging purposes.
  ///
  /// Iterates through the buffer content and appends each cell's symbol to the view string. If a
  /// cell is hidden by a multi-width symbol, it is added to the overwritten list and displayed at
  /// the end of the line.
  public static String bufferView(Buffer buf) {
    Rect area = buf.area();
    int width = area.width();
    int height = area.height();
    Cell[] content = buf.content();
    StringBuilder view = new StringBuilder(content.length + height * 3);
    for (int row = 0; row < height; row++) {
      List<int[]> overwrittenIdx = new ArrayList<>();
      List<String> overwrittenSym = new ArrayList<>();
      int skip = 0;
      view.append('"');
      for (int x = 0; x < width; x++) {
        Cell c = content[row * width + x];
        String sym = c.symbol();
        if (skip == 0) {
          view.append(sym);
        } else {
          overwrittenIdx.add(new int[] {x});
          overwrittenSym.add(sym);
        }
        skip = Math.max(skip, Wcwidth.width(sym));
        skip = Math.max(0, skip - 1);
      }
      view.append('"');
      if (!overwrittenIdx.isEmpty()) {
        view.append(" Hidden by multi-width symbols: [");
        for (int i = 0; i < overwrittenIdx.size(); i++) {
          if (i > 0) view.append(", ");
          view.append('(')
              .append(overwrittenIdx.get(i)[0])
              .append(", \"")
              .append(overwrittenSym.get(i))
              .append("\")");
        }
        view.append(']');
      }
      view.append('\n');
    }
    return view.toString();
  }

  // ---- Backend ----

  @Override
  public void draw(Iterable<BufferUpdate> content) {
    for (BufferUpdate update : content) {
      Cell target = buffer.cellAt(update.x(), update.y());
      Cell src = update.cell();
      target.setSymbol(src.symbol());
      target.fg = src.fg;
      target.bg = src.bg;
      target.underlineColor = src.underlineColor;
      target.modifier = src.modifier;
      target.skip = src.skip;
    }
  }

  @Override
  public void hideCursor() {
    cursor = false;
  }

  @Override
  public void showCursor() {
    cursor = true;
  }

  /// Returns whether the cursor is currently shown. Java has no field-level access from outside
  /// the class for tests; upstream tests poke the field directly. We expose this read-only.
  public boolean cursorShown() {
    return cursor;
  }

  /// Returns the current cursor `(x, y)` position. Exposed for tests that mirror upstream's
  /// `backend.pos == (x, y)` field check.
  public Position pos() {
    return new Position(posX, posY);
  }

  @Override
  public Position getCursorPosition() {
    return new Position(posX, posY);
  }

  @Override
  public void setCursorPosition(Position position) {
    this.posX = position.x();
    this.posY = position.y();
  }

  @Override
  public void clear() {
    buffer.reset();
  }

  @Override
  public void clearRegion(ClearType clearType) {
    Cell[] content = buffer.content();
    int width = buffer.area().width();
    int len = content.length;
    int regionStart;
    int regionEndExclusive;
    switch (clearType) {
      case All -> {
        clear();
        return;
      }
      case AfterCursor -> {
        int index = buffer.indexOf(posX, posY) + 1;
        regionStart = Math.min(index, len);
        regionEndExclusive = len;
      }
      case BeforeCursor -> {
        int index = buffer.indexOf(posX, posY);
        regionStart = 0;
        regionEndExclusive = Math.min(index, len);
      }
      case CurrentLine -> {
        int lineStart = buffer.indexOf(0, posY);
        int lineEndInclusive = buffer.indexOf(width - 1, posY);
        regionStart = lineStart;
        regionEndExclusive = lineEndInclusive + 1;
      }
      case UntilNewLine -> {
        int index = buffer.indexOf(posX, posY);
        int lineEndInclusive = buffer.indexOf(width - 1, posY);
        regionStart = index;
        regionEndExclusive = lineEndInclusive + 1;
      }
      default -> throw new IllegalStateException("unreachable");
    }
    for (int i = regionStart; i < regionEndExclusive; i++) {
      content[i].reset();
    }
  }

  /// Inserts `lineCount` line breaks at the current cursor position.
  ///
  /// After the insertion, the cursor x position will be incremented by 1 (unless it's already at
  /// the end of the line). This is a common behaviour of terminals in raw mode.
  ///
  /// If the number of lines to append is fewer than the number of lines in the buffer after the
  /// cursor y position, then the cursor is moved down by `lineCount` rows.
  ///
  /// If the number of lines to append is greater than the number of lines in the buffer after the
  /// cursor y position, then that number of empty lines (at most the buffer's height in this
  /// case — but this limit is replaced with scrolling in most back-end implementations) will be
  /// added after the current position and the cursor will be moved to the last row.
  @Override
  public void appendLines(int lineCount) {
    int curX = posX;
    int curY = posY;
    int width = buffer.area().width();
    int height = buffer.area().height();

    // The next column ensuring that we don't go past the last column.
    int newCursorX = Math.min(saturatingAddU16(curX, 1), saturatingSubU16(width, 1));

    int maxY = saturatingSubU16(height, 1);
    int linesAfterCursor = saturatingSubU16(maxY, curY);

    if (lineCount > linesAfterCursor) {
      // We need to insert blank lines at the bottom and scroll the lines from the top into
      // scrollback.
      int scrollBy = lineCount - linesAfterCursor;
      int len = buffer.content().length;
      int cellsToScrollback = Math.min(len, width * scrollBy);

      // Take the first `cellsToScrollback` cells out into scrollback (these are the rows that
      // scroll off the top of the buffer), and replace those slots with empty cells.
      Cell[] removed = new Cell[cellsToScrollback];
      Cell[] content = buffer.content();
      for (int i = 0; i < cellsToScrollback; i++) {
        removed[i] = content[i];
        content[i] = Cell.empty();
      }
      appendToScrollback(removed);

      // Rotate the buffer left by `cellsToScrollback` so the empty cells we just inserted move
      // to the bottom.
      rotateLeft(content, 0, content.length, cellsToScrollback);

      // If the requested scroll exceeds the buffer's capacity, append the remaining empty rows
      // directly to scrollback.
      int remainder = width * scrollBy - cellsToScrollback;
      if (remainder > 0) {
        Cell[] empties = new Cell[remainder];
        for (int i = 0; i < remainder; i++) empties[i] = Cell.empty();
        appendToScrollback(empties);
      }
    }

    int newCursorY = Math.min(saturatingAddU16(curY, lineCount), maxY);
    setCursorPosition(new Position(newCursorX, newCursorY));
  }

  @Override
  public Size size() {
    return buffer.area().asSize();
  }

  @Override
  public WindowSize windowSize() {
    return new WindowSize(buffer.area().asSize(), WINDOW_PIXEL_SIZE);
  }

  @Override
  public void flush() {
    // no-op
  }

  @Override
  public void scrollRegionUp(int regionStart, int regionEnd, int scrollBy) {
    int width = buffer.area().width();
    int height = buffer.area().height();
    int cellRegionStart = width * Math.min(regionStart, height);
    int cellRegionEnd = width * Math.min(regionEnd, height);
    int cellRegionLen = cellRegionEnd - cellRegionStart;
    int cellsToScrollBy = width * scrollBy;
    Cell[] content = buffer.content();

    // Simple case: nothing to copy into scrollback (region doesn't include row 0).
    if (cellRegionStart > 0) {
      if (cellsToScrollBy >= cellRegionLen) {
        // The scroll amount is large enough to clear the whole region.
        for (int i = cellRegionStart; i < cellRegionEnd; i++) content[i].reset();
      } else {
        // Scroll up by rotating, then fill in the bottom with empty cells.
        rotateLeft(content, cellRegionStart, cellRegionEnd, cellsToScrollBy);
        for (int i = cellRegionEnd - cellsToScrollBy; i < cellRegionEnd; i++) content[i].reset();
      }
      return;
    }

    // Region includes row 0 — push rows into scrollback.
    int cellsFromRegion = Math.min(cellRegionLen, cellsToScrollBy);
    Cell[] removed = new Cell[cellsFromRegion];
    for (int i = 0; i < cellsFromRegion; i++) {
      removed[i] = content[i];
      content[i] = Cell.empty();
    }
    appendToScrollback(removed);

    if (cellsToScrollBy < cellRegionLen) {
      // Rotate the remaining cells to the front of the region.
      rotateLeft(content, cellRegionStart, cellRegionEnd, cellsFromRegion);
    } else {
      // The splice cleared out the region. Insert empty rows in scrollback.
      int extra = cellsToScrollBy - cellRegionLen;
      if (extra > 0) {
        Cell[] empties = new Cell[extra];
        for (int i = 0; i < extra; i++) empties[i] = Cell.empty();
        appendToScrollback(empties);
      }
    }
  }

  @Override
  public void scrollRegionDown(int regionStart, int regionEnd, int scrollBy) {
    int width = buffer.area().width();
    int height = buffer.area().height();
    int cellRegionStart = width * Math.min(regionStart, height);
    int cellRegionEnd = width * Math.min(regionEnd, height);
    int cellRegionLen = cellRegionEnd - cellRegionStart;
    int cellsToScrollBy = width * scrollBy;
    Cell[] content = buffer.content();

    if (cellsToScrollBy >= cellRegionLen) {
      // The scroll amount is large enough to clear the whole region.
      for (int i = cellRegionStart; i < cellRegionEnd; i++) content[i].reset();
    } else {
      // Scroll down by rotating right, then fill in the top with empty cells.
      rotateRight(content, cellRegionStart, cellRegionEnd, cellsToScrollBy);
      for (int i = cellRegionStart; i < cellRegionStart + cellsToScrollBy; i++) content[i].reset();
    }
  }

  // ---- Internal helpers ----

  /// Appends the provided cells to the bottom of the scrollback buffer. The number of cells must
  /// be a multiple of the buffer's width. If the scrollback buffer ends up larger than `u16::MAX`
  /// lines tall, lines are removed from the top to bring it back down to size.
  private void appendToScrollback(Cell[] cells) {
    int width = scrollback.area().width();
    Cell[] old = scrollback.content();
    int newLen = old.length + cells.length;
    Cell[] grown = new Cell[newLen];
    System.arraycopy(old, 0, grown, 0, old.length);
    System.arraycopy(cells, 0, grown, old.length, cells.length);

    int newHeight = Math.min(newLen / width, U16_MAX);
    int keepFrom = Math.max(0, newLen - width * U16_MAX);
    Cell[] trimmed;
    if (keepFrom == 0) {
      trimmed = grown;
    } else {
      trimmed = new Cell[newLen - keepFrom];
      System.arraycopy(grown, keepFrom, trimmed, 0, trimmed.length);
    }
    // Rebuild the scrollback Buffer in place: keep the same field reference exposed to callers.
    scrollback.content = trimmed;
    scrollback.area = new Rect(0, 0, width, newHeight);
  }

  /// Rotates the half-open slice `[from, to)` of `arr` to the left by `n` positions (in place).
  /// Mirrors Rust's `[..].rotate_left(n)` on a slice.
  private static void rotateLeft(Object[] arr, int from, int to, int n) {
    int len = to - from;
    if (len <= 0 || n <= 0) return;
    int k = n % len;
    if (k == 0) return;
    reverse(arr, from, from + k);
    reverse(arr, from + k, to);
    reverse(arr, from, to);
  }

  /// Rotates the half-open slice `[from, to)` of `arr` to the right by `n` positions (in place).
  /// Mirrors Rust's `[..].rotate_right(n)` on a slice.
  private static void rotateRight(Object[] arr, int from, int to, int n) {
    int len = to - from;
    if (len <= 0 || n <= 0) return;
    int k = n % len;
    if (k == 0) return;
    rotateLeft(arr, from, to, len - k);
  }

  private static void reverse(Object[] arr, int from, int to) {
    int i = from;
    int j = to - 1;
    while (i < j) {
      Object tmp = arr[i];
      arr[i] = arr[j];
      arr[j] = tmp;
      i++;
      j--;
    }
  }

  private static int saturatingAddU16(int a, int b) {
    long r = (long) a + (long) b;
    if (r > U16_MAX) return U16_MAX;
    if (r < 0) return 0;
    return (int) r;
  }

  private static int saturatingSubU16(int a, int b) {
    long r = (long) a - (long) b;
    if (r < 0) return 0;
    if (r > U16_MAX) return U16_MAX;
    return (int) r;
  }

  // ---- Equality / hash ----

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TestBackend other)) return false;
    return cursor == other.cursor
        && posX == other.posX
        && posY == other.posY
        && buffer.equals(other.buffer)
        && scrollback.equals(other.scrollback);
  }

  @Override
  public int hashCode() {
    return Objects.hash(buffer, scrollback, cursor, posX, posY);
  }
}
