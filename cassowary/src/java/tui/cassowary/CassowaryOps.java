package tui.cassowary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/// Replacement for the Scala `operators` package object. Provides static helpers for building
/// expressions and constraints — the equivalents of the `+`, `-`, `*`, `/`, `unary_!` and `|`
/// operators in the original Scala API.
public final class CassowaryOps {
  private CassowaryOps() {}

  // ----- DoubleOps -----

  public static PartialConstraint partial(double self, WeightedRelation rhs) {
    return new PartialConstraint(Expression.fromConstant(self), rhs);
  }

  public static Expression add(double self, Variable rhs) {
    ArrayList<Term> ts = new ArrayList<>();
    ts.add(new Term(rhs, 1.0));
    return new Expression(ts, self);
  }

  public static Expression add(double self, Term rhs) {
    ArrayList<Term> ts = new ArrayList<>();
    ts.add(rhs);
    return new Expression(ts, self);
  }

  public static Expression add(double self, Expression rhs) {
    rhs.constant = rhs.constant + self;
    return rhs;
  }

  public static Expression sub(double self, Variable rhs) {
    ArrayList<Term> ts = new ArrayList<>();
    ts.add(new Term(rhs, -1.0));
    return new Expression(ts, self);
  }

  public static Expression sub(double self, Term rhs) {
    ArrayList<Term> ts = new ArrayList<>();
    ts.add(negate(rhs));
    return new Expression(ts, self);
  }

  public static Expression sub(double self, Expression rhs) {
    rhs.negate();
    rhs.constant += self;
    return rhs;
  }

  public static Term mul(double self, Variable rhs) {
    return new Term(rhs, self);
  }

  public static Term mul(double self, Term rhs) {
    rhs.coefficient = rhs.coefficient * self;
    return rhs;
  }

  public static Expression mul(double self, Expression rhs) {
    rhs.constant = rhs.constant * self;
    int i = 0;
    while (i < rhs.terms.size()) {
      rhs.terms.set(i, mul(rhs.terms.get(i), self));
      i += 1;
    }
    return rhs;
  }

  // ----- VariableOps -----

  public static PartialConstraint partial(Variable self, WeightedRelation rhs) {
    return new PartialConstraint(Expression.fromTerm(new Term(self, 1.0)), rhs);
  }

  public static Expression add(Variable self, double rhs) {
    ArrayList<Term> ts = new ArrayList<>();
    ts.add(new Term(self, 1.0));
    return new Expression(ts, rhs);
  }

  public static Expression add(Variable self, Variable rhs) {
    return Expression.of(new Term[] {new Term(self, 1.0), new Term(rhs, 1.0)}, 0.0);
  }

  public static Expression add(Variable self, Term rhs) {
    return Expression.of(new Term[] {new Term(self, 1.0), rhs}, 0.0);
  }

  public static Expression add(Variable self, Expression rhs) {
    rhs.terms.add(new Term(self, 1.0));
    return rhs;
  }

  public static Term negate(Variable self) {
    return new Term(self, -1.0);
  }

  public static Expression sub(Variable self, double rhs) {
    ArrayList<Term> ts = new ArrayList<>();
    ts.add(new Term(self, 1.0));
    return new Expression(ts, -rhs);
  }

  public static Expression sub(Variable self, Variable rhs) {
    ArrayList<Term> ts = new ArrayList<>();
    ts.add(new Term(self, 1.0));
    ts.add(new Term(rhs, -1.0));
    return new Expression(ts, 0.0);
  }

  public static Expression sub(Variable self, Term rhs) {
    ArrayList<Term> ts = new ArrayList<>();
    ts.add(new Term(self, 1.0));
    ts.add(negate(rhs));
    return new Expression(ts, 0.0);
  }

  public static Expression sub(Variable self, Expression rhs) {
    rhs.negate();
    rhs.terms.add(new Term(self, 1.0));
    return rhs;
  }

  public static Term mul(Variable self, double rhs) {
    return new Term(self, rhs);
  }

  public static Term div(Variable self, double rhs) {
    return new Term(self, 1.0 / rhs);
  }

  // ----- TermOps -----

  public static PartialConstraint partial(Term self, WeightedRelation rhs) {
    return new PartialConstraint(Expression.fromTerm(self), rhs);
  }

  public static Expression add(Term self, Variable rhs) {
    return Expression.of(new Term[] {self, new Term(rhs, 1.0)}, 0.0);
  }

  public static Expression add(Term self, double rhs) {
    ArrayList<Term> ts = new ArrayList<>();
    ts.add(self);
    return new Expression(ts, rhs);
  }

  public static Expression add(Term self, Term rhs) {
    ArrayList<Term> ts = new ArrayList<>();
    ts.add(self);
    ts.add(rhs);
    return new Expression(ts, 0.0);
  }

  public static Expression add(Term self, Expression rhs) {
    rhs.terms.add(self);
    return rhs;
  }

  public static Term negate(Term self) {
    self.coefficient = -self.coefficient;
    return self;
  }

