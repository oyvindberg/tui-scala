package tui;

public sealed interface Constraint
    permits Constraint.Percentage,
        Constraint.Ratio,
        Constraint.Length,
        Constraint.Max,
        Constraint.Min {

  default int apply(int length) {
    return switch (this) {
      case Percentage p -> Math.min((int) ((p.p() / 100.0) * length), length);
      case Ratio r -> {
        // avoid division by zero by using 1 when denominator is 0:
        // 0/0 -> 0 and x/0 -> length for x != 0
        double pct = r.num() / (double) Math.max(1, r.den());
        yield Math.min((int) (pct * length), length);
      }
      case Length l -> Math.min(length, l.l());
      case Max m -> Math.min(length, m.m());
      case Min m -> Math.max(length, m.m());
    };
  }

  record Percentage(int p) implements Constraint {}

  record Ratio(int num, int den) implements Constraint {}

  record Length(int l) implements Constraint {}

  record Max(int m) implements Constraint {}

  record Min(int m) implements Constraint {}
}
