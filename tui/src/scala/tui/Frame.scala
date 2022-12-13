package tui

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
