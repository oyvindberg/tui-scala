package jatatui.core.layout;

import java.util.ArrayList;
import java.util.List;

/// A size constraint applied to a layout segment.
///
/// Priorities (highest first): Min, Max, Length, Percentage, Ratio, Fill.
public sealed interface Constraint
    permits Constraint.Min,
        Constraint.Max,
        Constraint.Length,
        Constraint.Percentage,
        Constraint.Ratio,
        Constraint.Fill {

  record Min(int v) implements Constraint {}

  record Max(int v) implements Constraint {}

  record Length(int v) implements Constraint {}

  /// Percentage 0..100 of the available length. Values above 100 are capped at length.
  record Percentage(int v) implements Constraint {}

  /// Ratio numerator / denominator of the available length. `n/0` returns `length` (no panic).
  record Ratio(int numerator, int denominator) implements Constraint {}

  /// Like [Length], but yields any excess space when paired with other Fill constraints.
  record Fill(int v) implements Constraint {}

  /// Apply the constraint to the given length, returning the size to use.
  default int apply(int length) {
    return switch (this) {
      case Percentage p -> {
        float pf = p.v() / 100.0f;
        float lf = length;
        yield (int) Math.min(pf * lf, lf);
      }
      case Ratio r -> {
        float pf = (float) r.numerator() / (float) Math.max(1, r.denominator());
        float lf = length;
        yield (int) Math.min(pf * lf, lf);
      }
      case Length l -> Math.min(length, l.v());
      case Fill f -> Math.min(length, f.v());
      case Max m -> Math.min(length, m.v());
      case Min m -> Math.max(length, m.v());
    };
  }

  static List<Constraint> fromLengths(int... lengths) {
    List<Constraint> out = new ArrayList<>(lengths.length);
    for (int n : lengths) out.add(new Length(n));
    return out;
  }

  static List<Constraint> fromRatios(int[][] ratios) {
    List<Constraint> out = new ArrayList<>(ratios.length);
    for (int[] r : ratios) out.add(new Ratio(r[0], r[1]));
    return out;
  }

  static List<Constraint> fromPercentages(int... percentages) {
    List<Constraint> out = new ArrayList<>(percentages.length);
    for (int n : percentages) out.add(new Percentage(n));
    return out;
  }

  static List<Constraint> fromMaxes(int... maxes) {
    List<Constraint> out = new ArrayList<>(maxes.length);
    for (int n : maxes) out.add(new Max(n));
    return out;
  }

  static List<Constraint> fromMins(int... mins) {
    List<Constraint> out = new ArrayList<>(mins.length);
    for (int n : mins) out.add(new Min(n));
    return out;
  }

  static List<Constraint> fromFills(int... factors) {
    List<Constraint> out = new ArrayList<>(factors.length);
    for (int n : factors) out.add(new Fill(n));
    return out;
  }
}
