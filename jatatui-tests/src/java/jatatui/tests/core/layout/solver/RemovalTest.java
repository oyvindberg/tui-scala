package jatatui.tests.core.layout.solver;

import static jatatui.core.layout.solver.KasuariOps.constraint;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.layout.solver.Constraint;
import jatatui.core.layout.solver.Solver;
import jatatui.core.layout.solver.Strength;
import jatatui.core.layout.solver.Variable;
import jatatui.core.layout.solver.WeightedRelation;
import org.junit.jupiter.api.Test;

/// Java port of `submodules/kasuari/tests/removal.rs`.
public final class RemovalTest {

  private static final double EPSILON = 1e-8;

  @Test
  void remove_constraint() {
    Values values = new Values();

    Solver solver = new Solver();

    Variable val = Variable.create();

    Constraint c = constraint(val, WeightedRelation.EQ(Strength.REQUIRED), 100.0);
    solver.addConstraint(c).unwrap();
    values.updateValues(solver.fetchChanges());

    assertEquals(100.0, values.valueOf(val), EPSILON);

    solver.removeConstraint(c).unwrap();
    solver.addConstraint(constraint(val, WeightedRelation.EQ(Strength.REQUIRED), 0.0)).unwrap();
    values.updateValues(solver.fetchChanges());

    assertEquals(0.0, values.valueOf(val), EPSILON);
  }
}
