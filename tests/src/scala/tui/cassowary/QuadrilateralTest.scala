package tui
package cassowary

import tui.cassowary.{CassowaryOps => Ops}
import tui.cassowary.WeightedRelation.{EQ, GE, LE}
import tui.internal.Ranges

class QuadrilateralTest extends TuiTest {
  test("test_quadrilateral") {

    case class Point(
        x: Variable,
        y: Variable
    )
    object Point {
      def create(): Point = Point(Variable.create(), Variable.create())
    }

    val values = Values()

    val points = Array(Point.create(), Point.create(), Point.create(), Point.create())
    val point_starts = Array((10.0, 10.0), (10.0, 200.0), (200.0, 200.0), (200.0, 10.0))
    val midpoints = Array(Point.create(), Point.create(), Point.create(), Point.create())
    val solver = new Solver()
    var weight = 1.0
    val multiplier = 2.0

    Ranges.range(0, 4, (i: Int) => {
      val cs = Array[Constraint](
        Ops.constraint(points(i).x, EQ(Strength.WEAK.times(weight)), point_starts(i)._1),
        Ops.constraint(points(i).y, EQ(Strength.WEAK.times(weight)), point_starts(i)._2)
      )

      solver.addConstraints(cs).unwrap()

      weight *= multiplier;
    })

    Array((0, 1), (1, 2), (2, 3), (3, 0)).foreach { case (start, end) =>
      val cs = Array[Constraint](
        Ops.constraint(
          midpoints(start).x,
          EQ(Strength.REQUIRED),
          Ops.div(Ops.add(points(start).x, points(end).x), 2.0)
        ),
        Ops.constraint(
          midpoints(start).y,
          EQ(Strength.REQUIRED),
          Ops.div(Ops.add(points(start).y, points(end).y), 2.0)
        )
      )
      solver.addConstraints(cs).unwrap();
    }

    solver
      .addConstraints(
        Array[Constraint](
          Ops.constraint(Ops.add(points(0).x, 20.0), LE(Strength.STRONG), points(2).x),
          Ops.constraint(Ops.add(points(0).x, 20.0), LE(Strength.STRONG), points(3).x),
          Ops.constraint(Ops.add(points(1).x, 20.0), LE(Strength.STRONG), points(2).x),
          Ops.constraint(Ops.add(points(1).x, 20.0), LE(Strength.STRONG), points(3).x),
          Ops.constraint(Ops.add(points(0).y, 20.0), LE(Strength.STRONG), points(1).y),
          Ops.constraint(Ops.add(points(0).y, 20.0), LE(Strength.STRONG), points(2).y),
          Ops.constraint(Ops.add(points(3).y, 20.0), LE(Strength.STRONG), points(1).y),
          Ops.constraint(Ops.add(points(3).y, 20.0), LE(Strength.STRONG), points(2).y)
        )
      )
      .unwrap()

    points.foreach { point =>
      solver
        .addConstraints(
          Array[Constraint](
            Ops.constraint(point.x, GE(Strength.REQUIRED), 0.0),
            Ops.constraint(point.y, GE(Strength.REQUIRED), 0.0),
            Ops.constraint(point.x, LE(Strength.REQUIRED), 500.0),
            Ops.constraint(point.y, LE(Strength.REQUIRED), 500.0)
          )
        )
        .unwrap()
    }

    values.update_values(solver.fetchChanges())

    assertEq(
      Array(
        (values.value_of(midpoints(0).x), values.value_of(midpoints(0).y)),
        (values.value_of(midpoints(1).x), values.value_of(midpoints(1).y)),
        (values.value_of(midpoints(2).x), values.value_of(midpoints(2).y)),
        (values.value_of(midpoints(3).x), values.value_of(midpoints(3).y))
      ),
      Array((10.0, 105.0), (105.0, 200.0), (200.0, 105.0), (105.0, 10.0))
    )

    solver.addEditVariable(points(2).x, Strength.STRONG).unwrap()
    solver.addEditVariable(points(2).y, Strength.STRONG).unwrap()
    solver.suggestValue(points(2).x, 300.0).unwrap()
    solver.suggestValue(points(2).y, 400.0).unwrap()

    values.update_values(solver.fetchChanges())

    assertEq(
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

    assertEq(
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
