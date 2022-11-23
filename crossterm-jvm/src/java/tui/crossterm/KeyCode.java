package tui.crossterm;

/// Represents a key.
public sealed interface KeyCode
        permits KeyCode.Backspace,
        KeyCode.Enter,
        KeyCode.Left,
        KeyCode.Right,
        KeyCode.Up,
        KeyCode.Down,
        KeyCode.Home,
        KeyCode.End,
        KeyCode.PageUp,
        KeyCode.PageDown,
        KeyCode.Tab,
        KeyCode.BackTab,
        KeyCode.Delete,
        KeyCode.Insert,
        KeyCode.F,
        KeyCode.Char,
        KeyCode.Null,
        KeyCode.Esc,
        KeyCode.CapsLock,
        KeyCode.ScrollLock,
        KeyCode.NumLock,
        KeyCode.PrintScreen,
        KeyCode.Pause,
        KeyCode.Menu,
        KeyCode.KeypadBegin,
        KeyCode.Media,
        KeyCode.Modifier {
    /// Backspace key.
    record Backspace() implements KeyCode {
    }

    /// Enter key.
    record Enter() implements KeyCode {
    }

    /// Left arrow key.
    record Left() implements KeyCode {
    }

    /// Right arrow key.
    record Right() implements KeyCode {
    }

    /// Up arrow key.
    record Up() implements KeyCode {
    }

    /// Down arrow key.
    record Down() implements KeyCode {
    }

    /// Home key.
    record Home() implements KeyCode {
    }

    /// End key.
    record End() implements KeyCode {
    }

    /// Page up key.
    record PageUp() implements KeyCode {
    }

    /// Page down key.
    record PageDown() implements KeyCode {
    }

    /// Tab key.
    record Tab() implements KeyCode {
    }

    /// Shift + Tab key.
    record BackTab() implements KeyCode {
    }

    /// Delete key.
    record Delete() implements KeyCode {
    }

    /// Insert key.
    record Insert() implements KeyCode {
    }

    /// F key.
    ///
    /// `KeyCode::F(1)` represents F1 key, etc.
    record F(int num) implements KeyCode {
    }

    /// A character.
    ///
    /// `KeyCode::Char('c')` represents `c` character, etc.
    record Char(char c) implements KeyCode {
    }

    /// Null.
    record Null() implements KeyCode {
    }

    /// Escape key.
    record Esc() implements KeyCode {
    }

    /// Caps Lock key.
    ///
    /// **Note:** this key can only be read if
    /// [`KeyboardEnhancementFlags::DISAMBIGUATE_ESCAPE_CODES`] has been enabled with
    /// [`PushKeyboardEnhancementFlags`].
    record CapsLock() implements KeyCode {
    }

    /// Scroll Lock key.
    ///
    /// **Note:** this key can only be read if
    /// [`KeyboardEnhancementFlags::DISAMBIGUATE_ESCAPE_CODES`] has been enabled with
    /// [`PushKeyboardEnhancementFlags`].
    record ScrollLock() implements KeyCode {
    }

    /// Num Lock key.
    ///
    /// **Note:** this key can only be read if
    /// [`KeyboardEnhancementFlags::DISAMBIGUATE_ESCAPE_CODES`] has been enabled with
    /// [`PushKeyboardEnhancementFlags`].
    record NumLock() implements KeyCode {
    }

    /// Print Screen key.
    ///
    /// **Note:** this key can only be read if
    /// [`KeyboardEnhancementFlags::DISAMBIGUATE_ESCAPE_CODES`] has been enabled with
    /// [`PushKeyboardEnhancementFlags`].
    record PrintScreen() implements KeyCode {
    }

    /// Pause key.
    ///
    /// **Note:** this key can only be read if
    /// [`KeyboardEnhancementFlags::DISAMBIGUATE_ESCAPE_CODES`] has been enabled with
    /// [`PushKeyboardEnhancementFlags`].
    record Pause() implements KeyCode {
    }

    /// Menu key.
    ///
    /// **Note:** this key can only be read if
    /// [`KeyboardEnhancementFlags::DISAMBIGUATE_ESCAPE_CODES`] has been enabled with
    /// [`PushKeyboardEnhancementFlags`].
    record Menu() implements KeyCode {
    }

    /// The "Begin" key (often mapped to the 5 key when Num Lock is turned on).
    ///
    /// **Note:** this key can only be read if
    /// [`KeyboardEnhancementFlags::DISAMBIGUATE_ESCAPE_CODES`] has been enabled with
    /// [`PushKeyboardEnhancementFlags`].
    record KeypadBegin() implements KeyCode {
    }

    /// A media key.
    ///
    /// **Note:** these keys can only be read if
    /// [`KeyboardEnhancementFlags::DISAMBIGUATE_ESCAPE_CODES`] has been enabled with
    /// [`PushKeyboardEnhancementFlags`].
    record Media(MediaKeyCode mediaKeyCode) implements KeyCode {
    }

    /// A modifier key.
    ///
    /// **Note:** these keys can only be read if **both**
    /// [`KeyboardEnhancementFlags::DISAMBIGUATE_ESCAPE_CODES`] and
    /// [`KeyboardEnhancementFlags::REPORT_ALL_KEYS_AS_ESCAPE_CODES`] have been enabled with
    /// [`PushKeyboardEnhancementFlags`].
    record Modifier(ModifierKeyCode modifierKeyCode) implements KeyCode {
    }
}
