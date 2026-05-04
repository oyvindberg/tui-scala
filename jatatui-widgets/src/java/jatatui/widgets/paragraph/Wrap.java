package jatatui.widgets.paragraph;

/// Describes how to wrap text across lines in a [Paragraph].
///
/// Mirrors `ratatui_widgets::paragraph::Wrap`.
///
/// `trim` controls whether leading whitespace is stripped from wrapped lines:
/// - `true` — leading whitespace is trimmed (the indented "bullet point" demo in the upstream
///   docs becomes flush against the left margin after wrapping).
/// - `false` — indentation is preserved across wrapped lines.
public record Wrap(boolean trim) {}
