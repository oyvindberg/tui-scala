package jatatui.components.textinput;

import jatatui.core.style.Style;
import java.util.Optional;
import java.util.function.Consumer;

/// Props for the textInput Component.
///
/// Controlled: parent owns `value` and reacts to `onChange`. The component still tracks cursor
/// position internally (via `useState`) so consecutive renders don't reset the caret.
public record TextInputProps(
    String value,
    Consumer<String> onChange,
    String placeholder,
    Optional<String> focusId,
    boolean autoFocus,
    Optional<Runnable> onSubmit,
    Optional<Runnable> onCancel,
    Style style,
    Style focusedStyle,
    Style placeholderStyle,
    Style cursorStyle) {

  /// Minimal-args factory: value + onChange. No placeholder, default styles, not auto-focused.
  public static TextInputProps of(String value, Consumer<String> onChange) {
    return new TextInputProps(
        value,
        onChange,
        "",
        Optional.empty(),
        false,
        Optional.empty(),
        Optional.empty(),
        Style.empty(),
        Style.empty(),
        Style.empty(),
        Style.empty());
  }

  public TextInputProps withValue(String value) {
    return new TextInputProps(value, onChange, placeholder, focusId, autoFocus, onSubmit, onCancel, style, focusedStyle, placeholderStyle, cursorStyle);
  }

  public TextInputProps withOnChange(Consumer<String> onChange) {
    return new TextInputProps(value, onChange, placeholder, focusId, autoFocus, onSubmit, onCancel, style, focusedStyle, placeholderStyle, cursorStyle);
  }

  public TextInputProps withPlaceholder(String placeholder) {
    return new TextInputProps(value, onChange, placeholder, focusId, autoFocus, onSubmit, onCancel, style, focusedStyle, placeholderStyle, cursorStyle);
  }

  public TextInputProps withFocusId(String focusId) {
    return new TextInputProps(value, onChange, placeholder, Optional.of(focusId), autoFocus, onSubmit, onCancel, style, focusedStyle, placeholderStyle, cursorStyle);
  }

  public TextInputProps withAutoFocus(boolean autoFocus) {
    return new TextInputProps(value, onChange, placeholder, focusId, autoFocus, onSubmit, onCancel, style, focusedStyle, placeholderStyle, cursorStyle);
  }

  public TextInputProps withOnSubmit(Runnable onSubmit) {
    return new TextInputProps(value, onChange, placeholder, focusId, autoFocus, Optional.of(onSubmit), onCancel, style, focusedStyle, placeholderStyle, cursorStyle);
  }

  public TextInputProps withOnCancel(Runnable onCancel) {
    return new TextInputProps(value, onChange, placeholder, focusId, autoFocus, onSubmit, Optional.of(onCancel), style, focusedStyle, placeholderStyle, cursorStyle);
  }

  public TextInputProps withStyle(Style style) {
    return new TextInputProps(value, onChange, placeholder, focusId, autoFocus, onSubmit, onCancel, style, focusedStyle, placeholderStyle, cursorStyle);
  }

  public TextInputProps withFocusedStyle(Style focusedStyle) {
    return new TextInputProps(value, onChange, placeholder, focusId, autoFocus, onSubmit, onCancel, style, focusedStyle, placeholderStyle, cursorStyle);
  }

  public TextInputProps withPlaceholderStyle(Style placeholderStyle) {
    return new TextInputProps(value, onChange, placeholder, focusId, autoFocus, onSubmit, onCancel, style, focusedStyle, placeholderStyle, cursorStyle);
  }

  public TextInputProps withCursorStyle(Style cursorStyle) {
    return new TextInputProps(value, onChange, placeholder, focusId, autoFocus, onSubmit, onCancel, style, focusedStyle, placeholderStyle, cursorStyle);
  }
}
