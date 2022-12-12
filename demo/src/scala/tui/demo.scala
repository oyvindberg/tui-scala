package tui

import tui.crossterm.CrosstermJni

object demo {
  val Demos = Map[String, Array[String] => Unit](
    "barchart" -> tui.examples.barchart.Main.main,
    "block" -> tui.examples.block.Main.main,
    "canvas" -> tui.examples.canvas.Main.main,
    "chart" -> tui.examples.chart.Main.main,
    "gauge" -> tui.examples.gauge.Main.main,
    "layout" -> tui.examples.layout.Main.main,
    "list" -> tui.examples.list.App.main,
    "paragraph" -> tui.examples.paragraph.App.main,
    "popup" -> tui.examples.popup.Main.main,
    "sparkline" -> tui.examples.sparkline.Main.main,
    "table" -> tui.examples.table.Main.main,
    "tabs" -> tui.examples.tabs.Main.main,
    "user_input" -> tui.examples.user_input.Main.main
  )

  def main(args: Array[String]): Unit =
    args.headOption match {
      case Some("check") =>
        new CrosstermJni() // run for the side effect of testing jni library
        println("ok")
      case Some(name) =>
        Demos.get(name) match {
          case Some(demo) => demo(args.drop(1))
          case None =>
            System.err.println(s"$name is not among ${Demos.keys.mkString(",")}")

        }
      case None => System.err.println(s"specify which demo one of ${Demos.keys.mkString(",")} as parameter")
    }
}
