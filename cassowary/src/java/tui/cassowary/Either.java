package tui.cassowary;

public sealed interface Either<L, R> permits Either.Left, Either.Right {
  record Left<L, R>(L value) implements Either<L, R> {}

  record Right<L, R>(R value) implements Either<L, R> {}

  static <L, R> Either<L, R> left(L value) {
    return new Left<>(value);
  }

  static <L, R> Either<L, R> right(R value) {
    return new Right<>(value);
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
