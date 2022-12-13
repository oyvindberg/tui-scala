package tui.cassowary

import tui.cassowary.operators._

import scala.collection.mutable

//! This crate contains an implementation of the Cassowary constraint solving algorithm, based upon the work by
//! G.J. Badros et al. in 2001. This algorithm is designed primarily for use constraining elements in user interfaces.
//! Constraints are linear combinations of the problem variables. The notable features of Cassowary that make it
//! ideal for user interfaces are that it is incremental (i.e. you can add and remove constraints at runtime
//! and it will perform the minimum work to update the result) and that the constraints can be violated if
//! necessary,
//! with the order in which they are violated specified by setting a "strength" for each constraint.
//! This allows the solution to gracefully degrade, which is useful for when a
//! user interface needs to compromise on its constraints in order to still be able to display something.
//!
//! ## Constraint syntax
//!
//! This crate aims to provide syntax for describing linear constraints as naturally as possible, within
//! the limitations of Rust's type system. Generally you can write constraints as you would naturally, however
//! the operator symbol (for greater-than, less-than, equals) is replaced with an instance of the
//! `WeightedRelation` enum wrapped in "pipe brackets".
//!
//! For example, for the constraint
//! `(a + b) * 2 + c >= d + 1` with strength `s`, the code to use is
//!
//! ```ignore
//! (a + b) * 2.0 + c |GE(s)| d + 1.0
//! ```
//!
//! # A simple example
//!
//! Imagine a layout consisting of two elements laid out horizontally. For small window widths the elements
//! should compress to fit, but if there is enough space they should display at their preferred widths. The
//! first element will align to the left, and the second to the right. For  this example we will ignore
//! vertical layout.
//!
//! First we need to include the relevant parts of `cassowary`:
//!
//! ```
//! use cassowary::{ Solver, Variable };
//! use cassowary::WeightedRelation::*;
//! use cassowary::strength::{ WEAK, MEDIUM, STRONG, REQUIRED };
//! ```
//!
//! And we'll construct some conveniences for pretty printing (which should hopefully be self-explanatory):
//!
//! ```ignore
//! use std::collections::HashMap;
//! let mut names = HashMap::new();
//! fn print_changes(names: &HashMap<Variable, &'static str>, changes: &[(Variable, f64)]) {
//!     println!("Changes:");
//!     for &(ref var, ref val) in changes {
//!         println!("{}: {}", names[var], val);
//!     }
//! }
//! ```
//!
//! Let's define the variables required - the left and right edges of the elements, and the width of the window.
//!
//! ```ignore
//! let window_width = Variable::new();
//! names.insert(window_width, "window_width");
//!
//! struct Element {
//!     left: Variable,
//!     right: Variable
//! }
//! let box1 = Element {
//!     left: Variable::new(),
//!     right: Variable::new()
//! };
//! names.insert(box1.left, "box1.left");
//! names.insert(box1.right, "box1.right");
//!
//! let box2 = Element {
//!     left: Variable::new(),
//!     right: Variable::new()
//! };
//! names.insert(box2.left, "box2.left");
//! names.insert(box2.right, "box2.right");
//! ```
//!
//! Now to set up the solver and constraints.
//!
//! ```ignore
//! let mut solver = Solver::new();
//! solver.add_constraints(&[window_width |GE(REQUIRED)| 0.0, // positive window width
//!                          box1.left |EQ(REQUIRED)| 0.0, // left align
//!                          box2.right |EQ(REQUIRED)| window_width, // right align
//!                          box2.left |GE(REQUIRED)| box1.right, // no overlap
//!                          // positive widths
//!                          box1.left |LE(REQUIRED)| box1.right,
//!                          box2.left |LE(REQUIRED)| box2.right,
//!                          // preferred widths:
//!                          box1.right - box1.left |EQ(WEAK)| 50.0,
//!                          box2.right - box2.left |EQ(WEAK)| 100.0]).unwrap();
//! ```
//!
//! The window width is currently free to take any positive value. Let's constrain it to a particular value.
//! Since for this example we will repeatedly change the window width, it is most efficient to use an
//! "edit variable", instead of repeatedly removing and adding constraints (note that for efficiency
//! reasons we cannot edit a normal constraint that has been added to the solver).
//!
//! ```ignore
//! solver.add_edit_variable(window_width, STRONG).unwrap();
//! solver.suggest_value(window_width, 300.0).unwrap();
//! ```
//!
//! This value of 300 is enough to fit both boxes in with room to spare, so let's check that this is the case.
//! We can fetch a list of changes to the values of variables in the solver. Using the pretty printer defined
//! earlier we can see what values our variables now hold.
//!
//! ```ignore
//! print_changes(&names, solver.fetch_changes());
//! ```
//!
//! This should print (in a possibly different order):
//!
//! ```ignore
//! Changes:
//! window_width: 300
//! box1.right: 50
//! box2.left: 200
//! box2.right: 300
//! ```
//!
//! Note that the value of `box1.left` is not mentioned. This is because `solver.fetch_changes` only lists
//! *changes* to variables, and since each variable starts in the solver with a value of zero, any values that
//! have not changed from zero will not be reported.
//!
//! Now let's try compressing the window so that the boxes can't take up their preferred widths.
//!
//! ```ignore
//! solver.suggest_value(window_width, 75.0);
//! print_changes(&names, solver.fetch_changes);
//! ```
//!
//! Now the solver can't satisfy all of the constraints. It will pick at least one of the weakest constraints to
//! violate. In this case it will be one or both of the preferred widths. For efficiency reasons this is picked
//! nondeterministically, so there are two possible results. This could be
//!
//! ```ignore
//! Changes:
//! window_width: 75
//! box1.right: 0
//! box2.left: 0
//! box2.right: 75
//! ```
//!
//! or
//!
//! ```ignore
//! Changes:
//! window_width: 75
//! box2.left: 50
//! box2.right: 75
//! ```
//!
//! Due to the nature of the algorithm, "in-between" solutions, although just as valid, are not picked.
//!
//! In a user interface this is not likely a result we would prefer. The solution is to add another constraint
//! to control the behaviour when the preferred widths cannot both be satisfied. In this example we are going
//! to constrain the boxes to try to maintain a ratio between their widths.
//!
//! ```
//! # use cassowary::{ Solver, Variable };
//! # use cassowary::WeightedRelation::*;
//! # use cassowary::strength::{ WEAK, MEDIUM, STRONG, REQUIRED };
//! #
//! # use std::collections::HashMap;
//! # let mut names = HashMap::new();
//! # fn print_changes(names: &HashMap<Variable, &'static str>, changes: &[(Variable, f64)]) {
//! #     println!("Changes:");
//! #     for &(ref var, ref val) in changes {
//! #         println!("{}: {}", names[var], val);
//! #     }
//! # }
//! #
//! # let window_width = Variable::new();
//! # names.insert(window_width, "window_width");
//! # struct Element {
//! #    left: Variable,
//! #    right: Variable
//! # }
//! # let box1 = Element {
//! #     left: Variable::new(),
//! #     right: Variable::new()
//! # };
//! # names.insert(box1.left, "box1.left");
//! # names.insert(box1.right, "box1.right");
//! # let box2 = Element {
//! #     left: Variable::new(),
//! #     right: Variable::new()
//! # };
//! # names.insert(box2.left, "box2.left");
//! # names.insert(box2.right, "box2.right");
//! # let mut solver = Solver::new();
//! # solver.add_constraints(&[window_width |GE(REQUIRED)| 0.0, // positive window width
//! #                          box1.left |EQ(REQUIRED)| 0.0, // left align
//! #                          box2.right |EQ(REQUIRED)| window_width, // right align
//! #                          box2.left |GE(REQUIRED)| box1.right, // no overlap
//! #                          // positive widths
//! #                          box1.left |LE(REQUIRED)| box1.right,
//! #                          box2.left |LE(REQUIRED)| box2.right,
//! #                          // preferred widths:
//! #                          box1.right - box1.left |EQ(WEAK)| 50.0,
//! #                          box2.right - box2.left |EQ(WEAK)| 100.0]).unwrap();
//! # solver.add_edit_variable(window_width, STRONG).unwrap();
//! # solver.suggest_value(window_width, 300.0).unwrap();
//! # print_changes(&names, solver.fetch_changes());
//! # solver.suggest_value(window_width, 75.0);
//! # print_changes(&names, solver.fetch_changes());
//! solver.add_constraint(
//!     (box1.right - box1.left) / 50.0 |EQ(MEDIUM)| (box2.right - box2.left) / 100.0
//!     ).unwrap();
//! print_changes(&names, solver.fetch_changes());
//! ```
//!
//! Now the result gives values that maintain the ratio between the sizes of the two boxes:
//!
//! ```ignore
//! Changes:
//! box1.right: 25
//! box2.left: 25
//! ```
//!
//! This example may have appeared somewhat contrived, but hopefully it shows the power of the cassowary
//! algorithm for laying out user interfaces.
//!
//! One thing that this example exposes is that this crate is a rather low level library. It does not have
//! any inherent knowledge of user interfaces, directions or boxes. Thus for use in a user interface this
//! crate should ideally be wrapped by a higher level API, which is outside the scope of this crate.

