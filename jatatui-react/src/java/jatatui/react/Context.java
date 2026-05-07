package jatatui.react;

/// Identity-keyed value passed through the Element tree without prop drilling.
///
/// Mirrors React's `createContext`. Construct one as a `static final` constant somewhere; pass it
/// to [Components#provide] near the top of the tree to set a value, and to
/// [RenderContext#useContext] anywhere below to read it. When no provider is in scope,
/// `useContext` returns [#defaultValue].
public final class Context<T> {

  /// Returned from [RenderContext#useContext] when no enclosing provider supplies a value.
  public final T defaultValue;

  private Context(T defaultValue) {
    this.defaultValue = defaultValue;
  }

  /// Construct a new context with the given default. Each call returns a distinct identity —
  /// store it as a `static final` constant so providers and consumers can share it.
  public static <T> Context<T> create(T defaultValue) {
    return new Context<>(defaultValue);
  }
}
