package tui.crossterm;

public record KeyEvent(
        /// The key itself.
        KeyCode code,
        /// Additional key modifiers.
        KeyModifiers modifiers,
        /// Kind of event.
        KeyEventKind kind,
        /// Keyboard state.
        ///
        /// Only set if [`KeyboardEnhancementFlags::DISAMBIGUATE_ESCAPE_CODES`] has been enabled with
        /// [`PushKeyboardEnhancementFlags`].
        KeyEventState state
) {
}
