package tui.cassowary;

/// The possible relations that a constraint can specify.
public enum RelationalOperator {
  /// `<=`
  LessOrEqual,
  /// `==`
  Equal,
  /// `>=`
  GreaterOrEqual;

  @Override
  public String toString() {
    return switch (this) {
      case LessOrEqual -> "<=";
      case Equal -> "==";
      case GreaterOrEqual -> ">=";
    };
  }
}
