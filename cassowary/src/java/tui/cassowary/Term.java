package tui.cassowary;

import java.util.Objects;

/// A variable and a coefficient to multiply that variable by. This is a sub-expression in
/// a constraint equation.
public final class Term {
  public final Variable variable;
  public double coefficient;

  public Term(Variable variable, double coefficient) {
    this.variable = variable;
    this.coefficient = coefficient;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof Term other)) return false;
    return variable.equals(other.variable) && Double.compare(coefficient, other.coefficient) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(variable, coefficient);
  }
}
