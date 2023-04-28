package tui
package cassowary

import tui.cassowary.WeightedRelation._
import tui.cassowary.operators._

class RemovalTest extends TuiTest {
  ignore("remove_constraint") {
    val values = Values()

    val solver = Solver()

    val v = Variable()

    val constraint: Constraint = v | EQ(Strength.REQUIRED) | 100.0
    solver.add_constraint(constraint).unwrap()
    values.update_values(solver.fetch_changes())

    assertEq(values.value_of(v), 100.0)

    solver.remove_constraint(constraint).unwrap()
    solver.add_constraint(v | EQ(Strength.REQUIRED) | 0.0).unwrap()
    values.update_values(solver.fetch_changes())

    assertEq(values.value_of(v), 0.0)
  }
}
