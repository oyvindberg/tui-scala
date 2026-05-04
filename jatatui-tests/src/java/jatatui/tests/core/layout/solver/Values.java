package jatatui.tests.core.layout.solver;

import jatatui.core.layout.solver.Variable;
import jatatui.core.layout.solver.VariableChange;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/// Test helper that mirrors the `new_values` helper in `submodules/kasuari/tests/common/mod.rs`.
final class Values {
  private final Map<Variable, Double> values = new HashMap<>();

  double valueOf(Variable v) {
    return Optional.ofNullable(values.get(v)).orElse(0.0);
  }

  void updateValues(List<VariableChange> changes) {
    for (VariableChange change : changes) {
      values.put(change.variable(), change.value());
    }
  }
}
