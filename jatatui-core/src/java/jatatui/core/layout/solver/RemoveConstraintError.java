package jatatui.core.layout.solver;

/// The possible error conditions that `Solver.removeConstraint` can fail with.
public sealed interface RemoveConstraintError
    permits RemoveConstraintError.UnknownConstraint, RemoveConstraintError.InternalSolverError {

  /// The constraint specified was not already in the solver, so cannot be removed.
  record UnknownConstraint() implements RemoveConstraintError {}

  /// The solver entered an invalid state. If this occurs please report the issue. This variant
  // specifies
  /// additional details as a string.
  record InternalSolverError(String str) implements RemoveConstraintError {}
}
