package tui.crossterm;

public record KeyEventState(int bits) {
  /// The key event origins from the keypad.
  public static final int KEYPAD = 0b0000_0001;
  /// Caps Lock was enabled for this key event.
  ///
  /// **Note:** this is set for the initial press of Caps Lock itself.
  ///
  /// **crossterm 0.28 bit layout fix:** prior to crossterm 0.28 this constant
  /// shared a bit (`0b0000_1000`) with [`NUM_LOCK`], which made the two states
  /// indistinguishable. They are now distinct.
  public static final int CAPS_LOCK = 0b0000_0010;
  /// Num Lock was enabled for this key event.
  ///
  /// **Note:** this is set for the initial press of Num Lock itself.
  public static final int NUM_LOCK = 0b0000_0100;
  public static final int NONE = 0b0000_0000;
}
