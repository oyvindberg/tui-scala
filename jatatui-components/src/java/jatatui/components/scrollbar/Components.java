package jatatui.components.scrollbar;

import jatatui.react.Element;
import jatatui.widgets.scrollbar.ScrollbarOrientation;
import java.util.Optional;

/// Static factories for the React-style scrollbar. Mirrors the convention in
/// [jatatui.react.Components]: import statically and call `scrollbar(...)`.
///
/// API choice: **controlled**. The caller owns `position` and `contentLength` and passes them in
/// as props; the scrollbar is purely visual. A higher-level "ScrollableArea container" component
/// that internally manages scroll state (see DESIGN.md / future work) could be layered on top of
/// this without changing the controlled component.
///
/// Implementation note: [Element] is a sealed interface in `jatatui-react`, so the scrollbar is
/// surfaced as an [Element.WidgetWrap] wrapping a [ScrollbarWidget] that converts the props into
/// a freshly-constructed [jatatui.widgets.scrollbar.ScrollbarState] each render.
public final class Components {
  private Components() {}

  /// Scrollbar from a fully-specified [ScrollbarProps] record.
  public static Element scrollbar(ScrollbarProps props) {
    return jatatui.react.Components.widget(new ScrollbarWidget(props));
  }

  /// Vertical-right scrollbar (the most common case). Viewport length defaults to the track size.
  public static Element scrollbar(int position, int contentLength) {
    return scrollbar(ScrollbarProps.verticalRight(position, contentLength));
  }

  /// Scrollbar with explicit orientation. Viewport length defaults to the track size.
  public static Element scrollbar(
      int position, int contentLength, ScrollbarOrientation orientation) {
    return scrollbar(new ScrollbarProps(position, contentLength, Optional.empty(), orientation));
  }

  /// Scrollbar with explicit orientation and explicit viewport content length.
  public static Element scrollbar(
      int position,
      int contentLength,
      int viewportContentLength,
      ScrollbarOrientation orientation) {
    return scrollbar(
        new ScrollbarProps(
            position, contentLength, Optional.of(viewportContentLength), orientation));
  }
}