/// Identifies a variable for the constraint solver.
/// Each new variable is unique in the view of the solver, but copying or cloning the variable produces
/// a copy of the same variable.
case class Variable private (value: Int)

object Variable {
  def force(value: Int) = new Variable(value)
  /// Produces a new unique variable for use in constraint solving.
  def apply(): Variable = new Variable(VARIABLE_ID.getAndIncrement())
  private val VARIABLE_ID = new java.util.concurrent.atomic.AtomicInteger(0)
}

/// A variable and a coefficient to multiply that variable by. This is a sub-expression in
/// a constraint equation.
case class Term(
    variable: Variable,
    var coefficient: Double
)

/// An expression that can be the left hand or right hand side of a constraint equation.
/// It is a linear combination of variables, i.e. a sum of variables weighted by coefficients, plus an optional constant.
case class Expression(
    terms: mutable.ArrayBuffer[Term],
    var constant: Double
) {
  // shallow clone. investigate more if needed
  override def clone(): Expression = Expression(terms, constant)

  /// Mutates this expression by multiplying it by minus one.
  def negate(): Unit = {
    constant = -constant
    var i = 0
    while (i < terms.length) {
      terms(i) = !terms(i)
      i += 1
    }
  }
}

object Expression {
  /// Constructs an expression of the form _n_, where n is a constant real number, not a variable.
  def from_constant(v: Double): Expression =
    Expression(
      terms = mutable.ArrayBuffer.empty[Term],
      constant = v
    )
  /// Constructs an expression from a single term. Forms an expression of the form _n x_
  /// where n is the coefficient, and x is the variable.
  def from_term(term: Term): Expression =
    Expression(
      terms = mutable.ArrayBuffer.from(List(term)),
      constant = 0.0
    )
  /// General constructor. Each `Term` in `terms` is part of the sum forming the expression, as well as `constant`.
  def apply(terms: Array[Term], constant: Double): Expression =
    Expression(terms = mutable.ArrayBuffer.from(terms), constant = constant)
}

