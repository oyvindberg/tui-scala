package jatatui.core.layout.solver;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class Row {
  public Map<Symbol, Double> cells;
  public double constant;

  public Row(Map<Symbol, Double> cells, double constant) {
    this.cells = cells;
    this.constant = constant;
  }

  public static Row of(double constant) {
    return new Row(new HashMap<>(), constant);
  }

  // shallow clone. investigate more if needed
  @Override
  public Row clone() {
    return new Row(cells, constant);
  }

  public double add(double v) {
    this.constant += v;
    return this.constant;
  }

  public void insertSymbol(Symbol s, double coefficient) {
    Double oldCoefficient = cells.get(s);
    if (oldCoefficient == null) {
      if (!NearZero.apply(coefficient)) {
        cells.put(s, coefficient);
      }
    } else {
      double newCoefficient = coefficient + oldCoefficient;
      if (NearZero.apply(newCoefficient)) {
        cells.remove(s);
      } else {
        cells.put(s, newCoefficient);
      }
    }
  }

  public boolean insertRow(Row other, double coefficient) {
    double constantDiff = other.constant * coefficient;
    constant += constantDiff;
    for (Map.Entry<Symbol, Double> e : other.cells.entrySet()) {
      insertSymbol(e.getKey(), e.getValue() * coefficient);
    }
    return constantDiff != 0.0;
  }

  public Optional<Double> remove(Symbol s) {
    Double v = cells.remove(s);
    return Optional.ofNullable(v);
  }

  public void reverseSign() {
    constant = -constant;
    HashMap<Symbol, Double> next = new HashMap<>(cells.size());
    for (Map.Entry<Symbol, Double> e : cells.entrySet()) {
      next.put(e.getKey(), -e.getValue());
    }
    cells = next;
  }

  public void solveForSymbol(Symbol s) {
    double coeff = -1.0 / cells.get(s);
    cells.remove(s);
    constant *= coeff;
    HashMap<Symbol, Double> next = new HashMap<>(cells.size());
    for (Map.Entry<Symbol, Double> e : cells.entrySet()) {
      next.put(e.getKey(), e.getValue() * coeff);
    }
    cells = next;
  }

  public void solveForSymbols(Symbol lhs, Symbol rhs) {
    insertSymbol(lhs, -1.0);
    solveForSymbol(rhs);
  }

  public double coefficientFor(Symbol s) {
    Double v = cells.get(s);
    return v == null ? 0.0 : v;
  }

  public boolean substitute(Symbol s, Row row) {
    Double coeff = cells.get(s);
    if (coeff == null) {
      return false;
    }
    cells.remove(s);
    return insertRow(row, coeff);
  }
}
