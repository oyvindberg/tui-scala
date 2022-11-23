package tui.crossterm;

public sealed interface MouseEventKind permits
        MouseEventKind.Down,
        MouseEventKind.Up,
        MouseEventKind.Drag,
        MouseEventKind.Moved,
        MouseEventKind.ScrollDown,
        MouseEventKind.ScrollUp {
    /// Pressed mouse button. Contains the button that was pressed.
    record Down(MouseButton mouseButton) implements MouseEventKind {
    }

    /// Released mouse button. Contains the button that was released.
    record Up(MouseButton mouseButton) implements MouseEventKind {
    }

    /// Moved the mouse cursor while pressing the contained mouse button.
    record Drag(MouseButton mouseButton) implements MouseEventKind {
    }

    /// Moved the mouse cursor while not pressing a mouse button.
    record Moved() implements MouseEventKind {
    }

    /// Scrolled mouse wheel downwards (towards the user).
    record ScrollDown() implements MouseEventKind {
    }

    /// Scrolled mouse wheel upwards (away from the user).
    record ScrollUp() implements MouseEventKind {
    }
}
