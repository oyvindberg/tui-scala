package tui.crossterm;

public record KeyboardEnhancementFlags(int bits) {
    /// Represent Escape and modified keys using CSI-u sequences, so they can be unambiguously
    /// read.
    public static final int DISAMBIGUATE_ESCAPE_CODES = 0b0000_0001;
    /// Add extra events with [`KeyEvent.kind`] set to [`KeyEventKind::Repeat`] or
    /// [`KeyEventKind::Release`] when keys are autorepeated or released.
    public static final int REPORT_EVENT_TYPES = 0b0000_0010;
    // Send [alternate keycodes](https://sw.kovidgoyal.net/kitty/keyboard-protocol/#key-codes)
    // in addition to the base keycode.
    //
    // *Note*: these are not yet supported by crossterm.
    public static final int REPORT_ALTERNATE_KEYS = 0b0000_0100;
    /// Represent all keyboard events as CSI-u sequences. This is required to get repeat/release
    /// events for plain-text keys.
    public static final int REPORT_ALL_KEYS_AS_ESCAPE_CODES = 0b0000_1000;
    // Send the Unicode codepoint as well as the keycode.
    //
    // *Note*: this is not yet supported by crossterm.
    public static final int REPORT_ASSOCIATED_TEXT = 0b0001_0000;
}
