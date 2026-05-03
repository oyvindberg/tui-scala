package tui;

import java.util.ArrayList;
import java.util.List;
import tui.internal.BreakableForeach;
import tui.internal.DebugAssert;
import tui.internal.Ranges;
import tui.internal.UnicodeSegmentation;

/// A buffer that maps to the desired content of the terminal after the draw call
public final class Buffer {
  public Rect area;
  public Cell[] content;

  public Buffer(Rect area, Cell[] content) {
    this.area = area;
    this.content = content;
  }

  /// Returns a reference to Cell at the given coordinates
  public Cell get(int x, int y) {
    int i = this.indexOf(x, y);
    return content[i];
  }

  /// Returns a reference to Cell at the given coordinates
  public void set(int x, int y, Cell cell) {
    int i = this.indexOf(x, y);
    content[i] = cell;
  }

  /// Returns the index in the Vec<Cell> for the given global (x, y) coordinates.
  ///
  /// Global coordinates are offset by the Buffer's area offset (`x`/`y`).
  public int indexOf(int x, int y) {
    DebugAssert.apply(
        x >= this.area.left()
            && x < this.area.right()
            && y >= this.area.top()
            && y < this.area.bottom(),
        "Trying to access position outside the buffer: x={}, y={}, area={:?}",
        x,
        y,
        this.area);
    return (y - this.area.y()) * this.area.width() + (x - this.area.x());
  }

  /// Returns the (global) coordinates of a cell given its index
  ///
  /// Global coordinates are offset by the Buffer's area offset (`x`/`y`).
  public Position posOf(int i) {
    DebugAssert.apply(
        i < this.content.length,
        "Trying to get the coords of a cell outside the buffer: i={} len={}",
        i,
        this.content.length);
    return new Position(
        this.area.x() + i % this.area.width(), this.area.y() + i / this.area.width());
  }

  /// Print a string, starting at the position (x, y)
  public Position setString(int x, int y, String string, Style style) {
    return setStringn(x, y, string, Integer.MAX_VALUE, style);
  }

  /// Print at most the first n characters of a string if enough space is available until the end of the line
  public Position setStringn(int x, int y, String string, int width, Style style) {
    int[] index = {this.indexOf(x, y)};
    int[] xOffset = {x};
    Grapheme[] graphemes = UnicodeSegmentation.graphemes(string, true);
    int maxOffset = Math.min(this.area.right(), width + x);

    BreakableForeach.run(
        graphemes,
        (s, i) -> {
          if (s.width() == 0) return BreakableForeach.Res.Continue;
          // `x_offset + width > max_offset` could be integer overflow on 32-bit machines if we
          // change dimensions to usize or u32 and someone resizes the terminal to 1x2^32.
          if (s.width() > maxOffset - xOffset[0]) {
            return BreakableForeach.Res.Break;
          }
          content[index[0]].setSymbol(s).setStyle(style);

          // Reset following cells if multi-width (they would be hidden by the grapheme),
          Ranges.range(index[0] + 1, index[0] + s.width(), j -> content[j].reset());
          index[0] += s.width();
          xOffset[0] += s.width();
          return BreakableForeach.Res.Continue;
        });

    return new Position(xOffset[0], y);
  }

  public Position setSpans(int x0, int y, Spans spans, int width) {
    int remainingWidth = width;
    int x = x0;

    for (Span span : spans.spans()) {
      if (remainingWidth == 0) {
        break;
      }
      Position p = setStringn(x, y, span.content(), remainingWidth, span.style());
      int newX = p.x();
      int w = newX - x;
      x = newX;
      remainingWidth = remainingWidth - w;
    }
    return new Position(x, y);
  }

  public Position setSpan(int x, int y, Span span, int width) {
    return setStringn(x, y, span.content(), width, span.style());
  }

  public void setStyle(Rect area, Style style) {
    int y = area.top();
    while (y < area.bottom()) {
      int x = area.left();
      while (x < area.right()) {
        this.get(x, y).setStyle(style);
        x += 1;
      }
      y += 1;
    }
  }

