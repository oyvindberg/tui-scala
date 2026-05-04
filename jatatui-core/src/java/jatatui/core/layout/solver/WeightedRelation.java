package jatatui.core.layout.solver;

/// This is part of the syntactic sugar used for specifying constraints. This enum should be used as part of a
/// constraint expression. See the module documentation for more information.
public sealed interface WeightedRelation permits WeightedRelation.EQ, WeightedRelation.LE, WeightedRelation.GE {

  record OpAndStrength(RelationalOperator op, Strength strength) {}

  default OpAndStrength into() {
    return switch (this) {
      case EQ eq -> new OpAndStrength(RelationalOperator.Equal, eq.value());
      case LE le -> new OpAndStrength(RelationalOperator.LessOrEqual, le.value());
      case GE ge -> new OpAndStrength(RelationalOperator.GreaterOrEqual, ge.value());
    };
  }

  /// `==`
  record EQ(Strength value) implements WeightedRelation {}

  /// `<=`
  record LE(Strength value) implements WeightedRelation {}

  /// `>=`
  record GE(Strength value) implements WeightedRelation {}

  static EQ EQ(Strength s) {
    return new EQ(s);
  }

  static LE LE(Strength s) {
    return new LE(s);
  }

  static GE GE(Strength s) {
    return new GE(s);
  }
}
