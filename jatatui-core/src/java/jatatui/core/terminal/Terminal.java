package jatatui.core.terminal;

import jatatui.core.backend.Backend;
import jatatui.core.backend.ClearType;
import jatatui.core.buffer.Buffer;
import jatatui.core.buffer.BufferUpdate;
import jatatui.core.buffer.Cell;
import jatatui.core.layout.Position;
import jatatui.core.layout.Rect;
import jatatui.core.layout.Size;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/// An interface to interact and draw [Frame]s on the user's terminal.
///
/// This is the main entry point for jatatui. It is responsible for drawing and maintaining the
/// state of the buffers, cursor and viewport.
///
/// The `Terminal` struct maintains two buffers: the current and the previous. When the widgets are
/// drawn, the changes are accumulated in the current buffer. At the end of each draw pass, the two
/// buffers are compared, and only the changes between these buffers are written to the terminal,
/// avoiding any redundant operations. After flushing these changes, the buffers are swapped to
/// prepare for the next draw cycle.
///
/// The terminal also has a viewport which is the area of the terminal that is currently visible to
/// the user. It can be either fullscreen, inline or fixed. See [Viewport] for more information.
///
/// Applications should detect terminal resizes and call [#draw(Consumer)] to redraw the application
/// with the new size. This will automatically resize the internal buffers to match the new size for
/// inline and fullscreen viewports. Fixed viewports are not resized automatically.
///
/// Mirrors `ratatui_core::terminal::Terminal` (v0.30). Upstream is generic over a `Backend` trait
/// with an associated `Error` type; in Java the [Backend] uses checked [IOException]s, so the
/// generic parameter only needs to track the concrete back-end type.
public final class Terminal<B extends Backend> implements AutoCloseable {

  /// The backend used to interface with the terminal.
  private final B backend;

  /// Holds the results of the current and previous draw calls.
  private final Buffer[] buffers;

  /// Index of the current buffer.
  private int current;

  /// Whether the cursor is currently hidden.
  private boolean hiddenCursor;

  /// The viewport.
  private final Viewport viewport;

  /// Area of the viewport.
  private Rect viewportArea;

  /// Last known area of the terminal.
  private Rect lastKnownArea;

  /// Last known position of the cursor.
  private Position lastKnownCursorPos;

  /// Number of frames rendered so far.
  private int frameCount;

  private Terminal(
      B backend,
      Buffer current,
      Buffer previous,
      Viewport viewport,
      Rect viewportArea,
      Rect lastKnownArea,
      Position lastKnownCursorPos) {
    this.backend = backend;
    this.buffers = new Buffer[] {current, previous};
    this.current = 0;
    this.hiddenCursor = false;
    this.viewport = viewport;
    this.viewportArea = viewportArea;
    this.lastKnownArea = lastKnownArea;
    this.lastKnownCursorPos = lastKnownCursorPos;
    this.frameCount = 0;
  }

  // --- Construction ---

  /// Creates a new [Terminal] with the given [Backend] and a full screen viewport.
  ///
  /// Note that unlike higher-level convenience helpers, this does not install a panic hook — the
  /// caller is responsible for making sure the terminal is restored on uncaught exceptions.
  public static <B extends Backend> Terminal<B> create(B backend) throws IOException {
    return withOptions(backend, TerminalOptions.fullscreen());
  }

  /// Creates a new [Terminal] with the given [Backend] and [TerminalOptions].
  public static <B extends Backend> Terminal<B> withOptions(B backend, TerminalOptions options)
      throws IOException {
    Viewport viewport = options.viewport();
    Rect area;
    if (viewport instanceof Viewport.Fullscreen || viewport instanceof Viewport.Inline) {
      area = Rect.fromSize(backend.size());
    } else {
      area = ((Viewport.Fixed) viewport).area();
    }
    Rect viewportArea;
    Position cursorPos;
    if (viewport instanceof Viewport.Fullscreen) {
      viewportArea = area;
      cursorPos = Position.ORIGIN;
    } else if (viewport instanceof Viewport.Inline inline) {
      AreaAndCursor result = computeInlineSize(backend, inline.height(), area.asSize(), 0);
      viewportArea = result.area();
      cursorPos = result.cursor();
    } else {
      Rect fixedArea = ((Viewport.Fixed) viewport).area();
      viewportArea = fixedArea;
      cursorPos = fixedArea.asPosition();
    }
    return new Terminal<>(
        backend,
        Buffer.empty(viewportArea),
        Buffer.empty(viewportArea),
        viewport,
        viewportArea,
        area,
        cursorPos);
  }

