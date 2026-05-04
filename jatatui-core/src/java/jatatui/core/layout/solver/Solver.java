package jatatui.core.layout.solver;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/// A constraint solver using the Cassowary algorithm. For proper usage please see the top level
// package documentation.
public final class Solver {
  public final Map<Constraint, Tag> cns = new HashMap<>();
  public final Map<Variable, VarData> varData = new HashMap<>();
  public final Map<Symbol, Variable> varForSymbol = new HashMap<>();
  public final Deque<VariableChange> publicChanges = new ArrayDeque<>();
  public final Set<Variable> changed = new HashSet<>();
  public boolean shouldClearChanges = false;
  public final Map<Symbol, Row> rows = new HashMap<>();
  public final Map<Variable, EditInfo> edits = new HashMap<>();
  public final Deque<Symbol> infeasibleRows = new ArrayDeque<>(); // never contains external symbols
  public Row objective = Row.of(0.0);
  public Optional<Row> artificial = Optional.empty();
  public int idTick = 1;

  public Solver() {}

  public Either<AddConstraintError, Void> addConstraints(Constraint[] constraints) {
    int i = 0;
    while (i < constraints.length) {
      Constraint constraint = constraints[i];
      Either<AddConstraintError, Void> r = addConstraint(constraint);
      if (r instanceof Either.Left<?, ?>) {
        return r;
      }
      i += 1;
    }
    return Either.unit();
  }

  public Either<AddConstraintError, Void> addConstraints(List<Constraint> constraints) {
    return addConstraints(constraints.toArray(new Constraint[0]));
  }

  /// Add a constraint to the solver.
  public Either<AddConstraintError, Void> addConstraint(Constraint constraint) {
    if (cns.containsKey(constraint)) {
      return Either.left(new AddConstraintError.DuplicateConstraint());
    }

    // Creating a row causes symbols to reserved for the variables
    // in the constraint. If this method exits with an exception,
    // then its possible those variables will linger in the var map.
    // Since its likely that those variables will be used in other
    // constraints and since exceptional conditions are uncommon,
    // i'm not too worried about aggressive cleanup of the var map.
    RowAndTag rt = createRow(constraint);
    Row row = rt.row;
    Tag tag = rt.tag;
    Symbol subject = chooseSubject(row, tag);

    // If chooseSubject could find a valid entering symbol, one
    // last option is available if the entire row is composed of
    // dummy variables. If the constant of the row is zero, then
    // this represents redundant constraints and the new dummy
    // marker can enter the basis. If the constant is non-zero,
    // then it represents an unsatisfiable constraint.
    if (subject.kind() == SymbolKind.Invalid && allDummies(row)) {
      if (!NearZero.apply(row.constant)) {
        return Either.left(new AddConstraintError.UnsatisfiableConstraint());
      } else {
        subject = tag.marker();
      }
    }

    // If an entering symbol still isn't found, then the row must
    // be added using an artificial variable. If that fails, then
    // the row represents an unsatisfiable constraint.
    if (subject.kind() == SymbolKind.Invalid) {
      Either<InternalSolverError, Boolean> r = addWithArtificialVariable(row);
      if (r instanceof Either.Left<InternalSolverError, Boolean> l) {
        return Either.left(new AddConstraintError.InternalSolverError(l.value().str()));
      }
    } else {
      row.solveForSymbol(subject);
      substitute(subject, row);
      if (subject.kind() == SymbolKind.External && row.constant != 0.0) {
        Variable v = varForSymbol.get(subject);
        varChanged(v);
      }
      rows.put(subject, row);
    }

    cns.put(constraint, tag);

    // Optimizing after each constraint is added performs less
    // aggregate work due to a smaller average system size. It
    // also ensures the solver remains in a consistent state.
    Row objectiveClone = this.objective.clone();
    Either<InternalSolverError, Void> opt = optimise(objectiveClone);
    if (opt instanceof Either.Left<InternalSolverError, Void> l) {
      return Either.left(new AddConstraintError.InternalSolverError(l.value().str()));
    }
    return Either.unit();
  }

