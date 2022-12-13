package tui

//#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash)]
sealed trait Constraint {
  def apply(length: Int): Int =
    this match {
      case Constraint.Percentage(p)   => length * p / 100
      case Constraint.Ratio(num, den) => num * length / den
      case Constraint.Length(l)       => length.min(l)
      case Constraint.Max(m)          => length.min(m)
      case Constraint.Min(m)          => length.max(m)
    }
}

object Constraint {
  case class Percentage(p: Int) extends Constraint {
    require(p >= 0 && p <= 100)
  }
  case class Ratio(num: Int, den: Int) extends Constraint
  case class Length(l: Int) extends Constraint
  case class Max(m: Int) extends Constraint
  case class Min(m: Int) extends Constraint
}