  public static Expression sub(Term self, Variable rhs) {
    ArrayList<Term> ts = new ArrayList<>();
    ts.add(self);
    ts.add(new Term(rhs, -1.0));
    return new Expression(ts, 0.0);
  }

  public static Expression sub(Term self, double rhs) {
    ArrayList<Term> ts = new ArrayList<>();
    ts.add(self);
    return new Expression(ts, -rhs);
  }

  public static Expression sub(Term self, Term rhs) {
    ArrayList<Term> ts = new ArrayList<>();
    ts.add(self);
    ts.add(negate(rhs));
    return new Expression(ts, 0.0);
  }

  public static Expression sub(Term self, Expression rhs) {
    rhs.negate();
    rhs.terms.add(self);
    return rhs;
  }

  public static Term mul(Term self, double rhs) {
    self.coefficient = self.coefficient * rhs;
    return self;
  }

  public static Term div(Term self, double rhs) {
    self.coefficient = self.coefficient / rhs;
    return self;
  }

  // ----- ExpressionOps -----

  public static PartialConstraint partial(Expression self, WeightedRelation rhs) {
    return new PartialConstraint(self, rhs);
  }

  public static Expression add(Expression self, Variable rhs) {
    self.terms.add(new Term(rhs, 1.0));
    return self;
  }

  public static Expression add(Expression self, double rhs) {
    self.constant = self.constant + rhs;
    return self;
  }

  public static Expression add(Expression self, Expression rhs) {
    self.terms.addAll(rhs.terms);
    self.constant = rhs.constant;
    return self;
  }

  public static Expression negate(Expression self) {
    self.negate();
    return self;
  }

  public static Expression sub(Expression self, double rhs) {
    self.constant = self.constant - rhs;
    return self;
  }

  public static Expression sub(Expression self, Variable rhs) {
    self.terms.add(new Term(rhs, -1.0));
    return self;
  }

  public static Expression sub(Expression self, Term rhs) {
    self.terms.add(negate(rhs));
    return self;
  }

  public static Expression sub(Expression self, Expression rhs) {
    rhs.negate();
    self.terms.addAll(rhs.terms);
    self.constant += rhs.constant;
    return self;
  }

  public static Expression mul(Expression self, double rhs) {
    self.constant = self.constant * rhs;
    int i = 0;
    while (i < self.terms.size()) {
      self.terms.set(i, mul(self.terms.get(i), rhs));
      i += 1;
    }
    return self;
  }

  public static Expression div(Expression self, double rhs) {
    self.constant = self.constant / rhs;
    int i = 0;
    while (i < self.terms.size()) {
      self.terms.set(i, div(self.terms.get(i), rhs));
      i += 1;
    }
    return self;
  }

  // ----- PartialConstraintOps -----

  public static Constraint constrain(PartialConstraint self, double rhs) {
    WeightedRelation.OpAndStrength s = self.wr().into();
    return new Constraint(sub(self.e(), rhs), s.strength(), s.op());
  }

  public static Constraint constrain(PartialConstraint self, Variable rhs) {
    WeightedRelation.OpAndStrength s = self.wr().into();
    return new Constraint(sub(self.e(), rhs), s.strength(), s.op());
  }

  public static Constraint constrain(PartialConstraint self, Term rhs) {
    WeightedRelation.OpAndStrength s = self.wr().into();
    return new Constraint(sub(self.e(), rhs), s.strength(), s.op());
  }

  public static Constraint constrain(PartialConstraint self, Expression rhs) {
    WeightedRelation.OpAndStrength s = self.wr().into();
    return new Constraint(sub(self.e(), rhs), s.strength(), s.op());
  }

  // ----- High-level constraint builders (lhs | wr | rhs) -----

  public static Constraint constraint(Variable lhs, WeightedRelation wr, double rhs) {
    return constrain(partial(lhs, wr), rhs);
  }

  public static Constraint constraint(Variable lhs, WeightedRelation wr, Variable rhs) {
    return constrain(partial(lhs, wr), rhs);
  }

  public static Constraint constraint(Variable lhs, WeightedRelation wr, Term rhs) {
    return constrain(partial(lhs, wr), rhs);
  }

  public static Constraint constraint(Variable lhs, WeightedRelation wr, Expression rhs) {
    return constrain(partial(lhs, wr), rhs);
  }

  public static Constraint constraint(Expression lhs, WeightedRelation wr, double rhs) {
    return constrain(partial(lhs, wr), rhs);
  }

  public static Constraint constraint(Expression lhs, WeightedRelation wr, Variable rhs) {
    return constrain(partial(lhs, wr), rhs);
  }

  public static Constraint constraint(Expression lhs, WeightedRelation wr, Expression rhs) {
    return constrain(partial(lhs, wr), rhs);
  }

  public static Constraint constraint(Term lhs, WeightedRelation wr, double rhs) {
    return constrain(partial(lhs, wr), rhs);
  }

  public static Constraint constraint(Term lhs, WeightedRelation wr, Variable rhs) {
    return constrain(partial(lhs, wr), rhs);
  }

  public static Constraint constraint(double lhs, WeightedRelation wr, double rhs) {
    return constrain(partial(lhs, wr), rhs);
  }
}
