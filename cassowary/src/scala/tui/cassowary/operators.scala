package tui.cassowary

import scala.collection.mutable

object operators {
  implicit class DoubleOps(private val self: Double) extends AnyVal {
    def |(rhs: WeightedRelation): PartialConstraint = PartialConstraint(Expression.from_constant(self), rhs)

    def +(rhs: Variable) = new Expression(mutable.ArrayBuffer(Term(rhs, 1.0)), self)

    def +(rhs: Term): Expression = Expression(mutable.ArrayBuffer(rhs), self)

    def +(rhs: Expression): Expression = {
      rhs.constant = rhs.constant + self
      rhs
    }

    def -(rhs: Variable): Expression = Expression(mutable.ArrayBuffer(Term(rhs, -1.0)), self)

    def -(rhs: Term): Expression = Expression(mutable.ArrayBuffer(!rhs), self)

    def -(rhs: Expression): Expression = {
      rhs.negate()
      rhs.constant += self
      rhs
    }

    def *(rhs: Variable): Term = Term(rhs, self)

    def *(rhs: Term): Term = {
      rhs.coefficient = rhs.coefficient * self
      rhs
    }

    def *(rhs: Expression): Expression = {
      rhs.constant = rhs.constant * self
      var i = 0
      while (i < rhs.terms.length) {
        rhs.terms(i) = rhs.terms(i) * self
        i += 1
      }
      rhs
    }
  }

  implicit class VariableOps(private val self: Variable) extends AnyVal {
    def |(rhs: WeightedRelation): PartialConstraint = PartialConstraint(Expression.from_term(Term(self, coefficient = 1.0)), rhs)

    def +(rhs: Double) = new Expression(mutable.ArrayBuffer(Term(self, 1.0)), rhs)

    def +(rhs: Variable): Expression = Expression(Array(Term(self, 1.0), Term(rhs, 1.0)), 0.0)

    def +(rhs: Term): Expression = Expression(Array(Term(self, 1.0), rhs), 0.0)

    def +(rhs: Expression): Expression = {
      rhs.terms += Term(self, 1.0)
      rhs
    }

    def unary_! : Term = Term(self, -1.0)

    def -(rhs: Double): Expression = Expression(mutable.ArrayBuffer(Term(self, 1.0)), -rhs)

    def -(rhs: Variable): Expression = Expression(mutable.ArrayBuffer(Term(self, 1.0), Term(rhs, -1.0)), 0.0)

    def -(rhs: Term): Expression = Expression(mutable.ArrayBuffer(Term(self, 1.0), !rhs), 0.0)

    def -(rhs: Expression): Expression = {
      rhs.negate()
      rhs.terms.addOne(Term(self, 1.0))
      rhs
    }
    def *(rhs: Double): Term = Term(self, rhs)
    def /(rhs: Double): Term = Term(self, 1.0 / rhs)
  }

  implicit class TermOps(private val self: Term) extends AnyVal {
    def |(rhs: WeightedRelation): PartialConstraint = PartialConstraint(Expression.from_term(self), rhs)

    def +(rhs: Variable): Expression = Expression(Array(self, Term(rhs, 1.0)), 0.0)

    def +(rhs: Double): Expression = Expression(mutable.ArrayBuffer(self), rhs)

    def +(rhs: Term): Expression = Expression(mutable.ArrayBuffer(self, rhs), 0.0)

    def +(rhs: Expression): Expression = {
      rhs.terms.addOne(self)
      rhs
    }

    def unary_! : Term = {
      self.coefficient = -self.coefficient
      self
    }

    def -(rhs: Variable): Expression = Expression(mutable.ArrayBuffer(self, Term(rhs, -1.0)), 0.0)

    def -(rhs: Double): Expression = Expression(mutable.ArrayBuffer(self), -rhs)

    def -(rhs: Term): Expression = Expression(mutable.ArrayBuffer(self, !rhs), 0.0)

    def -(rhs: Expression): Expression = {
      rhs.negate()
      rhs.terms.addOne(self)
      rhs
    }
    def *(rhs: Double): Term = {
      self.coefficient = self.coefficient * rhs
      self
    }
    def /(rhs: Double): Term = {
      self.coefficient = self.coefficient / rhs
      self
    }
  }

  implicit class ExpressionOps(private val self: Expression) extends AnyVal {
    def |(rhs: WeightedRelation): PartialConstraint = PartialConstraint(self, rhs)

    def +(rhs: Variable): Expression = {
      self.terms.addOne(Term(rhs, 1.0))
      self
    }

    def +(self: Expression, rhs: Term): Expression = {
      self.terms.addOne(rhs)
      self
    }

    def +(rhs: Double): Expression = {
      self.constant = self.constant + rhs
      self
    }

    def +(rhs: Expression): Expression = {
      self.terms.addAll(rhs.terms)
      self.constant = rhs.constant
      self
    }

    def unary_! : Expression = {
      self.negate()
      self
    }

    def -(rhs: Double): Expression = {
      self.constant = self.constant - rhs
      self
    }

    def -(rhs: Variable): Expression = {
      self.terms.addOne(Term(rhs, -1.0))
      self
    }

    def -(rhs: Term): Expression = {
      self.terms.addOne(!rhs)
      self
    }

    def -(rhs: Expression): Expression = {
      rhs.negate()
      self.terms.addAll(rhs.terms)
      self.constant += rhs.constant
      self
    }
    def *(rhs: Double): Expression = {
      self.constant = self.constant * rhs
      var i = 0
      while (i < self.terms.length) {
        self.terms(i) = self.terms(i) * rhs
        i += 1
      }
      self
    }
    def /(rhs: Double): Expression = {
      self.constant = self.constant / rhs
      var i = 0
      while (i < self.terms.length) {
        self.terms(i) = self.terms(i) / rhs
        i += 1
      }
      self
    }
  }

  implicit class PartialConstraintOps(private val self: PartialConstraint) extends AnyVal {
    def |(rhs: Double): Constraint = {
      val (op, s) = self.wr.into
      Constraint(self.e - rhs, s, op)
    }
    def |(rhs: Variable): Constraint = {
      val (op, s) = self.wr.into
      Constraint(self.e - rhs, s, op)
    }
    def |(rhs: Term): Constraint = {
      val (op, s) = self.wr.into
      Constraint(self.e - rhs, s, op)
    }
    def |(rhs: Expression): Constraint = {
      val (op, s) = self.wr.into
      Constraint(self.e - rhs, s, op)
    }
  }
}