/// Contains useful constants and functions for producing strengths for use in the constraint solver.
/// Each constraint added to the solver has an associated strength specifying the precedence the solver should
/// impose when choosing which constraints to enforce. It will try to enforce all constraints, but if that
/// is impossible the lowest strength constraints are the first to be violated.
///
/// Strengths are simply real numbers. The strongest legal strength is 1,001,001,000.0. The weakest is 0.0.
/// For convenience constants are declared for commonly used strengths. These are `REQUIRED`, `STRONG`,
/// `MEDIUM` and `WEAK`. Feel free to multiply these by other values to get intermediate strengths.
/// Note that the solver will clip given strengths to the legal range.
///
/// `REQUIRED` signifies a constraint that cannot be violated under any circumstance. Use this special strength
/// sparingly, as the solver will fail completely if it find that not all of the `REQUIRED` constraints
/// can be satisfied. The other strengths represent fallible constraints. These should be the most
/// commonly used strenghts for use cases where violating a constraint is acceptable or even desired.
///
/// The solver will try to get as close to satisfying the constraints it violates as possible, strongest first.
/// This behaviour can be used (for example) to provide a "default" value for a variable should no other
/// stronger constraints be put upon it.
case class Strength(value: Double) {
  def *(rhs: Double): Strength = Strength(value * rhs)
}

