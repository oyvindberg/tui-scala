package jatatui.components.textinput;

import jatatui.core.style.Style;
import java.util.Optional;
import java.util.function.Consumer;

/// Props for the textInput Component.
///
/// Controlled: parent owns `value` and reacts to `onChange`. The component still tracks cursor
/// position internally (via `useState`) so consecutive renders don't reset the caret.
///
/// When `title` is non-empty, the input renders inside a bordered Block whose border style
/// changes color when focused — the standard "this field is active" affordance. When empty, the
/// input renders bare (no border) and the caller is responsible for any focus indication.
public record TextInputProps(
    String value,
    Consumer<String> onChange,
    String placeholder,
    String title,
    Optional<String> focusId,
    boolean autoFocus,
    Optional<Runnable> onSubmit,
    Optional<Runnable> onCancel,
    Style style,
    Style focusedStyle,
    Style placeholderStyle,
    Style cursorStyle,
    Style borderStyle,
    Style focusedBorderStyle) {

  /// Minimal-args factory: value + onChange. No placeholder, no title, default styles.
  public static TextInputProps of(String value, Consumer<String> onChange) {
    return new TextInputProps(
        value,
        onChange,
        "",
        "",
        Optional.empty(),
        false,
        Optional.empty(),
        Optional.empty(),
        Style.empty(),
        Style.empty(),
        Style.empty(),
        Style.empty(),
        Style.empty().withFg(new jatatui.core.style.Color.DarkGray()),
        Style.empty().withFg(new jatatui.core.style.Color.Yellow()));
  }

  public TextInputProps withValue(String value) { return copy(value, onChange, placeholder, title, focusId, autoFocus, onSubmit, onCancel, style, focusedStyle, placeholderStyle, cursorStyle, borderStyle, focusedBorderStyle); }
  public TextInputProps withOnChange(Consumer<String> onChange) { return copy(value, onChange, placeholder, title, focusId, autoFocus, onSubmit, onCancel, style, focusedStyle, placeholderStyle, cursorStyle, borderStyle, focusedBorderStyle); }
  public TextInputProps withPlaceholder(String placeholder) { return copy(value, onChange, placeholder, title, focusId, autoFocus, onSubmit, onCancel, style, focusedStyle, placeholderStyle, cursorStyle, borderStyle, focusedBorderStyle); }
  public TextInputProps withTitle(String title) { return copy(value, onChange, placeholder, title, focusId, autoFocus, onSubmit, onCancel, style, focusedStyle, placeholderStyle, cursorStyle, borderStyle, focusedBorderStyle); }
  public TextInputProps withFocusId(String focusId) { return copy(value, onChange, placeholder, title, Optional.of(focusId), autoFocus, onSubmit, onCancel, style, focusedStyle, placeholderStyle, cursorStyle, borderStyle, focusedBorderStyle); }
  public TextInputProps withAutoFocus(boolean autoFocus) { return copy(value, onChange, placeholder, title, focusId, autoFocus, onSubmit, onCancel, style, focusedStyle, placeholderStyle, cursorStyle, borderStyle, focusedBorderStyle); }
  public TextInputProps withOnSubmit(Runnable onSubmit) { return copy(value, onChange, placeholder, title, focusId, autoFocus, Optional.of(onSubmit), onCancel, style, focusedStyle, placeholderStyle, cursorStyle, borderStyle, focusedBorderStyle); }
  public TextInputProps withOnCancel(Runnable onCancel) { return copy(value, onChange, placeholder, title, focusId, autoFocus, onSubmit, Optional.of(onCancel), style, focusedStyle, placeholderStyle, cursorStyle, borderStyle, focusedBorderStyle); }
  public TextInputProps withStyle(Style style) { return copy(value, onChange, placeholder, title, focusId, autoFocus, onSubmit, onCancel, style, focusedStyle, placeholderStyle, cursorStyle, borderStyle, focusedBorderStyle); }
  public TextInputProps withFocusedStyle(Style focusedStyle) { return copy(value, onChange, placeholder, title, focusId, autoFocus, onSubmit, onCancel, style, focusedStyle, placeholderStyle, cursorStyle, borderStyle, focusedBorderStyle); }
  public TextInputProps withPlaceholderStyle(Style placeholderStyle) { return copy(value, onChange, placeholder, title, focusId, autoFocus, onSubmit, onCancel, style, focusedStyle, placeholderStyle, cursorStyle, borderStyle, focusedBorderStyle); }
  public TextInputProps withCursorStyle(Style cursorStyle) { return copy(value, onChange, placeholder, title, focusId, autoFocus, onSubmit, onCancel, style, focusedStyle, placeholderStyle, cursorStyle, borderStyle, focusedBorderStyle); }
  public TextInputProps withBorderStyle(Style borderStyle) { return copy(value, onChange, placeholder, title, focusId, autoFocus, onSubmit, onCancel, style, focusedStyle, placeholderStyle, cursorStyle, borderStyle, focusedBorderStyle); }
  public TextInputProps withFocusedBorderStyle(Style focusedBorderStyle) { return copy(value, onChange, placeholder, title, focusId, autoFocus, onSubmit, onCancel, style, focusedStyle, placeholderStyle, cursorStyle, borderStyle, focusedBorderStyle); }

  private static TextInputProps copy(String value, Consumer<String> onChange, String placeholder, String title, Optional<String> focusId, boolean autoFocus, Optional<Runnable> onSubmit, Optional<Runnable> onCancel, Style style, Style focusedStyle, Style placeholderStyle, Style cursorStyle, Style borderStyle, Style focusedBorderStyle) {
    return new TextInputProps(value, onChange, placeholder, title, focusId, autoFocus, onSubmit, onCancel, style, focusedStyle, placeholderStyle, cursorStyle, borderStyle, focusedBorderStyle);
  }
}
