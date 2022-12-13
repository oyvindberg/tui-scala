package tui

/// A `StatefulWidget` is a widget that can take advantage of some local state to remember things
/// between two draw calls.
///
/// Most widgets can be drawn directly based on the input parameters. However, some features may
/// require some kind of associated state to be implemented.
///
/// For example, the [`List`] widget can highlight the item currently selected. This can be
/// translated in an offset, which is the number of elements to skip in order to have the selected
/// item within the viewport currently allocated to this widget. The widget can therefore only
/// provide the following behavior: whenever the selected item is out of the viewport scroll to a
/// predefined position (making the selected item the last viewable item or the one in the middle
/// for example). Nonetheless, if the widget has access to the last computed offset then it can
/// implement a natural scrolling experience where the last offset is reused until the selected
/// item is out of the viewport.
///
trait StatefulWidget {
  type State;
  def render(area: Rect, buf: Buffer, state: State): Unit
}
