package tui
package terminal

import tui.backend.Backend
import tui.buffer.Buffer
import tui.layout.Rect
import tui.widgets.{StatefulWidget, Widget}

import scala.util.control.NonFatal

//#[derive(Debug, Clone, PartialEq)]
/// UNSTABLE
sealed trait ResizeBehavior
object ResizeBehavior {
  case object Fixed extends ResizeBehavior
  case object Auto extends ResizeBehavior
}

//#[derive(Debug, Clone, PartialEq)]
/// UNSTABLE
case class Viewport(
    var area: Rect,
    resize_behavior: ResizeBehavior
)

object Viewport {
  /// UNSTABLE
  def fixed(area: Rect): Viewport =
    Viewport(
      area,
      resize_behavior = ResizeBehavior.Fixed
    )
}

//#[derive(Debug, Clone, PartialEq)]
/// Options to pass to [`Terminal::with_options`]
case class TerminalOptions(
    /// Viewport used to draw to the terminal
    viewport: Viewport
)

/// Represents a consistent terminal interface for rendering.
case class Frame(
    buffer: Buffer,
    /// Terminal size, guaranteed not to change when rendering.
    size: Rect,

    /// Where should the cursor be after drawing this frame?
    ///
    /// If `None`, the cursor is hidden and its position is controlled by the backend. If `Some((x,
    /// y))`, the cursor is shown and placed at `(x, y)` after the call to `Terminal::draw()`.
    var cursor_position: Option[(Int, Int)]
) {

  /// Render a [`Widget`] to the current buffer using [`Widget::render`].
  ///
  /// # Examples
  ///
  /// ```rust
  /// # use tui::Terminal;
  /// # use tui::backend::TestBackend;
  /// # use tui::layout::Rect;
  /// # use tui::widgets::Block;
  /// # let backend = TestBackend::new(5, 5);
  /// # let mut terminal = Terminal::new(backend).unwrap();
  /// let block = Block::default();
  /// let area = Rect::new(0, 0, 5, 5);
  /// let mut frame = terminal.get_frame();
  /// frame.render_widget(block, area);
  /// ```
  def render_widget(widget: Widget, area: Rect): Unit =
    widget.render(area, buffer);

  /// Render a [`StatefulWidget`] to the current buffer using [`StatefulWidget::render`].
  ///
  /// The last argument should be an instance of the [`StatefulWidget::State`] associated to the
  /// given [`StatefulWidget`].
  ///
  /// # Examples
  ///
  /// ```rust
  /// # use tui::Terminal;
  /// # use tui::backend::TestBackend;
  /// # use tui::layout::Rect;
  /// # use tui::widgets::{List, ListItem, ListState};
  /// # let backend = TestBackend::new(5, 5);
  /// # let mut terminal = Terminal::new(backend).unwrap();
  /// let mut state = ListState::default();
  /// state.select(Some(1));
  /// let items = vec![
  ///     ListItem::new("Item 1"),
  ///     ListItem::new("Item 2"),
  /// ];
  /// let list = List::new(items);
  /// let area = Rect::new(0, 0, 5, 5);
  /// let mut frame = terminal.get_frame();
  /// frame.render_stateful_widget(list, area, &mut state);
  /// ```
  def render_stateful_widget[W <: StatefulWidget](widget: W, area: Rect)(state: widget.State): Unit =
    widget.render(area, buffer, state);

  /// After drawing this frame, make the cursor visible and put it at the specified (x, y)
  /// coordinates. If this method is not called, the cursor will be hidden.
  ///
  /// Note that this will interfere with calls to `Terminal::hide_cursor()`,
  /// `Terminal::show_cursor()`, and `Terminal::set_cursor()`. Pick one of the APIs and stick
  /// with it.
  def set_cursor(x: Int, y: Int): Unit =
    cursor_position = Some((x, y));
}

/// CompletedFrame represents the state of the terminal after all changes performed in the last
/// [`Terminal::draw`] call have been applied. Therefore, it is only valid until the next call to
/// [`Terminal::draw`].
case class CompletedFrame(
    buffer: Buffer,
    area: Rect
)

