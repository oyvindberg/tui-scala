package tui
package cassowary

class RemovalTest extends TuiTest {
  ignore("remove_constraint") {
    val values = Values()

    val solver = new Solver()

    val v = Variable.create()

    val constraint: Constraint = CassowaryOps.constraint(v, WeightedRelation.EQ(Strength.REQUIRED), 100.0)
    solver.addConstraint(constraint).unwrap()
    values.update_values(solver.fetchChanges())

    assertEq(values.value_of(v), 100.0)

    solver.removeConstraint(constraint).unwrap()
    solver.addConstraint(CassowaryOps.constraint(v, WeightedRelation.EQ(Strength.REQUIRED), 0.0)).unwrap()
    values.update_values(solver.fetchChanges())

    assertEq(values.value_of(v), 0.0)
  }
}
