package jatatui.widgets.chart;

import jatatui.core.layout.Constraint;

/// A pair of [Constraint]s used by [Chart] to decide whether the legend should be hidden.
///
/// `width` constrains the legend's horizontal size; `height` constrains the vertical. If the
/// legend exceeds either, it's hidden. [Constraint.Min] is an exception and always shows the
/// legend.
///
/// Replaces upstream's `(Constraint, Constraint)` tuple per the project's "tuples get dedicated
/// record types" rule.
public record HiddenLegendConstraints(Constraint width, Constraint height) {

  /// The default `(Ratio(1, 4), Ratio(1, 4))` constraints used by [Chart].
  public static final HiddenLegendConstraints DEFAULT =
      new HiddenLegendConstraints(new Constraint.Ratio(1, 4), new Constraint.Ratio(1, 4));

  /// Returns the default constraints (alias for [#DEFAULT]).
  public static HiddenLegendConstraints defaultConstraints() {
    return DEFAULT;
  }
}
