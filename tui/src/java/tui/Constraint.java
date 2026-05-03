package tui;

public sealed interface Constraint
    permits Constraint.Percentage,
        Constraint.Ratio,
        Constraint.Length,
        Constraint.Max,
        Constraint.Min {

  default int apply(int length) {
    return switch (this) {
      case Percentage p -> length * p.p() / 100;
      case Ratio r -> r.num() * length / r.den();
      case Length l -> Math.min(length, l.l());
      case Max m -> Math.min(length, m.m());
      case Min m -> Math.max(length, m.m());
    };
  }

  record Percentage(int p) implements Constraint {
    public Percentage {
      if (p < 0 || p > 100) {
        throw new IllegalArgumentException("Percentage out of range: " + p);
      }
    }
  }

  record Ratio(int num, int den) implements Constraint {}

  record Length(int l) implements Constraint {}

  record Max(int m) implements Constraint {}

  record Min(int m) implements Constraint {}
}
