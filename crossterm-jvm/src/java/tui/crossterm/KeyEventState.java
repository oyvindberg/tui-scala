package tui.crossterm;

public record KeyEventState(int bits) {
    /// The key event origins from the keypad.
    public static final int KEYPAD = 0b0000_0001;
    /// Caps Lock was enabled for this key event.
    ///
    /// **Note:** this is set for the initial press of Caps Lock itself.
    public static final int CAPS_LOCK = 0b0000_1000;
    /// Num Lock was enabled for this key event.
    ///
    /// **Note:** this is set for the initial press of Num Lock itself.
    public static final int NUM_LOCK = 0b0000_1000;
    public static final int NONE = 0b0000_0000;
}