  /// Resize the buffer so that the mapped area matches the given area and that the buffer length is equal to area.width * area.height
  public void resize(Rect area) {
    int length = area.area();
    Cell[] newContent = new Cell[length];
    int copyLen = Math.min(content.length, length);
    System.arraycopy(content, 0, newContent, 0, copyLen);
    for (int i = copyLen; i < length; i++) {
      newContent[i] = Cell.empty();
    }
    content = newContent;
    this.area = area;
  }

  /// Reset all cells in the buffer
  public void reset() {
    for (Cell c : content) {
      c.reset();
    }
  }

  /// Merge an other buffer into this one
  public void merge(Buffer other) {
    Rect newArea = area.union(other.area);
    Cell[] newContent = new Cell[newArea.area()];
    for (int i = 0; i < newContent.length; i++) newContent[i] = Cell.empty();

    int thisSize = area.area();
    int thisI = thisSize - 1;
    while (thisI >= 0) {
      Position p = posOf(thisI);
      // index in new content
      int k = (p.y() - newArea.y()) * newArea.width() + p.x() - newArea.x();
      newContent[k] = content[thisI].clone();
      thisI -= 1;
    }

    // Push content of the other buffer into this one (may erase previous data)
    int otherSize = other.area.area();
    int otherI = 0;
    while (otherI < otherSize) {
      Position p = other.posOf(otherI);
      // index in new content
      int k = (p.y() - newArea.y()) * newArea.width() + p.x() - newArea.x();
      newContent[k] = other.content[otherI].clone();
      otherI += 1;
    }
    this.area = newArea;
    this.content = newContent;
  }

  /// Builds a minimal sequence of coordinates and Cells necessary to update the UI from self to other.
  ///
  /// We're assuming that buffers are well-formed, that is no double-width cell is followed by a non-blank cell.
  public BufferUpdate[] diff(Buffer other) {
    Cell[] previousBuffer = content;
    Cell[] nextBuffer = other.content;
    int width = area.width();

    List<BufferUpdate> updates = new ArrayList<>();
    // Cells invalidated by drawing/replacing preceeding multi-width characters:
    int invalidated = 0;
    // Cells from the current buffer to skip due to preceeding multi-width characters taking their
    // place (the skipped cells should be blank anyway):
    int toSkip = 0;
    int i = 0;
    int max = Math.min(area.area(), other.area.area());
    while (i < max) {
      Cell current = nextBuffer[i];
      Cell previous = previousBuffer[i];
      if (!current.skip && (!current.equals(previous) || invalidated > 0) && toSkip == 0) {
        int x = i % width;
        int y = i / width;
        updates.add(new BufferUpdate(x, y, nextBuffer[i]));
      }

      toSkip = current.symbol.width() - 1;
      int affectedWidth = Math.max(current.symbol.width(), previous.symbol.width());
      invalidated = Math.max(affectedWidth, invalidated) - 1;
      i += 1;
    }
    return updates.toArray(new BufferUpdate[0]);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof Buffer other)) return false;
    if (!area.equals(other.area)) return false;
    if (content.length != other.content.length) return false;
    for (int i = 0; i < content.length; i++) {
      if (!content[i].equals(other.content[i])) return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int h = area.hashCode();
    for (Cell c : content) {
      h = 31 * h + c.hashCode();
    }
    return h;
  }

  /// Returns a Buffer with all cells set to the default one
  public static Buffer empty(Rect area) {
    Cell cell = Cell.empty();
    return Buffer.filled(area, cell);
  }

  /// Returns a Buffer with all cells initialized with the attributes of the given Cell
  public static Buffer filled(Rect area, Cell cell) {
    int size = area.area();
    Cell[] content = new Cell[size];
    for (int i = 0; i < size; i++) content[i] = cell.clone();
    return new Buffer(area, content);
  }

  /// Returns a Buffer containing the given lines
  public static Buffer withLines(String... lines) {
    int width = 0;
    for (String line : lines) {
      int w = new Grapheme(line).width();
      if (w > width) width = w;
    }
    Buffer buffer = Buffer.empty(new Rect(0, 0, width, lines.length));
    for (int y = 0; y < lines.length; y++) {
      buffer.setString(0, y, lines[y], Style.empty());
    }
    return buffer;
  }
}
