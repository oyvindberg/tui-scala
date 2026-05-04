package jatatui.tests.core.layout.solver;

import static jatatui.core.layout.solver.KasuariOps.add;
import static jatatui.core.layout.solver.KasuariOps.constraint;
import static jatatui.core.layout.solver.KasuariOps.div;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.layout.solver.Constraint;
import jatatui.core.layout.solver.Solver;
import jatatui.core.layout.solver.Strength;
import jatatui.core.layout.solver.Variable;
import jatatui.core.layout.solver.WeightedRelation;
import org.junit.jupiter.api.Test;

/// Java port of `submodules/kasuari/tests/quadrilateral.rs`.
public final class QuadrilateralTest {

  private static final double EPSILON = 1e-8;

  private record Point(Variable x, Variable y) {
    static Point create() {
      return new Point(Variable.create(), Variable.create());
    }
  }

  @Test
  void test_quadrilateral() {
    Values values = new Values();

    Point[] points = new Point[] {Point.create(), Point.create(), Point.create(), Point.create()};
    double[][] pointStarts =
        new double[][] {{10.0, 10.0}, {10.0, 200.0}, {200.0, 200.0}, {200.0, 10.0}};
    Point[] midpoints =
        new Point[] {Point.create(), Point.create(), Point.create(), Point.create()};

    Solver solver = new Solver();
    double weight = 1.0;
    double multiplier = 2.0;
    for (int i = 0; i < 4; i++) {
      solver
          .addConstraints(
              new Constraint[] {
                constraint(
                    points[i].x(),
                    WeightedRelation.EQ(Strength.WEAK.times(weight)),
                    pointStarts[i][0]),
                constraint(
                    points[i].y(),
                    WeightedRelation.EQ(Strength.WEAK.times(weight)),
                    pointStarts[i][1])
              })
          .unwrap();
      weight *= multiplier;
    }

    int[][] edges = new int[][] {{0, 1}, {1, 2}, {2, 3}, {3, 0}};
    for (int[] edge : edges) {
      int start = edge[0];
      int end = edge[1];
      solver
          .addConstraints(
              new Constraint[] {
                constraint(
                    midpoints[start].x(),
                    WeightedRelation.EQ(Strength.REQUIRED),
                    div(add(points[start].x(), points[end].x()), 2.0)),
                constraint(
                    midpoints[start].y(),
                    WeightedRelation.EQ(Strength.REQUIRED),
                    div(add(points[start].y(), points[end].y()), 2.0))
              })
          .unwrap();
    }

    solver
        .addConstraints(
            new Constraint[] {
              constraint(
                  add(points[0].x(), 20.0), WeightedRelation.LE(Strength.STRONG), points[2].x()),
              constraint(
                  add(points[0].x(), 20.0), WeightedRelation.LE(Strength.STRONG), points[3].x()),
              constraint(
                  add(points[1].x(), 20.0), WeightedRelation.LE(Strength.STRONG), points[2].x()),
              constraint(
                  add(points[1].x(), 20.0), WeightedRelation.LE(Strength.STRONG), points[3].x()),
              constraint(
                  add(points[0].y(), 20.0), WeightedRelation.LE(Strength.STRONG), points[1].y()),
              constraint(
                  add(points[0].y(), 20.0), WeightedRelation.LE(Strength.STRONG), points[2].y()),
              constraint(
                  add(points[3].y(), 20.0), WeightedRelation.LE(Strength.STRONG), points[1].y()),
              constraint(
                  add(points[3].y(), 20.0), WeightedRelation.LE(Strength.STRONG), points[2].y())
            })
        .unwrap();

    for (Point point : points) {
      solver
          .addConstraints(
              new Constraint[] {
                constraint(point.x(), WeightedRelation.GE(Strength.REQUIRED), 0.0),
                constraint(point.y(), WeightedRelation.GE(Strength.REQUIRED), 0.0),
                constraint(point.x(), WeightedRelation.LE(Strength.REQUIRED), 500.0),
                constraint(point.y(), WeightedRelation.LE(Strength.REQUIRED), 500.0)
              })
          .unwrap();
    }

    values.updateValues(solver.fetchChanges());

    double[][] expectedMidpoints1 =
        new double[][] {{10.0, 105.0}, {105.0, 200.0}, {200.0, 105.0}, {105.0, 10.0}};
    for (int i = 0; i < 4; i++) {
      assertEquals(expectedMidpoints1[i][0], values.valueOf(midpoints[i].x()), EPSILON);
      assertEquals(expectedMidpoints1[i][1], values.valueOf(midpoints[i].y()), EPSILON);
    }

    solver.addEditVariable(points[2].x(), Strength.STRONG).unwrap();
    solver.addEditVariable(points[2].y(), Strength.STRONG).unwrap();
    solver.suggestValue(points[2].x(), 300.0).unwrap();
    solver.suggestValue(points[2].y(), 400.0).unwrap();

    values.updateValues(solver.fetchChanges());

    double[][] expectedPoints =
        new double[][] {{10.0, 10.0}, {10.0, 200.0}, {300.0, 400.0}, {200.0, 10.0}};
    for (int i = 0; i < 4; i++) {
      assertEquals(expectedPoints[i][0], values.valueOf(points[i].x()), EPSILON);
      assertEquals(expectedPoints[i][1], values.valueOf(points[i].y()), EPSILON);
    }

    double[][] expectedMidpoints2 =
        new double[][] {{10.0, 105.0}, {155.0, 300.0}, {250.0, 205.0}, {105.0, 10.0}};
    for (int i = 0; i < 4; i++) {
      assertEquals(expectedMidpoints2[i][0], values.valueOf(midpoints[i].x()), EPSILON);
      assertEquals(expectedMidpoints2[i][1], values.valueOf(midpoints[i].y()), EPSILON);
    }
  }
}
