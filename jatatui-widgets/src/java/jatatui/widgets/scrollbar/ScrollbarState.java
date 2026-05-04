package jatatui.widgets.scrollbar;

import java.util.Objects;

/// A struct representing the state of a [Scrollbar] widget.
///
/// Mirrors `ratatui_widgets::scrollbar::ScrollbarState` (v0.30).
///
/// # Important
///
/// It's essential to set the `contentLength` field when using this struct. This field represents
/// the total length of the scrollable content. The default value is zero which will result in the
/// scrollbar not rendering.
///
/// For example, in the following list, assume there are 4 bullet points:
///
/// - the `contentLength` is 4
/// - the `position` is 0
/// - the `viewportContentLength` is 2
///
/// ```text
/// ┌───────────────┐
/// │1. this is a   █
/// │   single item █
/// │2. this is a   ║
/// │   second item ║
/// └───────────────┘
/// ```
///
/// If you don't have multi-line content, you can leave the `viewportContentLength` set to the
/// default and it'll use the track size as a `viewportContentLength`.
///
/// State is **mutable**: [#prev()], [#next()], [#first()], [#last()], [#scroll(ScrollDirection)]
/// modify it in place. Fluent setters [#withPosition(int)] etc. are also available for one-shot
/// initialization (return `this`).
public final class ScrollbarState {

  /// The total length of the scrollable content.
  public int contentLength;

  /// The current position within the scrollable content.
  public int position;

  /// The length of content in current viewport. A value of `0` means "use the track size".
  public int viewportContentLength;

  private ScrollbarState(int contentLength, int position, int viewportContentLength) {
    this.contentLength = contentLength;
    this.position = position;
    this.viewportContentLength = viewportContentLength;
  }

  /// Constructs a new [ScrollbarState] with the specified content length.
  public static ScrollbarState of(int contentLength) {
    return new ScrollbarState(contentLength, 0, 0);
  }

  /// Constructs a new [ScrollbarState] with `contentLength = 0`.
  public static ScrollbarState empty() {
    return new ScrollbarState(0, 0, 0);
  }

  // ---- Fluent setters (mutate in place, return this) ----

  /// Sets the scroll position of the scrollbar (mutates in place).
  public ScrollbarState withPosition(int position) {
    this.position = position;
    return this;
  }

  /// Sets the length of the scrollable content (mutates in place).
  public ScrollbarState withContentLength(int contentLength) {
    this.contentLength = contentLength;
    return this;
  }

  /// Sets the items' size — the length of content in the current viewport (mutates in place).
  public ScrollbarState withViewportContentLength(int viewportContentLength) {
    this.viewportContentLength = viewportContentLength;
    return this;
  }

  // ---- Mutating helpers ----

  /// Decrements the scroll position by one, ensuring it doesn't go below zero.
  public void prev() {
    position = Math.max(0, position - 1);
  }

  /// Increments the scroll position by one, ensuring it doesn't exceed the length of the content.
  public void next() {
    int max = Math.max(0, contentLength - 1);
    position = Math.min(max, position + 1);
  }

  /// Sets the scroll position to the start of the scrollable content.
  public void first() {
    position = 0;
  }

  /// Sets the scroll position to the end of the scrollable content.
  public void last() {
    position = Math.max(0, contentLength - 1);
  }

  /// Changes the scroll position based on the provided [ScrollDirection].
  public void scroll(ScrollDirection direction) {
    switch (direction) {
      case Forward -> next();
      case Backward -> prev();
    }
  }

  /// Returns the current position within the scrollable content.
  public int getPosition() {
    return position;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ScrollbarState other)) return false;
    return contentLength == other.contentLength
        && position == other.position
        && viewportContentLength == other.viewportContentLength;
  }

  @Override
  public int hashCode() {
    return Objects.hash(contentLength, position, viewportContentLength);
  }

  @Override
  public String toString() {
    return "ScrollbarState[contentLength="
        + contentLength
        + ", position="
        + position
        + ", viewportContentLength="
        + viewportContentLength
        + "]";
  }
}
