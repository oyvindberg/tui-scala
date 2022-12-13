package tui.cassowary

import scala.collection.mutable

case class Values(values: mutable.Map[Variable, Double] = mutable.Map.empty) {
  def value_of(v: Variable): Double = values.getOrElse(v, 0.0)

  def update_values(changes: Iterable[(Variable, Double)]): Unit =
    changes.foreach { case (v, value) =>
      println(s"$v changed to $value");
      values(v) = value
    }
}
