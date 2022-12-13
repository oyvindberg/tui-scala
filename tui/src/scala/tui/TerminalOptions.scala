package tui

//#[derive(Debug, Clone, PartialEq)]
/// Options to pass to [`Terminal::with_options`]
case class TerminalOptions(
    /// Viewport used to draw to the terminal
    viewport: Viewport
)
