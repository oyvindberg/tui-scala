package jatatui.core.layout.solver;

/// The possible error conditions that `Solver.addEditVariable` can fail with.
public sealed interface AddEditVariableError
    permits AddEditVariableError.DuplicateEditVariable, AddEditVariableError.BadRequiredStrength {

  /// The specified variable is already marked as an edit variable in the solver.
  record DuplicateEditVariable() implements AddEditVariableError {}

  /// The specified strength was `REQUIRED`. This is illegal for edit variable strengths.
  record BadRequiredStrength() implements AddEditVariableError {}
}
