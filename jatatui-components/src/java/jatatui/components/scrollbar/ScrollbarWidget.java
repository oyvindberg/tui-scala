package jatatui.components.scrollbar;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Rect;
import jatatui.core.widgets.Widget;
import jatatui.react.Element;
import jatatui.widgets.scrollbar.Scrollbar;
import jatatui.widgets.scrollbar.ScrollbarState;

/// Internal: a [Widget] that renders the underlying [Scrollbar] from a controlled
/// [ScrollbarProps]. We cannot implement [Element] directly because it is sealed in
/// `jatatui-react`, so the React-shaped surface is exposed via [Element.WidgetWrap].
///
/// Each render constructs a fresh [ScrollbarState] from the props — this is how the existing
/// widget's mutable state object maps into the React "props in, render out" model: we never let
/// the mutable state survive a frame; it is constructed, used, and dropped within `render`.
final class ScrollbarWidget implements Widget {

  private final ScrollbarProps props;

  ScrollbarWidget(ScrollbarProps props) {
    this.props = props;
  }

  @Override
  public void render(Rect area, Buffer buf) {
    ScrollbarState state = ScrollbarState.of(props.contentLength()).withPosition(props.position());
    props.viewportContentLength().ifPresent(state::withViewportContentLength);
    Scrollbar.of(props.orientation()).render(area, buf, state);
  }
}
