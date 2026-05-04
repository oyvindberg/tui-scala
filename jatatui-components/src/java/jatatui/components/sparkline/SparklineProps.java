package jatatui.components.sparkline;

import jatatui.core.style.Style;
import java.util.List;
import java.util.Optional;

/// Immutable props for the React-style sparkline component.
///
/// Stateless and controlled — the parent owns `data` and pushes new immutable lists in to drive
/// the chart. Memoization works on `equals()`, but with `List<Long>` reference identity matters:
/// passing the same list instance across renders skips the body entirely (cheap fast-path), while
/// a value-equal but different instance still avoids re-rendering thanks to `List.equals()`.
///
///   - `title`     — block title (rendered with `Borders.ALL`).
///   - `data`      — bar values, oldest first; truncated by the underlying widget if longer than
///                   the available width.
///   - `max`       — optional fixed maximum used to scale bar heights. Empty → use the dataset's
///                   own max each frame.
///   - `style`     — base style applied to the whole widget.
public record SparklineProps(String title, List<Long> data, Optional<Long> max, Style style) {}
