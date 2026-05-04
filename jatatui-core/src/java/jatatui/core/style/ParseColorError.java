package jatatui.core.style;

/// Error type indicating a failure to parse a color string.
///
/// Returned in the `left` of [jatatui.core.layout.solver.Either] from [Color#fromString(String)].
public record ParseColorError() {
  @Override
  public String toString() {
    return "Failed to parse Colors";
  }
}
