package tui

import tui.cassowary.Strength.{REQUIRED, WEAK}
import tui.cassowary.WeightedRelation._
import tui.cassowary.operators._
import tui.cassowary.{Constraint => CassowaryConstraint, Expression, Solver, Variable}
import tui.internal.ranges

import scala.collection.mutable

/** @param direction
  * @param margin
  * @param constraints
  *   Whether the last chunk of the computed layout should be expanded to fill the available space.
  * @param expand_to_fill
  */
class Layout(
    direction: Direction = Direction.Vertical,
    margin: Margin = Margin.None,
    expandToFill: Boolean = true
)(widgets: (Constraint, Widget)*)
    extends Widget {

  override def render(area: Rect, buf: Buffer): Unit = {
    val constraints = widgets.map(_._1).toArray
    val chunks = Layout.cached(area, constraints, margin, direction, expandToFill)

    ranges.range(0, widgets.length) { i =>
      val (_, widget) = widgets(i)
      val chunk = chunks(i)
      widget.render(chunk, buf)
    }
  }
}

object Layout {
  def detailed(direction: Direction = Direction.Vertical, margin: Margin = Margin.None, expandToFill: Boolean = true)(widgets: (Constraint, Widget)*) =
    new Layout(direction, margin, expandToFill)(widgets: _*)

  def apply(direction: Direction = Direction.Vertical, margin: Margin = Margin.None, expandToFill: Boolean = true)(widgets: Widget*) =
    new Layout(direction, margin, expandToFill)(widgets.map(w => Constraint.Ratio(1, widgets.length) -> w): _*)

  private val LAYOUT_CACHE = mutable.HashMap.empty[(Rect, Array[Constraint], Margin, Direction, Boolean), Array[Rect]]

  /** A container used by the solver inside split
    */
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

  def cached(area: Rect, constraints: Array[Constraint], margin: Margin, direction: Direction, expandToFill: Boolean): Array[Rect] =
    Layout.LAYOUT_CACHE.getOrElseUpdate(
      (area, constraints, margin, direction, expandToFill),
      split(area, constraints, margin, direction, expandToFill)
    )

  def split(area: Rect, constraints: Array[Constraint], margin: Margin, direction: Direction, expandToFill: Boolean): Array[Rect] = {
    val solver = new Solver
    val vars: mutable.Map[Variable, (Int, Int)] = mutable.Map.empty
    val elements = constraints.map(_ => new Element)
    val results: Array[Rect] = constraints.map(_ => Rect.default)

    val dest_area = area.inner(margin)
    ranges.range(0, elements.length) { i =>
      val e = elements(i)
      vars.update(e.x, (i, 0))
      vars.update(e.y, (i, 1))
      vars.update(e.width, (i, 2))
      vars.update(e.height, (i, 3));
    }

    val ccs = Array.newBuilder[CassowaryConstraint]
    ccs.sizeHint(elements.length * 4 + constraints.length * 6)

    elements.foreach { elt =>
      ccs += (elt.width | GE(REQUIRED) | 0.0)
      ccs += (elt.height | GE(REQUIRED) | 0.0)
      ccs += (elt.left | GE(REQUIRED) | dest_area.left.toDouble)
      ccs += (elt.top | GE(REQUIRED) | dest_area.top.toDouble)
      ccs += (elt.right | LE(REQUIRED) | dest_area.right.toDouble)
      ccs += (elt.bottom | LE(REQUIRED) | dest_area.bottom.toDouble);
    }

    elements.headOption foreach { first =>
      val c = direction match {
        case Direction.Horizontal => first.left | EQ(REQUIRED) | dest_area.left.toDouble
        case Direction.Vertical   => first.top | EQ(REQUIRED) | dest_area.top.toDouble
      }
      ccs += c
    }

    if (expandToFill) {
      elements.lastOption.foreach { last =>
        val c = direction match {
          case Direction.Horizontal => last.right | EQ(REQUIRED) | dest_area.right.toDouble
          case Direction.Vertical   => last.bottom | EQ(REQUIRED) | dest_area.bottom.toDouble
        }
        ccs += c
      }
    }

    direction match {
      case Direction.Horizontal =>
        elements.sliding(2).foreach {
          case Array(one, two) => ccs += ((one.x + one.width) | EQ(REQUIRED) | two.x);
          case _               => // ignore if only one
        }

        ranges.range(0, constraints.length) { i =>
          ccs += (elements(i).y | EQ(REQUIRED) | dest_area.y.toDouble)
          ccs += (elements(i).height | EQ(REQUIRED) | dest_area.height.toDouble)
          val size = constraints(i)
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
        ranges.range(0, constraints.length) { i =>
          ccs += (elements(i).x | EQ(REQUIRED) | dest_area.x.toDouble)
          ccs += (elements(i).width | EQ(REQUIRED) | dest_area.width.toDouble)
          val size = constraints(i)
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

    if (expandToFill) {
      // Fix imprecision by extending the last item a bit if necessary
      if (results.nonEmpty) {
        val lastIdx = results.length - 1
        val last = results(lastIdx)
        val updated = direction match {
          case Direction.Horizontal => last.copy(height = dest_area.bottom - last.y)
          case Direction.Vertical   => last.copy(width = dest_area.right - last.x)
        }
        results(lastIdx) = updated
      }
    }
    results
  }
}
