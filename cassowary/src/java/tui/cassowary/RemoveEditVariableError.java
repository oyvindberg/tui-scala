package tui.cassowary;

/// The possible error conditions that `Solver.removeEditVariable` can fail with.
public sealed interface RemoveEditVariableError
    permits RemoveEditVariableError.UnknownEditVariable, RemoveEditVariableError.InternalSolverError {

  /// The specified variable was not an edit variable in the solver, so cannot be removed.
  record UnknownEditVariable() implements RemoveEditVariableError {}

  /// The solver entered an invalid state. If this occurs please report the issue. This variant specifies
  /// additional details as a string.
  record InternalSolverError(String str) implements RemoveEditVariableError {}
}
