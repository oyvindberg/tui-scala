package jatatui.components.scrollbar;

import jatatui.widgets.scrollbar.ScrollbarOrientation;
import java.util.Optional;

/// Immutable props for the React-style scrollbar component.
///
/// Controlled: the caller owns `position` and `contentLength`. The component is purely visual —
/// every render translates these props into a freshly constructed
/// [jatatui.widgets.scrollbar.ScrollbarState] which is fed to the underlying
/// [jatatui.widgets.scrollbar.Scrollbar] widget.
///
///   - `position`            — current scroll offset within the content (0-based).
///   - `contentLength`       — total length of the scrollable content. `0` makes the widget render
///                             nothing — same convention as the underlying widget.
///   - `viewportContentLength` — `Optional.empty()` means "use the track size". Otherwise the
///                             length of content visible in the viewport (see
///                             [jatatui.widgets.scrollbar.ScrollbarState#viewportContentLength]).
///   - `orientation`         — where the scrollbar sits relative to the area it renders into.
public record ScrollbarProps(
    int position,
    int contentLength,
    Optional<Integer> viewportContentLength,
    ScrollbarOrientation orientation) {

  /// Vertical-right scrollbar with the given position / content length, viewport length defaulted
  /// to the track size.
  public static ScrollbarProps verticalRight(int position, int contentLength) {
    return new ScrollbarProps(
        position, contentLength, Optional.empty(), ScrollbarOrientation.VerticalRight);
  }

  /// Vertical-left scrollbar with the given position / content length.
  public static ScrollbarProps verticalLeft(int position, int contentLength) {
    return new ScrollbarProps(
        position, contentLength, Optional.empty(), ScrollbarOrientation.VerticalLeft);
  }

  /// Horizontal-bottom scrollbar with the given position / content length.
  public static ScrollbarProps horizontalBottom(int position, int contentLength) {
    return new ScrollbarProps(
        position, contentLength, Optional.empty(), ScrollbarOrientation.HorizontalBottom);
  }

  /// Horizontal-top scrollbar with the given position / content length.
  public static ScrollbarProps horizontalTop(int position, int contentLength) {
    return new ScrollbarProps(
        position, contentLength, Optional.empty(), ScrollbarOrientation.HorizontalTop);
  }

  public ScrollbarProps withPosition(int newPosition) {
    return new ScrollbarProps(newPosition, contentLength, viewportContentLength, orientation);
  }

  public ScrollbarProps withContentLength(int newContentLength) {
    return new ScrollbarProps(position, newContentLength, viewportContentLength, orientation);
  }

  public ScrollbarProps withViewportContentLength(int newViewportContentLength) {
    return new ScrollbarProps(
        position, contentLength, Optional.of(newViewportContentLength), orientation);
  }

  public ScrollbarProps withOrientation(ScrollbarOrientation newOrientation) {
    return new ScrollbarProps(position, contentLength, viewportContentLength, newOrientation);
  }
}
