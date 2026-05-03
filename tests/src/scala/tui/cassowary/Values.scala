package tui.cassowary

import scala.collection.mutable
import scala.jdk.CollectionConverters._

case class Values(values: mutable.Map[Variable, Double]) {
  def value_of(v: Variable): Double = values.getOrElse(v, 0.0)

  def update_values(changes: java.util.List[VariableChange]): Unit =
    changes.asScala.foreach { vc =>
      println(s"${vc.variable()} changed to ${vc.value()}")
      values(vc.variable()) = vc.value()
    }
}

object Values {
  def apply(): Values = new Values(mutable.Map.empty)
}