  // --- Frame access ---

  /// Returns a [Frame] object which provides a consistent view into the terminal state for
  /// rendering.
  ///
  /// Most callers should use [#draw(Consumer)] instead. This method is intended for advanced
  /// scenarios such as widget unit testing, buffer state inspection, manual cursor manipulation,
  /// multiple rendering passes, custom frame lifecycle management, or buffer exporting.
  public Frame getFrame() {
    return new Frame(Optional.empty(), viewportArea, currentBufferMut(), frameCount);
  }

  /// Returns the current buffer.
  ///
  /// Mutation is allowed — Java has no `&mut`, so callers should treat this as a borrowed
  /// reference and avoid retaining it across draws.
  public Buffer currentBufferMut() {
    return buffers[current];
  }

  /// Returns the backend.
  public B backend() {
    return backend;
  }

  // --- Drawing pipeline ---

  /// Obtains a difference between the previous and the current buffer and passes it to the current
  /// backend for drawing.
  public void flush() throws IOException {
    Buffer previousBuffer = buffers[1 - current];
    Buffer currentBuffer = buffers[current];
    List<BufferUpdate> updates = previousBuffer.diff(currentBuffer);
    if (!updates.isEmpty()) {
      BufferUpdate last = updates.get(updates.size() - 1);
      lastKnownCursorPos = new Position(last.x(), last.y());
    }
    backend.draw(updates);
  }

  /// Updates the Terminal so that internal buffers match the requested area.
  ///
  /// The requested area will be saved to remain consistent when rendering. This leads to a full
  /// clear of the screen.
  public void resize(Rect area) throws IOException {
    Rect nextArea;
    if (viewport instanceof Viewport.Inline inline) {
      int offsetInPreviousViewport = saturatingSub(lastKnownCursorPos.y(), viewportArea.top());
      nextArea =
          computeInlineSize(backend, inline.height(), area.asSize(), offsetInPreviousViewport)
              .area();
    } else {
      // Fullscreen | Fixed -> mirror upstream: use the requested `area` directly.
      nextArea = area;
    }
    setViewportArea(nextArea);
    clear();
    lastKnownArea = area;
  }

  private void setViewportArea(Rect area) {
    buffers[current].resize(area);
    buffers[1 - current].resize(area);
    viewportArea = area;
  }

  /// Queries the backend for size and resizes if it doesn't match the previous size.
  public void autoresize() throws IOException {
    // fixed viewports do not get autoresized
    if (viewport instanceof Viewport.Fullscreen || viewport instanceof Viewport.Inline) {
      Rect area = Rect.fromSize(size());
      if (!area.equals(lastKnownArea)) {
        resize(area);
      }
    }
  }

  /// Draws a single frame to the terminal.
  ///
  /// This method will:
  /// - autoresize the terminal if necessary,
  /// - call the render callback, passing it a [Frame] to render to,
  /// - flush the current internal state by copying the current buffer to the backend,
  /// - move the cursor to the last known position if it was set during the rendering closure,
  /// - return a [CompletedFrame] with the current buffer and the area of the terminal.
  ///
  /// The render callback should fully render the entire frame when called, including areas that
  /// are unchanged from the previous frame.
  public CompletedFrame draw(Consumer<Frame> renderCallback) throws IOException {
    // Autoresize - otherwise we get glitches if shrinking or potential desync between widgets
    // and the terminal (if growing), which may OOB.
    autoresize();

    Frame frame = getFrame();

    renderCallback.accept(frame);

    // We can't change the cursor position right away because we have to flush the frame to
    // stdout first. But we also can't keep the frame around, since it holds a reference to the
    // Buffer. Thus we extract the important data out of the Frame here.
    Optional<Position> cursorPosition = frame.cursorPosition;

    // Draw to stdout.
    flush();

    if (cursorPosition.isEmpty()) {
      hideCursor();
    } else {
      showCursor();
      setCursorPosition(cursorPosition.get());
    }

    swapBuffers();

    backend.flush();

    CompletedFrame completed = new CompletedFrame(buffers[1 - current], lastKnownArea, frameCount);

    // Increment frame count before returning from draw — wraps around at Integer.MAX_VALUE
    // (mirrors upstream's wrapping_add over usize).
    frameCount = frameCount == Integer.MAX_VALUE ? 0 : frameCount + 1;

    return completed;
  }

