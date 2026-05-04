package jatatui.widgets.reflow;

import java.util.Optional;

/// A state machine to pack styled symbols into lines.
///
/// Mirrors `ratatui_widgets::reflow::LineComposer`. The Rust trait yields slices of an internal
/// buffer (a streaming iterator), so it cannot use the `Iterator` trait directly. In Java we
/// expose the same shape through a single [#nextLine()] method that returns an [Optional]
/// [WrappedLine]. The implementation is free to mutate its internal state on every call —
/// references handed out via [WrappedLine] become invalid as soon as [#nextLine()] is called
/// again.
public interface LineComposer {

  /// Returns the next [WrappedLine], or [Optional#empty()] when the input is exhausted.
  ///
  /// The returned [WrappedLine] borrows internal state of the composer; its [WrappedLine#graphemes]
  /// list is valid only until the next call to [#nextLine()]. Callers that need to keep the
  /// graphemes around beyond a single call should make a defensive copy.
  Optional<WrappedLine> nextLine();
}
