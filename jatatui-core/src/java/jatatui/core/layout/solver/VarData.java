package jatatui.core.layout.solver;

public final class VarData {
  public double value;
  public final Symbol symbol;
  public int count;

  public VarData(double value, Symbol symbol, int count) {
    this.value = value;
    this.symbol = symbol;
    this.count = count;
  }
}
