package jatatui.core.layout.solver;

public record Symbol(int size, SymbolKind kind) {
  public static final Symbol invalid = new Symbol(0, SymbolKind.Invalid);
}
