package jatatui.core.layout.solver;

public sealed interface Either<L, R> permits Either.Left, Either.Right {
  record Left<L, R>(L value) implements Either<L, R> {}

  record Right<L, R>(R value) implements Either<L, R> {}

  static <L, R> Either<L, R> left(L value) {
    return new Left<>(value);
  }

  static <L, R> Either<L, R> right(R value) {
    return new Right<>(value);
  }

  /// Singleton for the void-success case used when `R = Void`. Use [unit] to obtain a
  /// properly-typed reference. Internally a `Right` whose value is the only legal `Void`
  /// inhabitant — `null` — but callers never observe that null because the `Right.value()`
  /// accessor of `Either<E, Void>` is type-`Void` (which has no callable methods).
  Either<?, Void> UNIT = new Right<>(null);

  /// Typed accessor for [UNIT]. Use this in place of `Either.right(null)` for `Either<E, Void>`
  /// success returns.
  @SuppressWarnings("unchecked")
  static <L> Either<L, Void> unit() {
    return (Either<L, Void>) UNIT;
  }

  default boolean isLeft() {
    return this instanceof Left<L, R>;
  }

  default boolean isRight() {
    return this instanceof Right<L, R>;
  }

  default R unwrap() {
    return switch (this) {
      case Right<L, R> r -> r.value();
      case Left<L, R> l -> throw new RuntimeException("failure: " + l.value());
    };
  }
}
