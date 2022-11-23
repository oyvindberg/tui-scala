package tui.crossterm;

public sealed interface Event permits Event.FocusGained, Event.FocusLost, Event.Key, Event.Mouse, Event.Paste, Event.Resize {
    /// The terminal gained focus
    record FocusGained() implements Event {
    }

    /// The terminal lost focus
    record FocusLost() implements Event {
    }

    /// A single key event with additional pressed modifiers.
    record Key(KeyEvent keyEvent) implements Event {
    }

    /// A single mouse event with additional pressed modifiers.
    record Mouse(MouseEvent mouseEvent) implements Event {
    }

    /// A string that was pasted into the terminal. Only emitted if bracketed paste has been
    /// enabled.
    record Paste(String string) implements Event {
    }

    /// An resize event with new dimensions after resize (columns, rows).
    /// **Note** that resize events can occur in batches.
    record Resize(int columns, int rows) implements Event {
    }
}