  /// Remove a constraint from the solver.
  public Either<RemoveConstraintError, Void> removeConstraint(Constraint constraint) {
    Optional<Tag> tagOpt = Optional.ofNullable(cns.remove(constraint));
    if (tagOpt.isEmpty()) {
      return Either.left(new RemoveConstraintError.UnknownConstraint());
    }
    Tag tag = tagOpt.get();

    // Remove the error effects from the objective function
    // *before* pivoting, or substitutions into the objective
    // will lead to incorrect solver results.
    removeConstraintEffects(constraint, tag);

    // If the marker is basic, simply drop the row. Otherwise,
    // pivot the marker into the basis and then drop the row.
    Optional<Row> removed = Optional.ofNullable(rows.remove(tag.marker()));
    if (removed.isEmpty()) {
      Optional<SymbolAndRow> leavingOpt = getMarkerLeavingRow(tag.marker());
      if (leavingOpt.isPresent()) {
        SymbolAndRow sr = leavingOpt.get();
        sr.row.solveForSymbols(sr.symbol, tag.marker());
        substitute(tag.marker(), sr.row);
      } else {
        return Either.left(
            new RemoveConstraintError.InternalSolverError("Failed to find leaving row."));
      }
    }

    // Optimizing after each constraint is removed ensures that the
    // solver remains consistent. It makes the solver api easier to
    // use at a small tradeoff for speed.
    Row objectiveClone = this.objective.clone();
    Either<InternalSolverError, Void> opt = optimise(objectiveClone);
    if (opt instanceof Either.Left<InternalSolverError, Void> l) {
      return Either.left(new RemoveConstraintError.InternalSolverError(l.value().str()));
    }

    // Check for and decrease the reference count for variables referenced by the constraint
    // If the reference count is zero remove the variable from the variable map
    for (Term term : constraint.expression.terms) {
      if (!NearZero.apply(term.coefficient)) {
        boolean shouldRemove = false;
        Optional<VarData> vdOpt = Optional.ofNullable(varData.get(term.variable));
        if (vdOpt.isPresent()) {
          VarData vd = vdOpt.get();
          vd.count -= 1;
          shouldRemove = vd.count == 0;
        }
        if (shouldRemove) {
          varForSymbol.remove(varData.get(term.variable).symbol);
          varData.remove(term.variable);
        }
      }
    }
    return Either.unit();
  }

  /// Test whether a constraint has been added to the solver.
  public boolean hasConstraint(Constraint constraint) {
    return cns.containsKey(constraint);
  }

  /// Add an edit variable to the solver.
  ///
  /// This method should be called before the `suggestValue` method is
  /// used to supply a suggested value for the given edit variable.
  public Either<AddEditVariableError, Void> addEditVariable(Variable v, Strength strength0) {
    if (edits.containsKey(v)) {
      return Either.left(new AddEditVariableError.DuplicateEditVariable());
    }
    Strength strength = Strength.clip(strength0);
    if (strength.equals(Strength.REQUIRED)) {
      return Either.left(new AddEditVariableError.BadRequiredStrength());
    }
    Constraint cn =
        new Constraint(Expression.fromTerm(new Term(v, 1.0)), strength, RelationalOperator.Equal);
    addConstraint(cn).unwrap();
    edits.put(v, new EditInfo(cns.get(cn), cn, 0.0));
    return Either.unit();
  }

  /// Remove an edit variable from the solver.
  public Either<RemoveEditVariableError, Void> removeEditVariable(Variable v) {
    Optional<EditInfo> infoOpt = Optional.ofNullable(edits.remove(v));
    if (infoOpt.isEmpty()) {
      return Either.left(new RemoveEditVariableError.UnknownEditVariable());
    }
    EditInfo info = infoOpt.get();
    Either<RemoveConstraintError, Void> r = removeConstraint(info.constraint);
    if (r instanceof Either.Left<RemoveConstraintError, Void> l) {
      return switch (l.value()) {
        case RemoveConstraintError.UnknownConstraint __ ->
            Either.left(
                new RemoveEditVariableError.InternalSolverError("Edit constraint not in system"));
        case RemoveConstraintError.InternalSolverError ise ->
            Either.left(new RemoveEditVariableError.InternalSolverError(ise.str()));
      };
    }
    return Either.unit();
  }