  // --- Cursor / clear / size ---

  /// Hides the cursor.
  public void hideCursor() throws IOException {
    backend.hideCursor();
    hiddenCursor = true;
  }

  /// Shows the cursor.
  public void showCursor() throws IOException {
    backend.showCursor();
    hiddenCursor = false;
  }

  /// Returns the current cursor position. The position is the position of the cursor after the
  /// last draw call.
  public Position getCursorPosition() throws IOException {
    return backend.getCursorPosition();
  }

  /// Sets the cursor position.
  public void setCursorPosition(Position position) throws IOException {
    backend.setCursorPosition(position);
    lastKnownCursorPos = position;
  }

  /// Clears the terminal and forces a full redraw on the next draw call.
  public void clear() throws IOException {
    if (viewport instanceof Viewport.Fullscreen) {
      backend.clearRegion(ClearType.All);
    } else if (viewport instanceof Viewport.Inline) {
      backend.setCursorPosition(viewportArea.asPosition());
      backend.clearRegion(ClearType.AfterCursor);
    } else {
      Rect area = viewportArea;
      for (int y = area.top(); y < area.bottom(); y++) {
        backend.setCursorPosition(new Position(0, y));
        backend.clearRegion(ClearType.AfterCursor);
      }
    }
    // Reset the back buffer to make sure the next update will redraw everything.
    buffers[1 - current].reset();
  }

  /// Clears the inactive buffer and swaps it with the current buffer.
  public void swapBuffers() {
    buffers[1 - current].reset();
    current = 1 - current;
  }

  /// Queries the real size of the backend.
  public Size size() throws IOException {
    return backend.size();
  }

  // --- insert_before ---

  /// Inserts some content before the current inline viewport. Has no effect when the viewport is
  /// not inline.
  ///
  /// The `drawFn` is called to draw into a writable [Buffer] that is `height` lines tall. The
  /// content of that buffer is then inserted before the viewport.
  ///
  /// If the viewport isn't yet at the bottom of the screen, inserted lines push it towards the
  /// bottom. Once the viewport is at the bottom of the screen, inserted lines scroll the area of
  /// the screen above the viewport upwards.
  ///
  /// If more lines are inserted than there is space on the screen, then the top lines go directly
  /// into the terminal's scrollback buffer.
  public void insertBefore(int height, Consumer<Buffer> drawFn) throws IOException {
    if (!(viewport instanceof Viewport.Inline)) {
      return;
    }
    insertBeforeNoScrollingRegions(height, drawFn);
  }

