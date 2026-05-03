package tui.cassowary;

/// The possible error conditions that `Solver.suggestValue` can fail with.
public sealed interface SuggestValueError
    permits SuggestValueError.UnknownEditVariable, SuggestValueError.InternalSolverError {

  /// The specified variable was not an edit variable in the solver, so cannot have its value suggested.
  record UnknownEditVariable() implements SuggestValueError {}

  /// The solver entered an invalid state. If this occurs please report the issue. This variant specifies
  /// additional details as a string.
  record InternalSolverError(String str) implements SuggestValueError {}
}
