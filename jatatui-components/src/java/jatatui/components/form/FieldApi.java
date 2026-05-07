package jatatui.components.form;

import java.util.Optional;
import java.util.function.Consumer;

/// Typed view of a single form field. Returned by [FormApi#useField].
public record FieldApi<T>(T value, Consumer<T> setValue, Optional<String> error) {

  /// Convenience: bind a textInput's `value` / `onChange` / `error` directly. Equivalent to
  /// `(value(), setValue())` plus an indicator if there's an error.
  public boolean hasError() {
    return error.isPresent();
  }
}
