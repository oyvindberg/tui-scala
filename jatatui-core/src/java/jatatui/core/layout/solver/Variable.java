package jatatui.core.layout.solver;

import java.util.concurrent.atomic.AtomicInteger;

/// Identifies a variable for the constraint solver.
/// Each new variable is unique in the view of the solver, but copying or cloning the variable produces
/// a copy of the same variable.
public record Variable(int value) {
  private static final AtomicInteger VARIABLE_ID = new AtomicInteger(0);

  public static Variable force(int value) {
    return new Variable(value);
  }

  /// Produces a new unique variable for use in constraint solving.
  public static Variable create() {
    return new Variable(VARIABLE_ID.getAndIncrement());
  }
}
