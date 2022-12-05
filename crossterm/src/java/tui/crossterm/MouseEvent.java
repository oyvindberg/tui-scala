package tui.crossterm;

public record MouseEvent(
        /// The kind of mouse event that was caused.
        MouseEventKind kind,
        /// The column that the event occurred on.
        int column,
        /// The row that the event occurred on.
        int row,
        /// The key modifiers active when the event occurred.
        KeyModifiers modifiers
) {
}