  /// Test whether an edit variable has been added to the solver.
  public boolean hasEditVariable(Variable v) {
    return edits.containsKey(v);
  }

  /// Suggest a value for the given edit variable.
  ///
  /// This method should be used after an edit variable has been added to
  /// the solver in order to suggest the value for that variable.
  public Either<SuggestValueError, Void> suggestValue(Variable variable, double value) {
    Optional<EditInfo> infoOpt = Optional.ofNullable(edits.get(variable));
    if (infoOpt.isEmpty()) {
      return Either.left(new SuggestValueError.UnknownEditVariable());
    }
    EditInfo info = infoOpt.get();
    double delta = value - info.constant;
    info.constant = value;
    Symbol infoTagMarker = info.tag.marker();
    Symbol infoTagOther = info.tag.other();
    // tag.marker and tag.other are never external symbols

    // The nice version of the following code runs into non-lexical borrow issues.
    // Ideally the `if row...` code would be in the body of the if. Pretend that it is.

    if (rows.containsKey(infoTagMarker)) {
      Row row = rows.get(infoTagMarker);
      if (row.add(-delta) < 0.0) {
        infeasibleRows.push(infoTagMarker);
      } else if (rows.containsKey(infoTagOther)) {
        Row row2 = rows.get(infoTagOther);
        if (row2.add(delta) < 0.0) {
          infeasibleRows.push(infoTagOther);
        }
      }
    } else {
      for (Map.Entry<Symbol, Row> e : rows.entrySet()) {
        Symbol symbol = e.getKey();
        Row row = e.getValue();
        double coeff = row.coefficientFor(infoTagMarker);
        double diff = delta * coeff;
        if (diff != 0.0 && symbol.kind() == SymbolKind.External) {
          Variable v = varForSymbol.get(symbol);
          // inline var_changed - borrow checker workaround
          if (shouldClearChanges) {
            changed.clear();
            shouldClearChanges = false;
          }
          changed.add(v);
        }
        if (coeff != 0.0 && row.add(diff) < 0.0 && symbol.kind() != SymbolKind.External) {
          infeasibleRows.push(symbol);
        }
      }
    }

    Either<InternalSolverError, Void> r = dualOptimise();
    if (r instanceof Either.Left<InternalSolverError, Void> l) {
      return Either.left(new SuggestValueError.InternalSolverError(l.value().str()));
    }
    return Either.unit();
  }

  public void varChanged(Variable v) {
    if (shouldClearChanges) {
      changed.clear();
      shouldClearChanges = false;
    }
    changed.add(v);
  }

  /// Fetches all changes to the values of variables since the last call to this function.
  ///
  /// The list of changes returned is not in a specific order. Each change comprises the variable
  // changed and
  /// the new value of that variable.
  public List<VariableChange> fetchChanges() {
    if (shouldClearChanges) {
      changed.clear();
      shouldClearChanges = false;
    } else {
      shouldClearChanges = true;
    }
    publicChanges.clear();
    for (Variable v : changed) {
      Optional<VarData> vdOpt = Optional.ofNullable(this.varData.get(v));
      if (vdOpt.isPresent()) {
        VarData vd = vdOpt.get();
        Optional<Row> rOpt = Optional.ofNullable(rows.get(vd.symbol));
        double newValue = rOpt.map(r -> r.constant).orElse(0.0);
        double oldValue = vd.value;
        if (oldValue != newValue) {
          publicChanges.push(new VariableChange(v, newValue));
          vd.value = newValue;
        }
      }
    }
    return new ArrayList<>(publicChanges);
  }

