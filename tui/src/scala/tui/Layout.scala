package tui

import tui.cassowary.Strength.{REQUIRED, WEAK}
import tui.cassowary.WeightedRelation._
import tui.cassowary.operators._
import tui.cassowary.{Constraint => CassowaryConstraint, Expression, Solver, Variable}
import tui.internal.ranges

import scala.collection.mutable

case class Layout(
    direction: Direction = Direction.Vertical,
    margin: Margin = Margin(horizontal = 0, vertical = 0),
    constraints: Array[Constraint] = Array.empty,
    /// Whether the last chunk of the computed layout should be expanded to fill the available
    /// space.
    expand_to_fill: Boolean = true
) {
  def split(area: Rect): Array[Rect] =
    Layout.LAYOUT_CACHE.getOrElseUpdate((area, this), Layout.split(area, this))
}

object Layout {
  val LAYOUT_CACHE = mutable.HashMap.empty[(Rect, Layout), Array[Rect]]

  /// A container used by the solver inside split
  case class Element(
      x: Variable = Variable(),
      y: Variable = Variable(),
      width: Variable = Variable(),
      height: Variable = Variable()
  ) {
    def left: Variable = x

    def top: Variable = y

    def right: Expression = x + width

    def bottom: Expression = y + height
  }

  def split(area: Rect, layout: Layout): Array[Rect] = {
    val solver = new Solver
    val vars: mutable.Map[Variable, (Int, Int)] = mutable.Map.empty
    val elements = layout.constraints.map(_ => new Element)
    val results: Array[Rect] = layout.constraints
      .map(_ => Rect.default)

    val dest_area = area.inner(layout.margin);
    ranges.range(0, elements.length) { i =>
      val e = elements(i)
      vars.update(e.x, (i, 0));
      vars.update(e.y, (i, 1));
      vars.update(e.width, (i, 2));
      vars.update(e.height, (i, 3));
    }

    val ccs = Array.newBuilder[CassowaryConstraint]
    ccs.sizeHint(elements.length * 4 + layout.constraints.length * 6)

    elements.foreach { elt =>
      ccs += (elt.width | GE(REQUIRED) | 0.0);
      ccs += (elt.height | GE(REQUIRED) | 0.0);
      ccs += (elt.left | GE(REQUIRED) | dest_area.left.toDouble);
      ccs += (elt.top | GE(REQUIRED) | dest_area.top.toDouble);
      ccs += (elt.right | LE(REQUIRED) | dest_area.right.toDouble);
      ccs += (elt.bottom | LE(REQUIRED) | dest_area.bottom.toDouble);
    }

    elements.headOption foreach { first =>
      val c = layout.direction match {
        case Direction.Horizontal => first.left | EQ(REQUIRED) | dest_area.left.toDouble
        case Direction.Vertical   => first.top | EQ(REQUIRED) | dest_area.top.toDouble
      }
      ccs += c
    }

    if (layout.expand_to_fill) {
      elements.lastOption.foreach { last =>
        val c = layout.direction match {
          case Direction.Horizontal => last.right | EQ(REQUIRED) | dest_area.right.toDouble
          case Direction.Vertical   => last.bottom | EQ(REQUIRED) | dest_area.bottom.toDouble
        }
        ccs += c
      }
    }

    layout.direction match {
      case Direction.Horizontal =>
        elements.sliding(2).foreach {
          case Array(one, two) => ccs += ((one.x + one.width) | EQ(REQUIRED) | two.x);
          case _               => // ignore if only one
        }

        ranges.range(0, layout.constraints.length) { i =>
          ccs += (elements(i).y | EQ(REQUIRED) | dest_area.y.toDouble);
          ccs += (elements(i).height | EQ(REQUIRED) | dest_area.height.toDouble);
          val size = layout.constraints(i)
          ccs += (size match {
            case Constraint.Length(v) =>
              elements(i).width | EQ(WEAK) | v.toDouble
            case Constraint.Percentage(v) =>
              elements(i).width | EQ(WEAK) | ((v * dest_area.width).toDouble / 100.0)
            case Constraint.Ratio(n, d) =>
              elements(i).width | EQ(WEAK) | (dest_area.width.toDouble * n.toDouble / d.toDouble)
            case Constraint.Min(v) =>
              elements(i).width | GE(WEAK) | v.toDouble
            case Constraint.Max(v) =>
              elements(i).width | LE(WEAK) | v.toDouble
          })
        }
      case Direction.Vertical =>
        elements.sliding(2).foreach {
          case Array(one, two) => ccs += ((one.y + one.height) | EQ(REQUIRED) | two.y);
          case _               => // ignore if only one
        }
        ranges.range(0, layout.constraints.length) { i =>
          ccs += (elements(i).x | EQ(REQUIRED) | dest_area.x.toDouble);
          ccs += (elements(i).width | EQ(REQUIRED) | dest_area.width.toDouble);
          val size = layout.constraints(i)
          ccs += (size match {
            case Constraint.Length(v) =>
              elements(i).height | EQ(WEAK) | v.toDouble
            case Constraint.Percentage(v) =>
              elements(i).height | EQ(WEAK) | ((v * dest_area.height).toDouble / 100.0)
            case Constraint.Ratio(n, d) =>
              elements(i).height | EQ(WEAK) | (dest_area.height.toDouble * n.toDouble / d.toDouble)
            case Constraint.Min(v) =>
              elements(i).height | GE(WEAK) | v.toDouble
            case Constraint.Max(v) =>
              elements(i).height | LE(WEAK) | v.toDouble
          });
        }

    }

    solver.add_constraints(ccs.result()) match {
      case Left(err) => sys.error(s"Error while adding constraints: $err")
      case Right(()) => ()
    }

    solver.fetch_changes().foreach { case (v, value0) =>
      val (index, attr) = vars(v)
      val value = math.max(0, value0.toInt)
      val rect = results(index)
      attr match {
        case 0 => results(index) = rect.copy(x = value)
        case 1 => results(index) = rect.copy(y = value)
        case 2 => results(index) = rect.copy(width = value)
        case 3 => results(index) = rect.copy(height = value)
        case _ => ()
      }
    }

    if (layout.expand_to_fill) {
      // Fix imprecision by extending the last item a bit if necessary
      if (results.nonEmpty) {
        val lastIdx = results.length - 1
        val last = results(lastIdx)
        val updated = layout.direction match {
          case Direction.Horizontal => last.copy(height = dest_area.bottom - last.y)
          case Direction.Vertical   => last.copy(width = dest_area.right - last.x)
        }
        results(lastIdx) = updated
      }
    }
    results
  }
}
