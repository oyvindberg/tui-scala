package jatatui.core.layout.solver;

/// The possible error conditions that `Solver.addConstraint` can fail with.
public sealed interface AddConstraintError
    permits AddConstraintError.DuplicateConstraint,
        AddConstraintError.UnsatisfiableConstraint,
        AddConstraintError.InternalSolverError {

  /// The constraint specified has already been added to the solver.
  record DuplicateConstraint() implements AddConstraintError {}

  /// The constraint is required, but it is unsatisfiable in conjunction with the existing
  // constraints.
  record UnsatisfiableConstraint() implements AddConstraintError {}

  /// The solver entered an invalid state. If this occurs please report the issue. This variant
  // specifies
  /// additional details as a string.
  record InternalSolverError(String str) implements AddConstraintError {}
}
