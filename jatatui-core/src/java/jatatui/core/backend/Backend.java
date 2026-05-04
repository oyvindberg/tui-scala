package jatatui.core.backend;

import jatatui.core.buffer.BufferUpdate;
import jatatui.core.layout.Position;
import jatatui.core.layout.Size;
import java.io.IOException;

/// Abstraction over a terminal back-end — the surface the [`jatatui.core.terminal.Terminal`] class
/// drives to render frames.
///
/// Mirrors upstream `ratatui_core::backend::Backend` (v0.30).
///
/// ## I/O errors
///
/// Upstream models failures with an associated `Error` type (`type Error: core::error::Error`).
/// Concrete back-ends typically use [`std::io::Error`]; the `TestBackend` uses
/// `core::convert::Infallible`. Java's idiomatic equivalent is a checked `IOException`, so each
/// method on this interface is declared `throws IOException`. (Per the project rules, `Either` is
/// reserved for domain errors — I/O is the natural Java idiom for terminal back-ends.) The
/// `TestBackend` simply never throws.
///
/// Most applications should not need to interact with this trait directly — the
/// `jatatui.core.terminal.Terminal` class provides a higher-level interface.
public interface Backend {

  /// Draws the given content to the terminal screen.
  ///
  /// Each [BufferUpdate] carries `(x, y, cell)`. The back-end writes the cell at `(x, y)`. The
  /// cell reference must not be mutated by the back-end; ownership stays with the caller (this
  /// mirrors the `&Cell` borrow upstream).
  void draw(Iterable<BufferUpdate> content) throws IOException;

  /// Inserts `n` line breaks to the terminal screen.
  ///
  /// This method is optional and may not be implemented by all back-ends. The default is a no-op,
  /// matching upstream's default trait method body.
  default void appendLines(int n) throws IOException {
    // no-op
  }

  /// Hides the cursor on the terminal screen.
  void hideCursor() throws IOException;

  /// Shows the cursor on the terminal screen.
  void showCursor() throws IOException;

  /// Returns the current cursor position on the terminal screen. The origin (0, 0) is at the top
  /// left corner of the screen.
  Position getCursorPosition() throws IOException;

  /// Sets the cursor position on the terminal screen to the given position. The origin (0, 0) is
  /// at the top left corner of the screen.
  void setCursorPosition(Position position) throws IOException;

  /// Clears the entire terminal screen.
  void clear() throws IOException;

  /// Clears a region of the terminal specified by the [ClearType] parameter.
  ///
  /// May be unsupported by some back-ends — those should throw an [IOException] for clear types
  /// they cannot handle. The convention is that [ClearType#All] is always supported (and is
  /// equivalent to calling [#clear()]).
  void clearRegion(ClearType clearType) throws IOException;

  /// Returns the size of the terminal screen in columns/rows as a [Size].
  Size size() throws IOException;

  /// Returns the size of the terminal screen in columns/rows and pixels as a [WindowSize].
  ///
  /// The reason for not returning only the pixel size, given the redundancy with [#size()], is
  /// that the underlying back-ends most likely fetch both values with one syscall, and callers
  /// typically need columns/rows alongside the pixel size.
  WindowSize windowSize() throws IOException;

  /// Flushes any buffered content to the terminal screen.
  void flush() throws IOException;

  /// Scrolls a region of the screen upwards. The region is the half-open row range
  /// `[regionStart, regionEnd)`.
  ///
  /// Each row in the region is replaced by the row `lineCount` rows below it, except the bottom
  /// `lineCount` rows, which are replaced by empty rows. If `lineCount` is equal to or larger than
  /// the number of rows in the region, then all rows are replaced with empty rows.
  ///
  /// If the region includes row 0, then `lineCount` rows are copied into the bottom of the
  /// scrollback buffer. These rows are first taken from the old contents of the region, starting
  /// from the top. If there aren't sufficient rows in the region, then the remainder are empty
  /// rows.
  ///
  /// The position of the cursor afterwards is undefined.
  ///
  /// Upstream gates this behind the `scrolling-regions` feature; we expose it unconditionally
  /// since Java has no Cargo features. Back-ends that don't support scrolling regions can throw
  /// [UnsupportedOperationException] (wrapped in an [IOException] is also acceptable).
  void scrollRegionUp(int regionStart, int regionEnd, int lineCount) throws IOException;

  /// Scrolls a region of the screen downwards. The region is the half-open row range
  /// `[regionStart, regionEnd)`.
  ///
  /// Each row in the region is replaced by the row `lineCount` rows above it, except the top
  /// `lineCount` rows, which are replaced by empty rows. If `lineCount` is equal to or larger than
  /// the number of rows in the region, then all rows are replaced with empty rows.
  ///
  /// The position of the cursor afterwards is undefined.
  ///
  /// This function is asymmetrical with regard to the scrollback buffer compared to
  /// [#scrollRegionUp(int, int, int)] — the down direction never copies into scrollback. This is
  /// how terminals seem to implement things.
  void scrollRegionDown(int regionStart, int regionEnd, int lineCount) throws IOException;
}
