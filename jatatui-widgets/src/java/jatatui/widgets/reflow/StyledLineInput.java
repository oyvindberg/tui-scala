package jatatui.widgets.reflow;

import jatatui.core.layout.HorizontalAlignment;
import jatatui.core.text.StyledGrapheme;
import java.util.Iterator;

/// An input line for a [LineComposer]: a stream of [StyledGrapheme]s plus a per-line
/// [HorizontalAlignment].
///
/// Mirrors the upstream tuple `(I, Alignment)` where `I: Iterator<Item = StyledGrapheme>` used
/// by `WordWrapper` and `LineTruncator`. The naming follows the playbook rule that tuples get a
/// dedicated record type.
public record StyledLineInput(Iterator<StyledGrapheme> graphemes, HorizontalAlignment alignment) {}
