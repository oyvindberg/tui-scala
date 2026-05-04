package jatatui.core.layout.solver;

/// This is an intermediate type used in the syntactic sugar for specifying constraints. You should
// not use it
/// directly.
public record PartialConstraint(Expression e, WeightedRelation wr) {}
