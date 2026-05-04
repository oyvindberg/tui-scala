package jatatui.widgets.reflow;

import jatatui.core.layout.HorizontalAlignment;
import jatatui.core.text.StyledGrapheme;
import java.util.List;

/// A line that has been wrapped to a certain width.
///
/// Mirrors `ratatui_widgets::reflow::WrappedLine`. The lifetimes on the upstream struct are
/// elided in Java; see the doc on [LineComposer] for the borrowing semantics — a `WrappedLine`
/// returned by a [LineComposer] is only valid until the next call to [LineComposer#nextLine()].
public record WrappedLine(
    List<StyledGrapheme> graphemes, int width, HorizontalAlignment alignment) {}
