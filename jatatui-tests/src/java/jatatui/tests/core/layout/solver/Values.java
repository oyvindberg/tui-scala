package jatatui.tests.core.layout.solver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jatatui.core.layout.solver.Variable;
import jatatui.core.layout.solver.VariableChange;

/// Test helper that mirrors the `new_values` helper in `submodules/kasuari/tests/common/mod.rs`.
final class Values {
  private final Map<Variable, Double> values = new HashMap<>();

  double valueOf(Variable v) {
    Double d = values.get(v);
    return d == null ? 0.0 : d;
  }

  void updateValues(List<VariableChange> changes) {
    for (VariableChange change : changes) {
      values.put(change.variable(), change.value());
    }
  }
}