object Strength {
  implicit val ordering: Ordering[Strength] = Ordering.by(_.value)
  /// Create a constraint as a linear combination of STRONG, MEDIUM and WEAK strengths, corresponding to `a`
  /// `b` and `c` respectively. The result is further multiplied by `w`.
  def create(a: Double, b: Double, c: Double, w: Double): Strength =
    Strength(
      (a * w).max(0.0).min(1000.0) * 1_000_000.0 +
        (b * w).max(0.0).min(1000.0) * 1000.0 +
        (c * w).max(0.0).min(1000.0)
    )

  val REQUIRED: Strength = Strength(1_001_001_000.0)
  val STRONG: Strength = Strength(1_000_000.0)
  val MEDIUM: Strength = Strength(1_000.0)
  val WEAK: Strength = Strength(1.0)

  /// Clips a strength value to the legal range
  def clip(s: Strength): Strength =
    Strength(s.value.min(REQUIRED.value).max(0.0))
}

/// The possible relations that a constraint can specify.
sealed trait RelationalOperator {
  override def toString: String = this match {
    case RelationalOperator.LessOrEqual    => "<="
    case RelationalOperator.Equal          => "=="
    case RelationalOperator.GreaterOrEqual => ">="
  }
}
object RelationalOperator {
  /// `<=`
  case object LessOrEqual extends RelationalOperator
  /// `==`
  case object Equal extends RelationalOperator
  /// `>=`
  case object GreaterOrEqual extends RelationalOperator
}

/// A constraint, consisting of an equation governed by an expression and a relational operator,
/// and an associated strength.
case class Constraint(
    /// The expression of the left hand side of the constraint equation.
    expression: Expression,
    /// The strength of the constraint that the solver will use.
    strength: Strength,
    /// The relational operator governing the constraint.
    op: RelationalOperator
) {
  // mutable structures as key in a hash map :(
  override def equals(obj: Any): Boolean = obj match {
    case x: Constraint => x eq this
    case _             => false
  }

  override val hashCode: Int = System.identityHashCode(this)
}

/// This is part of the syntactic sugar used for specifying constraints. This enum should be used as part of a
/// constraint expression. See the module documentation for more information.
sealed trait WeightedRelation {
  def into: (RelationalOperator, Strength) = this match {
    case WeightedRelation.EQ(s) => (RelationalOperator.Equal, s)
    case WeightedRelation.LE(s) => (RelationalOperator.LessOrEqual, s)
    case WeightedRelation.GE(s) => (RelationalOperator.GreaterOrEqual, s)
  }
}

object WeightedRelation {
  /// `==`
  case class EQ(value: Strength) extends WeightedRelation

  /// `<=`
  case class LE(value: Strength) extends WeightedRelation

  /// `>=`
  case class GE(value: Strength) extends WeightedRelation
}

/// This is an intermediate type used in the syntactic sugar for specifying constraints. You should not use it
/// directly.
case class PartialConstraint(e: Expression, wr: WeightedRelation)

sealed trait SymbolType
object SymbolType {
  case object Invalid extends SymbolType
  case object External extends SymbolType
  case object Slack extends SymbolType
  case object Error extends SymbolType
  case object Dummy extends SymbolType
}

case class Symbol(size: Int, tpe: SymbolType)

object Symbol {
  val invalid: Symbol = Symbol(0, SymbolType.Invalid)
}

object near_zero {
  val EPS: Double = 1e-8

  def apply(value: Double): Boolean =
    if (value < 0.0) { -value < EPS }
    else { value < EPS }
}