/// Interface to the terminal backed by Termion
//#[derive(Debug)]
case class Terminal private (
    backend: Backend,
    /// Holds the results of the current and previous draw calls. The two are compared at the end
    /// of each draw pass to output the necessary updates to the terminal
    buffers: Array[Buffer],
    /// Index of the current buffer in the previous array
    var current: Int,
    /// Whether the cursor is currently hidden
    var hidden_cursor: Boolean,
    /// Viewport
    viewport: Viewport
) {
  require(buffers.length == 2)

  def drop() =
    // Attempt to restore the cursor state
    if (hidden_cursor) {
      try show_cursor()
      catch {
        case NonFatal(e) => System.err.println(s"Failed to show the cursor: ${e.getMessage}")
      }
    }
  /// Get a Frame object which provides a consistent view into the terminal state for rendering.
  def get_frame(): Frame =
    Frame(
      buffer = current_buffer_mut(),
      size = viewport.area,
      cursor_position = None
    )

  def current_buffer_mut(): Buffer =
    buffers(current)

  /// Obtains a difference between the previous and the current buffer and passes it to the
  /// current backend for drawing.
  def flush(): Unit = {
    val previous_buffer = buffers(1 - current);
    val current_buffer = buffers(current);
    val updates = previous_buffer.diff(current_buffer);
    backend.draw(updates)
  }

  /// Updates the Terminal so that internal buffers match the requested size. Requested size will
  /// be saved so the size can remain consistent when rendering.
  /// This leads to a full clear of the screen.
  def resize(area: Rect): Unit = {
    buffers(current).resize(area);
    buffers(1 - current).resize(area);
    viewport.area = area;
    clear()
  }

  /// Queries the backend for size and resizes if it doesn't match the previous size.
  def autoresize(): Unit =
    if (viewport.resize_behavior == ResizeBehavior.Auto) {
      val size_ = size();
      if (size_ != viewport.area) {
        resize(size_);
      }
    };

  /// Synchronizes terminal size, calls the rendering closure, flushes the current internal state
  /// and prepares for the next draw call.
  def draw(f: Frame => Unit): CompletedFrame = {
    // Autoresize - otherwise we get glitches if shrinking or potential desync between widgets
    // and the terminal (if growing), which may OOB.
    autoresize();

    val frame = get_frame();
    f(frame);
    // We can't change the cursor position right away because we have to flush the frame to
    // stdout first. But we also can't keep the frame around, since it holds a &mut to
    // Terminal. Thus, we're taking the important data out of the Frame and dropping it.
    val cursor_position = frame.cursor_position;

    // Draw to stdout
    flush()

    cursor_position match {
      case None => hide_cursor()
      case Some((x, y)) =>
        show_cursor();
        set_cursor(x, y)
    }

    // Swap buffers
    buffers(1 - current).reset();
    current = 1 - current;

    // Flush
    backend.flush();
    CompletedFrame(
      buffer = buffers(1 - current),
      area = viewport.area
    )
  }

  def hide_cursor(): Unit = {
    backend.hide_cursor();
    hidden_cursor = true;
  }

  def show_cursor(): Unit = {
    backend.show_cursor();
    hidden_cursor = false;
  }

  def get_cursor(): (Int, Int) =
    backend.get_cursor()

  def set_cursor(x: Int, y: Int): Unit =
    backend.set_cursor(x, y)

  /// Clear the terminal and force a full redraw on the next draw call.
  def clear(): Unit = {
    backend.clear();
    // Reset the back buffer to make sure the next update will redraw everything.
    buffers(1 - current).reset();
  }

  /// Queries the real size of the backend.
  def size(): Rect =
    backend.size()
}

object Terminal {
  /// Wrapper around Terminal initialization. Each buffer is initialized with a blank string and
  /// default colors for the foreground and the background
  def init(backend: Backend): Terminal = {

    val size = backend.size()

    Terminal.with_options(
      backend,
      TerminalOptions(
        viewport = Viewport(
          area = size,
          resize_behavior = ResizeBehavior.Auto
        )
      )
    )
  }

  /// UNSTABLE
  def with_options(backend: Backend, options: TerminalOptions): Terminal =
    Terminal(
      backend,
      buffers = Array(
        Buffer.empty(options.viewport.area),
        Buffer.empty(options.viewport.area)
      ),
      current = 0,
      hidden_cursor = false,
      viewport = options.viewport
    )
}
