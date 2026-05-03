package tui.cassowary;

/// A constraint, consisting of an equation governed by an expression and a relational operator,
/// and an associated strength.
public final class Constraint {
  /// The expression of the left hand side of the constraint equation.
  public final Expression expression;
  /// The strength of the constraint that the solver will use.
  public final Strength strength;
  /// The relational operator governing the constraint.
  public final RelationalOperator op;

  private final int hash;

  public Constraint(Expression expression, Strength strength, RelationalOperator op) {
    this.expression = expression;
    this.strength = strength;
    this.op = op;
    this.hash = System.identityHashCode(this);
  }

  // mutable structures as key in a hash map :(
  @Override
  public boolean equals(Object obj) {
    return obj instanceof Constraint other && other == this;
  }

  @Override
  public int hashCode() {
    return hash;
  }
}