case class Row(
    var cells: mutable.Map[Symbol, Double],
    var constant: Double
) {
  // shallow clone. investigate more if needed
  override def clone(): Row = Row(cells, constant)

  def add(v: Double): Double = {
    this.constant += v
    this.constant
  }

  def insert_symbol(s: Symbol, coefficient: Double): Unit =
    cells.get(s) match {
      case None =>
        if (!near_zero(coefficient)) {
          cells(s) = coefficient
        }
      case Some(oldCoefficient) =>
        val newCoefficient = coefficient + oldCoefficient
        if (near_zero(newCoefficient)) {
          cells.remove(s)
          ()
        } else {
          cells(s) = newCoefficient
        }
    }

  def insert_row(other: Row, coefficient: Double): Boolean = {
    val constant_diff = other.constant * coefficient
    constant += constant_diff
    other.cells.foreach { case (s, v) =>
      insert_symbol(s, v * coefficient);
    }
    constant_diff != 0.0
  }

  def remove(s: Symbol): Option[Double] =
    cells.remove(s)

  def reverse_sign(): Unit = {
    constant = -constant
    cells = cells.map { case (s, v) => (s, -v) }
  }

  def solve_for_symbol(s: Symbol): Unit = {
    val coeff = -1.0 / cells(s)
    cells.remove(s)
    constant *= coeff
    cells = cells.map { case (s, v) => (s, v * coeff) }
  }

  def solve_for_symbols(lhs: Symbol, rhs: Symbol): Unit = {
    insert_symbol(lhs, -1.0)
    solve_for_symbol(rhs)
  }

  def coefficient_for(s: Symbol): Double =
    cells.getOrElse(s, 0.0)

  def substitute(s: Symbol, row: Row): Boolean =
    cells.get(s) match {
      case Some(coeff) =>
        cells.remove(s)
        insert_row(row, coeff)
      case None => false
    }
}

object Row {
  def apply(constant: Double): Row =
    Row(
      cells = mutable.Map.empty,
      constant = constant
    )
}

/// The possible error conditions that `Solver::add_constraint` can fail with.
sealed trait AddConstraintError
object AddConstraintError {
  /// The constraint specified has already been added to the solver.
  case object DuplicateConstraint extends AddConstraintError
  /// The constraint is required, but it is unsatisfiable in conjunction with the existing constraints.
  case object UnsatisfiableConstraint extends AddConstraintError
  /// The solver entered an invalid state. If this occurs please report the issue. This variant specifies
  /// additional details as a string.
  case class InternalSolverError(str: String) extends AddConstraintError
}

/// The possible error conditions that `Solver::remove_constraint` can fail with.
sealed trait RemoveConstraintError
object RemoveConstraintError {
  /// The constraint specified was not already in the solver, so cannot be removed.
  case object UnknownConstraint extends RemoveConstraintError
  /// The solver entered an invalid state. If this occurs please report the issue. This variant specifies
  /// additional details as a string.
  case class InternalSolverError(str: String) extends RemoveConstraintError
}

/// The possible error conditions that `Solver::add_edit_variable` can fail with.
sealed trait AddEditVariableError
object AddEditVariableError {
  /// The specified variable is already marked as an edit variable in the solver.
  case object DuplicateEditVariable extends AddEditVariableError
  /// The specified strength was `REQUIRED`. This is illegal for edit variable strengths.
  case object BadRequiredStrength extends AddEditVariableError
}

/// The possible error conditions that `Solver::remove_edit_variable` can fail with.
sealed trait RemoveEditVariableError
object RemoveEditVariableError {
  /// The specified variable was not an edit variable in the solver, so cannot be removed.
  case object UnknownEditVariable extends RemoveEditVariableError
  /// The solver entered an invalid state. If this occurs please report the issue. This variant specifies
  /// additional details as a string.
  case class InternalSolverError(str: String) extends RemoveEditVariableError
}

/// The possible error conditions that `Solver::suggest_value` can fail with.
sealed trait SuggestValueError
object SuggestValueError {
  /// The specified variable was not an edit variable in the solver, so cannot have its value suggested.
  case object UnknownEditVariable extends SuggestValueError
  /// The solver entered an invalid state. If this occurs please report the issue. This variant specifies
  /// additional details as a string.
  case class InternalSolverError(str: String) extends SuggestValueError
}

case class InternalSolverError(str: String);
