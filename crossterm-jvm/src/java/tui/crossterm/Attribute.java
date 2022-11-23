package tui.crossterm;

public enum Attribute {
    /// Resets all the attributes.
    Reset,
    /// Increases the text intensity.
    Bold,
    /// Decreases the text intensity.
    Dim,
    /// Emphasises the text.
    Italic,
    /// Underlines the text.
    Underlined,

    // Other types of underlining
    /// Double underlines the text.
    DoubleUnderlined,
    /// Undercurls the text.
    Undercurled,
    /// Underdots the text.
    Underdotted,
    /// Underdashes the text.
    Underdashed,

    /// Makes the text blinking (< 150 per minute).
    SlowBlink,
    /// Makes the text blinking (>= 150 per minute).
    RapidBlink,
    /// Swaps foreground and background colors.
    Reverse,
    /// Hides the text (also known as Conceal).
    Hidden,
    /// Crosses the text.
    CrossedOut,
    /// Sets the [Fraktur](https://en.wikipedia.org/wiki/Fraktur) typeface.
    ///
    /// Mostly used for [mathematical alphanumeric symbols](https://en.wikipedia.org/wiki/Mathematical_Alphanumeric_Symbols).
    Fraktur,
    /// Turns off the `Bold` attribute. - Inconsistent - Prefer to use NormalIntensity
    NoBold,
    /// Switches the text back to normal intensity (no bold, italic).
    NormalIntensity,
    /// Turns off the `Italic` attribute.
    NoItalic,
    /// Turns off the `Underlined` attribute.
    NoUnderline,
    /// Turns off the text blinking (`SlowBlink` or `RapidBlink`).
    NoBlink,
    /// Turns off the `Reverse` attribute.
    NoReverse,
    /// Turns off the `Hidden` attribute.
    NoHidden,
    /// Turns off the `CrossedOut` attribute.
    NotCrossedOut,
    /// Makes the text framed.
    Framed,
    /// Makes the text encircled.
    Encircled,
    /// Draws a line at the top of the text.
    OverLined,
    /// Turns off the `Frame` and `Encircled` attributes.
    NotFramedOrEncircled,
    /// Turns off the `OverLined` attribute.
    NotOverLined,
}


