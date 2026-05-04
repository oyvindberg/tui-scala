package jatatui.core.layout.solver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/// An expression that can be the left hand or right hand side of a constraint equation.
/// It is a linear combination of variables, i.e. a sum of variables weighted by coefficients, plus an optional constant.
public final class Expression {
  public final List<Term> terms;
  public double constant;

  public Expression(List<Term> terms, double constant) {
    this.terms = terms;
    this.constant = constant;
  }

  // shallow clone. investigate more if needed
  @Override
  public Expression clone() {
    return new Expression(terms, constant);
  }

  /// Mutates this expression by multiplying it by minus one.
  public void negate() {
    constant = -constant;
    int i = 0;
    while (i < terms.size()) {
      terms.set(i, KasuariOps.negate(terms.get(i)));
      i += 1;
    }
  }

  /// Constructs an expression of the form _n_, where n is a constant real number, not a variable.
  public static Expression fromConstant(double v) {
    return new Expression(new ArrayList<>(), v);
  }

  /// Constructs an expression from a single term. Forms an expression of the form _n x_
  /// where n is the coefficient, and x is the variable.
  public static Expression fromTerm(Term term) {
    ArrayList<Term> ts = new ArrayList<>();
    ts.add(term);
    return new Expression(ts, 0.0);
  }

  /// General constructor. Each `Term` in `terms` is part of the sum forming the expression, as well as `constant`.
  public static Expression of(Term[] terms, double constant) {
    return new Expression(new ArrayList<>(Arrays.asList(terms)), constant);
  }
}
