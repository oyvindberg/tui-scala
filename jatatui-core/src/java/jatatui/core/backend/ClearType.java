package jatatui.core.backend;

/// The different kinds of clearing operations a terminal supports.
///
/// Mirrors the upstream `ratatui_core::backend::ClearType` enum (v0.30). Upstream derives
/// `strum::Display` and `strum::EnumString`; we get matching `toString` for free from Java's enum
/// (the variant name) and reproduce the parsing helper as [#fromString(String)].
public enum ClearType {
  /// Clear the entire screen.
  All,
  /// Clear everything after the cursor.
  AfterCursor,
  /// Clear everything before the cursor.
  BeforeCursor,
  /// Clear the current line.
  CurrentLine,
  /// Clear everything from the cursor until the next newline.
  UntilNewLine;

  /// Parse a string into a [ClearType], returning [java.util.Optional#empty()] if the name is
  /// unknown. Matches the behaviour of upstream `ClearType::from_str` (which returns
  /// `strum::ParseError::VariantNotFound` on failure).
  public static java.util.Optional<ClearType> fromString(String s) {
    for (ClearType t : values()) {
      if (t.name().equals(s)) return java.util.Optional.of(t);
    }
    return java.util.Optional.empty();
  }
}
