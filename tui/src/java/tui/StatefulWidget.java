package tui;

/// A `StatefulWidget` is a widget that can take advantage of some local state to remember things between two draw calls.
///
/// Most widgets can be drawn directly based on the input parameters. However, some features may require some kind of associated state to be implemented.
public interface StatefulWidget<State> {
  void render(Rect area, Buffer buf, State state);
}