  /// Reset the solver to the empty starting condition.
  ///
  /// This method resets the internal solver state to the empty starting
  /// condition, as if no constraints or edit variables have been added.
  /// This can be faster than deleting the solver and creating a new one
  /// when the entire system must change, since it can avoid unnecessary
  /// heap (de)allocations.
  public void reset() {
    rows.clear();
    cns.clear();
    varData.clear();
    varForSymbol.clear();
    changed.clear();
    shouldClearChanges = false;
    edits.clear();
    infeasibleRows.clear();
    objective = Row.of(0.0);
    artificial = Optional.empty();
    idTick = 1;
  }

  /// Get the symbol for the given variable.
  ///
  /// If a symbol does not exist for the variable, one will be created.
  public Symbol getVarSymbol(Variable v) {
    Optional<VarData> existing = Optional.ofNullable(varData.get(v));
    VarData value;
    if (existing.isEmpty()) {
      Symbol s = new Symbol(idTick, SymbolKind.External);
      varForSymbol.put(s, v);
      idTick += 1;
      value = new VarData(Double.NaN, s, 0);
      varData.put(v, value);
    } else {
      value = existing.get();
    }
    value.count += 1;
    return value.symbol;
  }

  public record RowAndTag(Row row, Tag tag) {}

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
  public RowAndTag createRow(Constraint constraint) {
    Expression expr = constraint.expression;
    Row row = Row.of(expr.constant);
    // Substitute the current basic variables into the row.
    for (Term term : expr.terms) {
      if (!NearZero.apply(term.coefficient)) {
        Symbol symbol = getVarSymbol(term.variable);
        Optional<Row> otherRow = Optional.ofNullable(rows.get(symbol));
        if (otherRow.isPresent()) {
          row.insertRow(otherRow.get(), term.coefficient);
        } else {
          row.insertSymbol(symbol, term.coefficient);
        }
      }
    }

    // Add the necessary slack, error, and dummy variables.
    Tag tag;
    switch (constraint.op) {
      case GreaterOrEqual:
      case LessOrEqual:
        {
          double coeff = constraint.op == RelationalOperator.LessOrEqual ? 1.0 : -1.0;
          Symbol slack = new Symbol(idTick, SymbolKind.Slack);
          idTick += 1;
          row.insertSymbol(slack, coeff);
          if (constraint.strength.compareTo(Strength.REQUIRED) < 0) {
            Symbol error = new Symbol(idTick, SymbolKind.Error);
            idTick += 1;
            row.insertSymbol(error, -coeff);
            objective.insertSymbol(error, constraint.strength.value());
            tag = new Tag(slack, error);
          } else {
            tag = new Tag(slack, Symbol.invalid);
          }
          break;
        }
      case Equal:
        {
          if (constraint.strength.compareTo(Strength.REQUIRED) < 0) {
            Symbol errplus = new Symbol(idTick, SymbolKind.Error);
            idTick += 1;
            Symbol errminus = new Symbol(idTick, SymbolKind.Error);
            idTick += 1;
            row.insertSymbol(errplus, -1.0); // v = eplus - eminus
            row.insertSymbol(errminus, 1.0); // v - eplus + eminus = 0
            objective.insertSymbol(errplus, constraint.strength.value());
            objective.insertSymbol(errminus, constraint.strength.value());
            tag = new Tag(errplus, errminus);
          } else {
            Symbol dummy = new Symbol(idTick, SymbolKind.Dummy);
            idTick += 1;
            row.insertSymbol(dummy, 1.0);
            tag = new Tag(dummy, Symbol.invalid);
          }
          break;
        }
      default:
        throw new IllegalStateException("Unhandled operator: " + constraint.op);
    }

    // Ensure the row has a positive constant.
    if (row.constant < 0.0) {
      row.reverseSign();
    }
    return new RowAndTag(row, tag);
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
  public Symbol chooseSubject(Row row, Tag tag) {
    for (Symbol s : row.cells.keySet()) {
      if (s.kind() == SymbolKind.External) {
        return s;
      }
    }
    if (tag.marker().kind() == SymbolKind.Slack || tag.marker().kind() == SymbolKind.Error) {
      if (row.coefficientFor(tag.marker()) < 0.0) {
        return tag.marker();
      }
    }
    if (tag.other().kind() == SymbolKind.Slack || tag.other().kind() == SymbolKind.Error) {
      if (row.coefficientFor(tag.other()) < 0.0) {
        return tag.other();
      }
    }
    return Symbol.invalid;
  }

  /// Add the row to the tableau using an artificial variable.
  ///
  /// This will return false if the constraint cannot be satisfied.
  public Either<InternalSolverError, Boolean> addWithArtificialVariable(Row row) {
    // Create and add the artificial variable to the tableau
    Symbol art = new Symbol(idTick, SymbolKind.Slack);
    idTick += 1;
    rows.put(art, row.clone());
    this.artificial = Optional.of(row.clone());

    // Optimize the artificial objective. This is successful
    // only if the artificial objective is optimized to zero.
    Row artificialClone = this.artificial.get().clone();
    Either<InternalSolverError, Void> r = this.optimise(artificialClone);
    if (r instanceof Either.Left<InternalSolverError, Void> l) {
      return Either.left(l.value());
    }
    boolean success = NearZero.apply(artificialClone.constant);
    this.artificial = Optional.empty();

    // If the artificial variable is basic, pivot the row so that
    // it becomes basic. If the row is constant, exit early.
    Optional<Row> removedOpt = Optional.ofNullable(rows.remove(art));
    if (removedOpt.isPresent()) {
      Row removed = removedOpt.get();
      if (removed.cells.isEmpty()) {
        return Either.right(success);
      }
      Symbol entering = anyPivotableSymbol(removed); // never External
      if (entering.kind() == SymbolKind.Invalid) {
        return Either.right(false); // unsatisfiable (will this ever happen?)
      }
      removed.solveForSymbols(art, entering);
      substitute(entering, removed);
      rows.put(entering, removed);
    }

    // Remove the artificial row from the tableau
    for (Map.Entry<Symbol, Row> e : rows.entrySet()) {
      e.getValue().remove(art);
    }
    objective.remove(art);
    return Either.right(success);
  }

  /// Substitute the parametric symbol with the given row.
  ///
  /// This method will substitute all instances of the parametric symbol
  /// in the tableau and the objective function with the given row.
  public void substitute(Symbol symbol, Row row) {
    for (Map.Entry<Symbol, Row> e : rows.entrySet()) {
      Symbol otherSymbol = e.getKey();
      Row otherRow = e.getValue();
      boolean constantChanged = otherRow.substitute(symbol, row);
      if (otherSymbol.kind() == SymbolKind.External && constantChanged) {
        Variable v = varForSymbol.get(otherSymbol);
        // inline var_changed
        if (shouldClearChanges) {
          changed.clear();
          shouldClearChanges = false;
        }
        changed.add(v);
      }
      if (otherSymbol.kind() != SymbolKind.External && otherRow.constant < 0.0) {
        infeasibleRows.push(otherSymbol);
      }
    }
    objective.substitute(symbol, row);
    artificial.ifPresent(r -> r.substitute(symbol, row));
  }

  /// Optimize the system for the given objective function.
  ///
  /// This method performs iterations of Phase 2 of the simplex method
  /// until the objective function reaches a minimum.
  public Either<InternalSolverError, Void> optimise(Row objective) {
    while (true) {
      Symbol entering = getEnteringSymbol(objective);
      if (entering.kind() == SymbolKind.Invalid) {
        return Either.unit();
      }
      Optional<SymbolAndRow> leavingOpt = getLeavingRow(entering);
      if (leavingOpt.isEmpty()) {
        return Either.left(new InternalSolverError("The objective is unbounded"));
      }
      SymbolAndRow sr = leavingOpt.get();
      // pivot the entering symbol into the basis
      sr.row.solveForSymbols(sr.symbol, entering);
      substitute(entering, sr.row);
      if (entering.kind() == SymbolKind.External && sr.row.constant != 0.0) {
        Variable v = varForSymbol.get(entering);
        varChanged(v);
      }
      rows.put(entering, sr.row);
    }
  }

  /// Optimize the system using the dual of the simplex method.
  ///
  /// The current state of the system should be such that the objective
  /// function is optimal, but not feasible. This method will perform
  /// an iteration of the dual simplex method to make the solution both
  /// optimal and feasible.
  public Either<InternalSolverError, Void> dualOptimise() {
    while (!infeasibleRows.isEmpty()) {
      Symbol leaving = infeasibleRows.pop();
      Optional<Row> rowOpt = Optional.empty();
      Optional<Row> r0 = Optional.ofNullable(rows.get(leaving));
      if (r0.isPresent() && r0.get().constant < 0.0) {
        rowOpt = Optional.ofNullable(rows.remove(leaving));
      }
      if (rowOpt.isPresent()) {
        Row row = rowOpt.get();
        Symbol entering = getDualEnteringSymbol(row);
        if (entering.kind() == SymbolKind.Invalid) {
          return Either.left(new InternalSolverError("Dual optimise failed."));
        }
        // pivot the entering symbol into the basis
        row.solveForSymbols(leaving, entering);
        substitute(entering, row);
        if (entering.kind() == SymbolKind.External && row.constant != 0.0) {
          Variable v = varForSymbol.get(entering);
          varChanged(v);
        }
        rows.put(entering, row);
      }
    }
    return Either.unit();
  }

  /// Compute the entering variable for a pivot operation.
  ///
  /// This method will return first symbol in the objective function which
  /// is non-dummy and has a coefficient less than zero. If no symbol meets
  /// the criteria, it means the objective function is at a minimum, and an
  /// invalid symbol is returned.
  /// Could return an External symbol
  public Symbol getEnteringSymbol(Row objective) {
    for (Map.Entry<Symbol, Double> e : objective.cells.entrySet()) {
      Symbol symbol = e.getKey();
      double value = e.getValue();
      if (symbol.kind() != SymbolKind.Dummy && value < 0.0) {
        return symbol;
      }
    }
    return Symbol.invalid;
  }

  /// Compute the entering symbol for the dual optimize operation.
  ///
  /// This method will return the symbol in the row which has a positive
  /// coefficient and yields the minimum ratio for its respective symbol
  /// in the objective function. The provided row *must* be infeasible.
  /// If no symbol is found which meats the criteria, an invalid symbol
  /// is returned.
  /// Could return an External symbol
  public Symbol getDualEnteringSymbol(Row row) {
    Symbol entering = Symbol.invalid;
    double ratio = Double.POSITIVE_INFINITY;
    for (Map.Entry<Symbol, Double> e : row.cells.entrySet()) {
      Symbol symbol = e.getKey();
      double value = e.getValue();
      if (value > 0.0 && symbol.kind() != SymbolKind.Dummy) {
        double coeff = objective.coefficientFor(symbol);
        double r = coeff / value;
        if (r < ratio) {
          ratio = r;
          entering = symbol;
        }
      }
    }
    return entering;
  }

  /// Get the first Slack or Error symbol in the row.
  ///
  /// If no such symbol is present, and Invalid symbol will be returned.
  /// Never returns an External symbol
  public Symbol anyPivotableSymbol(Row row) {
    for (Symbol symbol : row.cells.keySet()) {
      if (symbol.kind() == SymbolKind.Slack || symbol.kind() == SymbolKind.Error) {
        return symbol;
      }
    }
    return Symbol.invalid;
  }

  public record SymbolAndRow(Symbol symbol, Row row) {}

  /// Compute the row which holds the exit symbol for a pivot.
  ///
  /// This method will return an iterator to the row in the row map
  /// which holds the exit symbol. If no appropriate exit symbol is
  /// found, the end() iterator will be returned. This indicates that
  /// the objective function is unbounded.
  /// Never returns a row for an External symbol
  public Optional<SymbolAndRow> getLeavingRow(Symbol entering) {
    double ratio = Double.POSITIVE_INFINITY;
    Optional<Symbol> found = Optional.empty();
    for (Map.Entry<Symbol, Row> e : rows.entrySet()) {
      Symbol symbol = e.getKey();
      Row row = e.getValue();
      if (symbol.kind() != SymbolKind.External) {
        double temp = row.coefficientFor(entering);
        if (temp < 0.0) {
          double tempRatio = -row.constant / temp;
          if (tempRatio < ratio) {
            ratio = tempRatio;
            found = Optional.of(symbol);
          }
        }
      }
    }
    if (found.isEmpty()) {
      return Optional.empty();
    }
    Symbol foundSymbol = found.get();
    Row removed = rows.remove(foundSymbol);
    return Optional.of(new SymbolAndRow(foundSymbol, removed));
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
  public Optional<SymbolAndRow> getMarkerLeavingRow(Symbol marker) {
    double r1 = Double.POSITIVE_INFINITY;
    double r2 = r1;
    Optional<Symbol> first = Optional.empty();
    Optional<Symbol> second = Optional.empty();
    Optional<Symbol> third = Optional.empty();

    for (Map.Entry<Symbol, Row> e : rows.entrySet()) {
      Symbol symbol = e.getKey();
      Row row = e.getValue();
      double c = row.coefficientFor(marker);
      if (c == 0.0) {
        continue;
      }
      if (symbol.kind() == SymbolKind.External) {
        third = Optional.of(symbol);
      } else if (c < 0.0) {
        double r = -row.constant / c;
        if (r < r1) {
          r1 = r;
          first = Optional.of(symbol);
        }
      } else {
        double r = row.constant / c;
        if (r < r2) {
          r2 = r;
          second = Optional.of(symbol);
        }
      }
    }

    Optional<Symbol> chosen;
    if (first.isPresent()) {
      chosen = first;
    } else if (second.isPresent()) {
      chosen = second;
    } else {
      chosen = third;
    }
    if (chosen.isEmpty()) {
      return Optional.empty();
    }
    Symbol s = chosen.get();
    if (s.kind() == SymbolKind.External && this.rows.get(s).constant != 0.0) {
      Variable v = this.varForSymbol.get(s);
      varChanged(v);
    }
    Optional<Row> removed = Optional.ofNullable(rows.remove(s));
    if (removed.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(new SymbolAndRow(s, removed.get()));
  }

  /// Remove the effects of a constraint on the objective function.
  public void removeConstraintEffects(Constraint cn, Tag tag) {
    if (tag.marker().kind() == SymbolKind.Error) {
      removeMarkerEffects(tag.marker(), cn.strength);
    }
    if (tag.other().kind() == SymbolKind.Error) {
      removeMarkerEffects(tag.other(), cn.strength);
    }
  }

  /// Remove the effects of an error marker on the objective function.
  public void removeMarkerEffects(Symbol marker, Strength strength) {
    Optional<Row> row = Optional.ofNullable(rows.get(marker));
    if (row.isPresent()) {
      objective.insertRow(row.get(), -strength.value());
    } else {
      objective.insertSymbol(marker, -strength.value());
    }
  }

  /// Test whether a row is composed of all dummy variables.
  public boolean allDummies(Row row) {
    for (Symbol s : row.cells.keySet()) {
      if (s.kind() != SymbolKind.Dummy) return false;
    }
    return true;
  }

  /// Get the stored value for a variable.
  ///
  /// Normally values should be retrieved and updated using `fetchChanges`, but
  /// this method can be used for debugging or testing.
  public double getValue(Variable v) {
    Optional<VarData> s = Optional.ofNullable(varData.get(v));
    if (s.isEmpty()) return 0.0;
    Optional<Row> r = Optional.ofNullable(rows.get(s.get().symbol));
    if (r.isEmpty()) return 0.0;
    return r.get().constant;
  }
}