  /// Implements [#insertBefore(int, Consumer)] using only standard backend capabilities (no
  /// scrolling-regions feature). Mirrors upstream's `insert_before_no_scrolling_regions`.
  private void insertBeforeNoScrollingRegions(int height, Consumer<Buffer> drawFn)
      throws IOException {
    // First render all of the lines to insert into a temporary buffer, then loop drawing chunks
    // from the buffer to the screen.
    Rect area = new Rect(0, 0, viewportArea.width(), height);
    Buffer buffer = Buffer.empty(area);
    drawFn.accept(buffer);
    Cell[] cells = buffer.content();
    int cellOffset = 0;

    // Use long variables to avoid worrying about overflowed u16s when adding, or about negative
    // results when subtracting.
    long drawnHeight = viewportArea.top();
    long bufferHeight = height;
    long viewportHeight = viewportArea.height();
    long screenHeight = lastKnownArea.height();

    // Loop, drawing large chunks of text (up to a screen-full at a time), until the remainder of
    // the buffer plus the viewport fits on the screen.
    while (bufferHeight + viewportHeight > screenHeight) {
      long toDraw = Math.min(bufferHeight, screenHeight);
      long scrollUp = Math.max(0, drawnHeight + toDraw - screenHeight);
      scrollUp((int) scrollUp);
      cellOffset = drawLines((int) (drawnHeight - scrollUp), (int) toDraw, cells, cellOffset);
      drawnHeight += toDraw - scrollUp;
      bufferHeight -= toDraw;
    }

    // There is now enough room on the screen for the remaining buffer plus the viewport.
    long scrollUp = Math.max(0, drawnHeight + bufferHeight + viewportHeight - screenHeight);
    scrollUp((int) scrollUp);
    drawLines((int) (drawnHeight - scrollUp), (int) bufferHeight, cells, cellOffset);
    drawnHeight += bufferHeight - scrollUp;

    setViewportArea(
        new Rect(viewportArea.x(), (int) drawnHeight, viewportArea.width(), viewportArea.height()));

    // Clear the viewport off the screen.
    clear();
  }

  /// Draws lines at the given vertical offset. The cell array (starting at `offset`) must contain
  /// enough cells for the requested lines. Returns the new offset into `cells` after the drawn
  /// region.
  private int drawLines(int yOffset, int linesToDraw, Cell[] cells, int offset) throws IOException {
    int width = lastKnownArea.width();
    int total = width * linesToDraw;
    if (linesToDraw > 0) {
      List<BufferUpdate> updates = new ArrayList<>(total);
      for (int i = 0; i < total; i++) {
        int x = i % width;
        int y = yOffset + (i / width);
        updates.add(new BufferUpdate(x, y, cells[offset + i]));
      }
      backend.draw(updates);
      backend.flush();
    }
    return offset + total;
  }

  /// Scrolls the whole screen up by the given number of lines.
  private void scrollUp(int linesToScroll) throws IOException {
    if (linesToScroll > 0) {
      setCursorPosition(new Position(0, saturatingSub(lastKnownArea.height(), 1)));
      backend.appendLines(linesToScroll);
    }
  }

  // --- AutoCloseable ---

  /// Restores the cursor state when this terminal is closed. Mirrors upstream's `Drop`
  /// implementation, which calls `show_cursor()` if the cursor was hidden.
  ///
  /// Failures are logged to stderr and swallowed (matching upstream `eprintln!`) so callers can use
  /// this in try-with-resources without losing the original exception.
  @Override
  public void close() {
    if (hiddenCursor) {
      try {
        showCursor();
      } catch (IOException e) {
        System.err.println("Failed to show the cursor: " + e.getMessage());
      }
    }
  }

  // --- Helpers ---

  private record AreaAndCursor(Rect area, Position cursor) {}

  private static AreaAndCursor computeInlineSize(
      Backend backend, int height, Size size, int offsetInPreviousViewport) throws IOException {
    Position pos = backend.getCursorPosition();
    int row = pos.y();

    int maxHeight = Math.min(size.height(), height);

    int linesAfterCursor = saturatingSub(saturatingSub(height, offsetInPreviousViewport), 1);

    backend.appendLines(linesAfterCursor);

    int availableLines = saturatingSub(saturatingSub(size.height(), row), 1);
    int missingLines = saturatingSub(linesAfterCursor, availableLines);
    if (missingLines > 0) {
      row = saturatingSub(row, missingLines);
    }
    row = saturatingSub(row, offsetInPreviousViewport);

    return new AreaAndCursor(new Rect(0, row, size.width(), maxHeight), pos);
  }

  private static int saturatingSub(int a, int b) {
    long r = (long) a - (long) b;
    if (r < 0) return 0;
    if (r > Integer.MAX_VALUE) return Integer.MAX_VALUE;
    return (int) r;
  }
}
