package tui
package cassowary

import tui.cassowary.WeightedRelation._
import tui.cassowary.operators._
import tui.internal.ranges

class quadrilateral extends TuiTest {
  test("test_quadrilateral") {

    case class Point(
        x: Variable = Variable(),
        y: Variable = Variable()
    )
    val values = common.Values()

    val points = Array(Point(), Point(), Point(), Point())
    val point_starts = Array((10.0, 10.0), (10.0, 200.0), (200.0, 200.0), (200.0, 10.0))
    val midpoints = Array(Point(), Point(), Point(), Point())
    val solver = Solver()
    var weight = 1.0
    val multiplier = 2.0

    ranges.range(0, 4) { i =>
      val cs = Array(points(i).x | EQ(Strength.WEAK * weight) | point_starts(i)._1, points(i).y | EQ(Strength.WEAK * weight) | point_starts(i)._2)

      // check that constraint DSL creates the correct thing
      if (i == 0) {
        val expectedCs = Seq(
          Constraint(
            expression = Expression(terms = Array(Term(variable = Variable.force(0), coefficient = 1.0)), constant = -10.0),
            strength = Strength(1.0),
            op = RelationalOperator.Equal
          ),
          Constraint(
            expression = Expression(terms = Array(Term(variable = Variable.force(1), coefficient = 1.0)), constant = -10.0),
            strength = Strength(1.0),
            op = RelationalOperator.Equal
          )
        )

        assert_eq(expected = expectedCs.toList.toString(), actual = cs.toList.toString())
      }

      solver.add_constraints(cs).unwrap()

      weight *= multiplier;
    }

    Array((0, 1), (1, 2), (2, 3), (3, 0)).foreach { case (start, end) =>
      val cs = Array(
        midpoints(start).x | EQ(Strength.REQUIRED) | (points(start).x + points(end).x) / 2.0,
        midpoints(start).y | EQ(Strength.REQUIRED) | (points(start).y + points(end).y) / 2.0
      )
      solver.add_constraints(cs).unwrap();
    }

    solver
      .add_constraints(
        Array(
          points(0).x + 20.0 | LE(Strength.STRONG) | points(2).x,
          points(0).x + 20.0 | LE(Strength.STRONG) | points(3).x,
          points(1).x + 20.0 | LE(Strength.STRONG) | points(2).x,
          points(1).x + 20.0 | LE(Strength.STRONG) | points(3).x,
          points(0).y + 20.0 | LE(Strength.STRONG) | points(1).y,
          points(0).y + 20.0 | LE(Strength.STRONG) | points(2).y,
          points(3).y + 20.0 | LE(Strength.STRONG) | points(1).y,
          points(3).y + 20.0 | LE(Strength.STRONG) | points(2).y
        )
      )
      .unwrap()

    points.foreach { point =>
      solver
        .add_constraints(
          Array(
            point.x | GE(Strength.REQUIRED) | 0.0,
            point.y | GE(Strength.REQUIRED) | 0.0,
            point.x | LE(Strength.REQUIRED) | 500.0,
            point.y | LE(Strength.REQUIRED) | 500.0
          )
        )
        .unwrap()
    }

    values.update_values(solver.fetch_changes())

    assert_eq(
      Array(
        (values.value_of(midpoints(0).x), values.value_of(midpoints(0).y)),
        (values.value_of(midpoints(1).x), values.value_of(midpoints(1).y)),
        (values.value_of(midpoints(2).x), values.value_of(midpoints(2).y)),
        (values.value_of(midpoints(3).x), values.value_of(midpoints(3).y))
      ),
      Array((10.0, 105.0), (105.0, 200.0), (200.0, 105.0), (105.0, 10.0))
    )

    solver.add_edit_variable(points(2).x, Strength.STRONG).unwrap()
    solver.add_edit_variable(points(2).y, Strength.STRONG).unwrap()
    solver.suggest_value(points(2).x, 300.0).unwrap()
    solver.suggest_value(points(2).y, 400.0).unwrap()

    values.update_values(solver.fetch_changes())

    assert_eq(
      Array(
        (values.value_of(points(0).x), values.value_of(points(0).y)),
        (values.value_of(points(1).x), values.value_of(points(1).y)),
        (values.value_of(points(2).x), values.value_of(points(2).y)),
        (values.value_of(points(3).x), values.value_of(points(3).y))
      ),
      Array(
        (10.0, 10.0),
        (10.0, 200.0),
        (300.0, 400.0),
        (200.0, 10.0)
      )
    )

    assert_eq(
      Array(
        (values.value_of(midpoints(0).x), values.value_of(midpoints(0).y)),
        (values.value_of(midpoints(1).x), values.value_of(midpoints(1).y)),
        (values.value_of(midpoints(2).x), values.value_of(midpoints(2).y)),
        (values.value_of(midpoints(3).x), values.value_of(midpoints(3).y))
      ),
      Array(
        (10.0, 105.0),
        (155.0, 300.0),
        (250.0, 205.0),
        (105.0, 10.0)
      )
    )
  }
}
