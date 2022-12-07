package tui.cassowary

import scala.Ordering.Implicits._

case class Tag(marker: Symbol, other: Symbol)

case class EditInfo(
    tag: Tag,
    constraint: Constraint,
    var constant: Double
)

import scala.collection.mutable

case class VarData(var double: Double, symbol: Symbol, var count: Int)

/// A constraint solver using the Cassowary algorithm. For proper usage please see the top level crate documentation.
case class Solver(
    cns: mutable.Map[Constraint, Tag] = mutable.Map.empty,
    var_data: mutable.Map[Variable, VarData] = mutable.Map.empty,
    var_for_symbol: mutable.Map[Symbol, Variable] = mutable.Map.empty,
    public_changes: mutable.Stack[(Variable, Double)] = mutable.Stack.empty,
    changed: mutable.Set[Variable] = mutable.Set.empty,
    var should_clear_changes: Boolean = false,
    rows: mutable.Map[Symbol, Row] = mutable.Map.empty,
    edits: mutable.Map[Variable, EditInfo] = mutable.Map.empty,
    infeasible_rows: mutable.Stack[Symbol] = mutable.Stack.empty, // never contains external symbols
    var objective: Row = Row(0.0),
    var artificial: Option[Row] = None,
    var id_tick: Int = 1
) {
  def add_constraints(constraints: Array[Constraint]): Either[AddConstraintError, Unit] = {
    var i = 0
    while (i < constraints.length) {
      val constraint = constraints(i)
      add_constraint(constraint) match {
        case left @ Left(_) => return left
        case Right(())      => ()
      }
      i += 1
    }
    Right(())
  }

  /// Add a constraint to the solver.
  def add_constraint(constraint: Constraint): Either[AddConstraintError, Unit] = {
    if (cns.contains(constraint)) {
      return Left(AddConstraintError.DuplicateConstraint);
    }

    // Creating a row causes symbols to reserved for the variables
    // in the constraint. If this method exits with an exception,
    // then its possible those variables will linger in the var map.
    // Since its likely that those variables will be used in other
    // constraints and since exceptional conditions are uncommon,
    // i'm not too worried about aggressive cleanup of the var map.
    val (row, tag) = create_row(constraint);
    var subject = choose_subject(row, tag);

    // If chooseSubject could find a valid entering symbol, one
    // last option is available if the entire row is composed of
    // dummy variables. If the constant of the row is zero, then
    // this represents redundant constraints and the new dummy
    // marker can enter the basis. If the constant is non-zero,
    // then it represents an unsatisfiable constraint.
    if (subject.tpe == SymbolType.Invalid && all_dummies(row)) {
      if (!near_zero(row.constant)) {
        return Left(AddConstraintError.UnsatisfiableConstraint);
      } else {
        subject = tag.marker;
      }
    }

    // If an entering symbol still isn't found, then the row must
    // be added using an artificial variable. If that fails, then
    // the row represents an unsatisfiable constraint.
    if (subject.tpe == SymbolType.Invalid) {
      add_with_artificial_variable(row) match {
        case Left(e)  => return Left(AddConstraintError.InternalSolverError(e.str))
        case Right(_) => ()
      }
    } else {
      row.solve_for_symbol(subject);
      substitute(subject, row);
      if (subject.tpe == SymbolType.External && row.constant != 0.0) {
        val v = var_for_symbol(subject)
        var_changed(v);
      }
      rows.update(subject, row);
    }

    cns.update(constraint, tag);

    // Optimizing after each constraint is added performs less
    // aggregate work due to a smaller average system size. It
    // also ensures the solver remains in a consistent state.
    val objective = this.objective.clone();
    optimise(objective) match {
      case Left(e)   => Left(AddConstraintError.InternalSolverError(e.str))
      case Right(()) => Right(())
    }
  }

  /// Remove a constraint from the solver.
  def remove_constraint(constraint: Constraint): Either[RemoveConstraintError, Unit] = {
    val tag = cns.remove(constraint) match {
      case Some(value) => value
      case None        => return Left(RemoveConstraintError.UnknownConstraint)
    }

    // Remove the error effects from the objective function
    // *before* pivoting, or substitutions into the objective
    // will lead to incorrect solver results.
    remove_constraint_effects(constraint, tag);

    // If the marker is basic, simply drop the row. Otherwise,
    // pivot the marker into the basis and then drop the row.
    rows.remove(tag.marker) match {
      case Some(_) => ()
      case None =>
        get_marker_leaving_row(tag.marker) match {
          case Some((leaving, row)) =>
            row.solve_for_symbols(leaving, tag.marker);
            substitute(tag.marker, row);
          case None => return Left(RemoveConstraintError.InternalSolverError("Failed to find leaving row."))
        }
    }

    // Optimizing after each constraint is removed ensures that the
    // solver remains consistent. It makes the solver api easier to
    // use at a small tradeoff for speed.
    val objective = this.objective.clone();
    optimise(objective) match {
      case Right(_) => ()
      case Left(e)  => return Left(RemoveConstraintError.InternalSolverError(e.str))
    }

    // Check for and decrease the reference count for variables referenced by the constraint
    // If the reference count is zero remove the variable from the variable map
    constraint.expression.terms.foreach { term =>
      if (!near_zero(term.coefficient)) {
        var should_remove = false;
        var_data.get(term.variable) match {
          case Some(varData) =>
            varData.count -= 1
            should_remove = varData.count == 0
          case None => ()
        }
        if (should_remove) {
          var_for_symbol.remove(var_data(term.variable).symbol);
          var_data.remove(term.variable);
        }
      }
    }
    Right(())
  }

  /// Test whether a constraint has been added to the solver.
  def has_constraint(constraint: Constraint): Boolean =
    cns.contains(constraint)

  /// Add an edit variable to the solver.
  ///
  /// This method should be called before the `suggest_value` method is
  /// used to supply a suggested value for the given edit variable.
  def add_edit_variable(v: Variable, strength0: Strength): Either[AddEditVariableError, Unit] = {
    if (edits.contains(v)) {
      return Left(AddEditVariableError.DuplicateEditVariable);
    }
    val strength = Strength.clip(strength0);
    if (strength == Strength.REQUIRED) {
      return Left(AddEditVariableError.BadRequiredStrength);
    }
    val cn = Constraint(Expression.from_term(Term(v, 1.0)), strength, RelationalOperator.Equal);
    add_constraint(cn).unwrap()
    edits.update(v, EditInfo(tag = cns(cn), constraint = cn, constant = 0.0));
    Right(())
  }

  /// Remove an edit variable from the solver.
  def remove_edit_variable(v: Variable): Either[RemoveEditVariableError, Unit] =
    edits.remove(v).map(_.constraint) match {
      case Some(constraint) =>
        remove_constraint(constraint) match {
          case Left(RemoveConstraintError.UnknownConstraint) =>
            Left(RemoveEditVariableError.InternalSolverError("Edit constraint not in system"))
          case Left(RemoveConstraintError.InternalSolverError(s)) =>
            Left(RemoveEditVariableError.InternalSolverError(s))
          case Right(()) => Right(())
        }
      case None =>
        Left(RemoveEditVariableError.UnknownEditVariable)
    }

  /// Test whether an edit variable has been added to the solver.
  def has_edit_variable(v: Variable): Boolean =
    edits.contains(v)

  /// Suggest a value for the given edit variable.
  ///
  /// This method should be used after an edit variable has been added to
  /// the solver in order to suggest the value for that variable.
  def suggest_value(variable: Variable, value: Double): Either[SuggestValueError, Unit] = {
    val (info_tag_marker, info_tag_other, delta) = {
      val info = edits.get(variable) match {
        case Some(value) => value
        case None        => return Left(SuggestValueError.UnknownEditVariable)
      }
      val delta = value - info.constant;
      info.constant = value;
      (info.tag.marker, info.tag.other, delta)
    };
    // tag.marker and tag.other are never external symbols

    // The nice version of the following code runs into non-lexical borrow issues.
    // Ideally the `if row...` code would be in the body of the if. Pretend that it is.

    if (rows.contains(info_tag_marker)) {
      val row = rows(info_tag_marker)
      if (row.add(-delta) < 0.0) {
        infeasible_rows.push(info_tag_marker);
      } else if (rows.contains(info_tag_other)) {
        val row = rows(info_tag_other)
        if (row.add(delta) < 0.0) {
          infeasible_rows.push(info_tag_other);
        }
      }
    } else {
      rows.foreach { case (symbol, row) =>
        val coeff = row.coefficient_for(info_tag_marker);
        val diff = delta * coeff;
        if (diff != 0.0 && symbol.tpe == SymbolType.External) {
          val v = var_for_symbol(symbol);
          // inline var_changed - borrow checker workaround
          if (should_clear_changes) {
            changed.clear();
            should_clear_changes = false;
          }
          changed.addOne(v);
        }
        if (
          coeff != 0.0 &&
          row.add(diff) < 0.0 &&
          symbol.tpe != SymbolType.External
        ) {
          infeasible_rows.push(symbol);
        }
      }
    }

    dual_optimise() match {
      case Right(()) => Right(())
      case Left(e)   => Left(SuggestValueError.InternalSolverError(e.str))
    }
  }

  def var_changed(v: Variable): Unit = {
    if (should_clear_changes) {
      changed.clear();
      should_clear_changes = false;
    }
    changed.addOne(v);
  }

  /// Fetches all changes to the values of variables since the last call to this function.
  ///
  /// The list of changes returned is not in a specific order. Each change comprises the variable changed and
  /// the new value of that variable.
  def fetch_changes(): Seq[(Variable, Double)] = {
    if (should_clear_changes) {
      changed.clear();
      should_clear_changes = false;
    } else {
      should_clear_changes = true;
    }
    public_changes.clear();
    changed.foreach { v =>
      this.var_data.get(v).foreach { var_data =>
        val new_value = rows.get(var_data.symbol).map(_.constant).getOrElse(0.0);
        val old_value = var_data.double;
        if (old_value != new_value) {
          public_changes.push((v, new_value));
          var_data.double = new_value;
        }

      }
    }
    public_changes.toSeq
  }

  /// Reset the solver to the empty starting condition.
  ///
  /// This method resets the internal solver state to the empty starting
  /// condition, as if no constraints or edit variables have been added.
  /// This can be faster than deleting the solver and creating a new one
  /// when the entire system must change, since it can avoid unnecessary
  /// heap (de)allocations.
  def reset(): Unit = {
    rows.clear();
    cns.clear();
    var_data.clear();
    var_for_symbol.clear();
    changed.clear();
    should_clear_changes = false;
    edits.clear();
    infeasible_rows.clear();
    objective = Row(0.0);
    artificial = None;
    id_tick = 1;
  }

  /// Get the symbol for the given variable.
  ///
  /// If a symbol does not exist for the variable, one will be created.
  def get_var_symbol(v: Variable): Symbol = {
    val value = var_data.getOrElseUpdate(
      v, {
        val s = Symbol(id_tick, SymbolType.External);
        var_for_symbol.update(s, v);
        id_tick += 1;
        VarData(Double.NaN, s, 0)
      }
    )
    value.count += 1;
    value.symbol
  }

  /// Create a new Row object for the given constraint.
  ///
  /// The terms in the constraint will be converted to cells in the row.
  /// Any term in the constraint with a coefficient of zero is ignored.
  /// This method uses the `getVarSymbol` method to get the symbol for
  /// the variables added to the row. If the symbol for a given cell
  /// variable is basic, the cell variable will be substituted with the
  /// basic row.
  ///
  /// The necessary slack and error variables will be added to the row.
  /// If the constant for the row is negative, the sign for the row
  /// will be inverted so the constant becomes positive.
  ///
  /// The tag will be updated with the marker and error symbols to use
  /// for tracking the movement of the constraint in the tableau.
  def create_row(constraint: Constraint): (Row, Tag) = {
    val expr = constraint.expression;
    val row = Row(expr.constant);
    // Substitute the current basic variables into the row.
    expr.terms.foreach { term =>
      if (!near_zero(term.coefficient)) {
        val symbol = get_var_symbol(term.variable);
        rows.get(symbol) match {
          case Some(other_row) =>
            row.insert_row(other_row, term.coefficient);
          case None =>
            row.insert_symbol(symbol, term.coefficient);
        }
      }
    }

    // Add the necessary slack, error, and dummy variables.
    val tag = constraint.op match {
      case RelationalOperator.GreaterOrEqual | RelationalOperator.LessOrEqual =>
        val coeff = if (constraint.op == RelationalOperator.LessOrEqual) 1.0 else -1.0;
        val slack = Symbol(id_tick, SymbolType.Slack);
        id_tick += 1;
        row.insert_symbol(slack, coeff);
        if (constraint.strength < Strength.REQUIRED) {
          val error = Symbol(id_tick, SymbolType.Error);
          id_tick += 1;
          row.insert_symbol(error, -coeff);
          objective.insert_symbol(error, constraint.strength.value);
          Tag(marker = slack, other = error)
        } else {
          Tag(marker = slack, other = Symbol.invalid)
        }

      case RelationalOperator.Equal =>
        if (constraint.strength < Strength.REQUIRED) {
          val errplus = Symbol(id_tick, SymbolType.Error);
          id_tick += 1;
          val errminus = Symbol(id_tick, SymbolType.Error);
          id_tick += 1;
          row.insert_symbol(errplus, -1.0); // v = eplus - eminus
          row.insert_symbol(errminus, 1.0); // v - eplus + eminus = 0
          objective.insert_symbol(errplus, constraint.strength.value);
          objective.insert_symbol(errminus, constraint.strength.value);
          Tag(marker = errplus, other = errminus)
        } else {
          val dummy = Symbol(id_tick, SymbolType.Dummy);
          id_tick += 1;
          row.insert_symbol(dummy, 1.0);
          Tag(marker = dummy, other = Symbol.invalid)
        }
    };

    // Ensure the row has a positive constant.
    if (row.constant < 0.0) {
      row.reverse_sign();
    }
    (row, tag)
  }

  /// Choose the subject for solving for the row.
  ///
  /// This method will choose the best subject for using as the solve
  /// target for the row. An invalid symbol will be returned if there
  /// is no valid target.
  ///
  /// The symbols are chosen according to the following precedence:
  ///
  /// 1) The first symbol representing an external variable.
  /// 2) A negative slack or error tag variable.
  ///
  /// If a subject cannot be found, an invalid symbol will be returned.
  def choose_subject(row: Row, tag: Tag): Symbol = {
    row.cells.keys.find(_.tpe == SymbolType.External) match {
      case Some(s) => return s
      case None    => ()
    }
    if (tag.marker.tpe == SymbolType.Slack || tag.marker.tpe == SymbolType.Error) {
      if (row.coefficient_for(tag.marker) < 0.0) {
        return tag.marker;
      }
    }
    if (tag.other.tpe == SymbolType.Slack || tag.other.tpe == SymbolType.Error) {
      if (row.coefficient_for(tag.other) < 0.0) {
        return tag.other;
      }
    }
    Symbol.invalid
  }

  /// Add the row to the tableau using an artificial variable.
  ///
  /// This will return false if the constraint cannot be satisfied.
  def add_with_artificial_variable(row: Row): Either[InternalSolverError, Boolean] = {
    // Create and add the artificial variable to the tableau
    val art = Symbol(id_tick, SymbolType.Slack);
    id_tick += 1;
    rows.update(art, row.clone());
    this.artificial = Some(row.clone())

    // Optimize the artificial objective. This is successful
    // only if the artificial objective is optimized to zero.
    val artificial = this.artificial.get.clone()
    this.optimise(artificial) match {
      case Left(msg) => return Left(msg)
      case Right(()) => ()
    }
    val success = near_zero(artificial.constant);
    this.artificial = None;

    // If the artificial variable is basic, pivot the row so that
    // it becomes basic. If the row is constant, exit early.
    rows.remove(art) match {
      case None => ()
      case Some(row) =>
        if (row.cells.isEmpty) {
          return Right(success);
        }
        val entering = any_pivotable_symbol(row); // never External
        if (entering.tpe == SymbolType.Invalid) {
          return Right(false); // unsatisfiable (will this ever happen?)
        }
        row.solve_for_symbols(art, entering);
        substitute(entering, row);
        rows.update(entering, row);

    }

    // Remove the artificial row from the tableau
    rows.foreach { case (_, row) => row.remove(art) }
    objective.remove(art);
    Right(success)
  }

  /// Substitute the parametric symbol with the given row.
  ///
  /// This method will substitute all instances of the parametric symbol
  /// in the tableau and the objective function with the given row.
  def substitute(symbol: Symbol, row: Row): Unit = {
    rows.foreach { case (other_symbol, other_row) =>
      val constant_changed = other_row.substitute(symbol, row);
      if (other_symbol.tpe == SymbolType.External && constant_changed) {
        val v = var_for_symbol(other_symbol);
        // inline var_changed
        if (should_clear_changes) {
          changed.clear();
          should_clear_changes = false;
        }
        changed.addOne(v);
      }
      if (other_symbol.tpe != SymbolType.External && other_row.constant < 0.0) {
        infeasible_rows.push(other_symbol);
      }
    }
    objective.substitute(symbol, row);
    artificial.foreach(_.substitute(symbol, row))
  }

  /// Optimize the system for the given objective function.
  ///
  /// This method performs iterations of Phase 2 of the simplex method
  /// until the objective function reaches a minimum.
  def optimise(objective: Row): Either[InternalSolverError, Unit] = {
    while (true) {
      val entering = get_entering_symbol(objective);
      if (entering.tpe == SymbolType.Invalid) {
        return Right(());
      }
      get_leaving_row(entering) match {
        case Some((leaving, row)) =>
          // pivot the entering symbol into the basis
          row.solve_for_symbols(leaving, entering);
          substitute(entering, row);
          if (entering.tpe == SymbolType.External && row.constant != 0.0) {
            val v = var_for_symbol(entering);
            var_changed(v);
          }
          rows.update(entering, row);

        case None => return Left(InternalSolverError("The objective is unbounded"))
      }
    }
    sys.error("unexpected")
  }

  /// Optimize the system using the dual of the simplex method.
  ///
  /// The current state of the system should be such that the objective
  /// function is optimal, but not feasible. This method will perform
  /// an iteration of the dual simplex method to make the solution both
  /// optimal and feasible.
  def dual_optimise(): Either[InternalSolverError, Unit] = {
    while (infeasible_rows.nonEmpty) {
      val leaving = infeasible_rows.pop();
      val row: Option[Row] = rows.get(leaving) match {
        case Some(row) if row.constant < 0.0 =>
          rows.remove(leaving)
        case _ => None
      }

      row match {
        case Some(row) =>
          val entering = get_dual_entering_symbol(row);
          if (entering.tpe == SymbolType.Invalid) {
            return Left(InternalSolverError("Dual optimise failed."));
          }
          // pivot the entering symbol into the basis
          row.solve_for_symbols(leaving, entering);
          substitute(entering, row);
          if (entering.tpe == SymbolType.External && row.constant != 0.0) {
            val v = var_for_symbol(entering);
            var_changed(v);
          }
          rows.update(entering, row);

        case None => None
      }
    }
    Right(())
  }

  /// Compute the entering variable for a pivot operation.
  ///
  /// This method will return first symbol in the objective function which
  /// is non-dummy and has a coefficient less than zero. If no symbol meets
  /// the criteria, it means the objective function is at a minimum, and an
  /// invalid symbol is returned.
  /// Could return an External symbol
  def get_entering_symbol(objective: Row): Symbol =
    objective.cells
      .collectFirst { case (symbol, value) if symbol.tpe != SymbolType.Dummy && value < 0.0 => symbol }
      .getOrElse(Symbol.invalid)

  /// Compute the entering symbol for the dual optimize operation.
  ///
  /// This method will return the symbol in the row which has a positive
  /// coefficient and yields the minimum ratio for its respective symbol
  /// in the objective function. The provided row *must* be infeasible.
  /// If no symbol is found which meats the criteria, an invalid symbol
  /// is returned.
  /// Could return an External symbol
  def get_dual_entering_symbol(row: Row): Symbol = {
    var entering: Symbol = Symbol.invalid
    var ratio: Double = Double.PositiveInfinity
    row.cells.foreach { case (symbol, value) =>
      if (value > 0.0 && symbol.tpe != SymbolType.Dummy) {
        val coeff = objective.coefficient_for(symbol);
        val r = coeff / value;
        if (r < ratio) {
          ratio = r;
          entering = symbol;
        }
      }
    }
    entering
  }

  /// Get the first Slack or Error symbol in the row.
  ///
  /// If no such symbol is present, and Invalid symbol will be returned.
  /// Never returns an External symbol
  def any_pivotable_symbol(row: Row): Symbol =
    row.cells.keys
      .collectFirst { case symbol if symbol.tpe == SymbolType.Slack || symbol.tpe == SymbolType.Error => symbol }
      .getOrElse(Symbol.invalid)

  /// Compute the row which holds the exit symbol for a pivot.
  ///
  /// This method will return an iterator to the row in the row map
  /// which holds the exit symbol. If no appropriate exit symbol is
  /// found, the end() iterator will be returned. This indicates that
  /// the objective function is unbounded.
  /// Never returns a row for an External symbol
  def get_leaving_row(entering: Symbol): Option[(Symbol, Row)] = {
    var ratio: Double = Double.PositiveInfinity
    var found = Option.empty[Symbol];
    rows.foreach { case (symbol, row) =>
      if (symbol.tpe != SymbolType.External) {
        val temp = row.coefficient_for(entering);
        if (temp < 0.0) {
          val temp_ratio = -row.constant / temp;
          if (temp_ratio < ratio) {
            ratio = temp_ratio;
            found = Some(symbol);
          }
        }
      }
    }
    found.map(s => (s, rows.remove(s).get))
  }

  /// Compute the leaving row for a marker variable.
  ///
  /// This method will return an iterator to the row in the row map
  /// which holds the given marker variable. The row will be chosen
  /// according to the following precedence:
  ///
  /// 1) The row with a restricted basic varible and a negative coefficient
  ///    for the marker with the smallest ratio of -constant / coefficient.
  ///
  /// 2) The row with a restricted basic variable and the smallest ratio
  ///    of constant / coefficient.
  ///
  /// 3) The last unrestricted row which contains the marker.
  ///
  /// If the marker does not exist in any row, the row map end() iterator
  /// will be returned. This indicates an internal solver error since
  /// the marker *should* exist somewhere in the tableau.
  def get_marker_leaving_row(marker: Symbol): Option[(Symbol, Row)] = {
    var r1: Double = Double.PositiveInfinity
    var r2 = r1;
    var first = Option.empty[Symbol];
    var second = Option.empty[Symbol];
    var third = Option.empty[Symbol];

    rows.foreach { case (symbol, row) =>
      val c = row.coefficient_for(marker);
      if (c != 0.0) {
        ()
      } else if (symbol.tpe == SymbolType.External) {
        third = Some(symbol);
      } else if (c < 0.0) {
        val r = -row.constant / c;
        if (r < r1) {
          r1 = r;
          first = Some(symbol);
        }
      } else {
        val r = row.constant / c;
        if (r < r2) {
          r2 = r;
          second = Some(symbol);
        }
      }
    }

    first.orElse(second).orElse(third).flatMap { s =>
      if (s.tpe == SymbolType.External && this.rows(s).constant != 0.0) {
        val v = this.var_for_symbol(s)
        var_changed(v);
      }
      rows.remove(s).map(r => (s, r))
    }
  }

  /// Remove the effects of a constraint on the objective function.
  def remove_constraint_effects(cn: Constraint, tag: Tag): Unit =
    if (tag.marker.tpe == SymbolType.Error) {
      remove_marker_effects(tag.marker, cn.strength);
    } else if (tag.other.tpe == SymbolType.Error) {
      remove_marker_effects(tag.other, cn.strength);
    }

  /// Remove the effects of an error marker on the objective function.
  def remove_marker_effects(marker: Symbol, strength: Strength): Unit =
    rows.get(marker) match {
      case Some(row) =>
        objective.insert_row(row, -strength.value)
        ()
      case None =>
        objective.insert_symbol(marker, -strength.value)
        ()
    }

  /// Test whether a row is composed of all dummy variables.
  def all_dummies(row: Row): Boolean =
    row.cells.keys.forall(s => s.tpe == SymbolType.Dummy)

  /// Get the stored value for a variable.
  ///
  /// Normally values should be retrieved and updated using `fetch_changes`, but
  /// this method can be used for debugging or testing.
  def get_value(v: Variable): Double = {
    val found = for {
      s <- var_data.get(v)
      r <- rows.get(s.symbol)
    } yield r.constant
    found.getOrElse(0.0)
  }
}
