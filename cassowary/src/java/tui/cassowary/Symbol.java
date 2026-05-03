package tui.cassowary;

public record Symbol(int size, SymbolType tpe) {
  public static final Symbol invalid = new Symbol(0, SymbolType.Invalid);
}
