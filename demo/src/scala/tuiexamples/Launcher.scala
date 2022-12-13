package tuiexamples

import tui.crossterm.CrosstermJni

object Launcher {
  val Demos: Map[String, Array[String] => Unit] = Map[String, Array[String] => Unit](
    "barchart" -> BarChartExample.main,
    "block" -> BlockExample.main,
    "canvas" -> CanvasExample.main,
    "chart" -> ChartExample.main,
    "custom_widget" -> CustomWidgetExample.main,
    "demo" -> demo.Demo.main,
    "gauge" -> GaugeExample.main,
    "layout" -> LayoutExample.main,
    "list" -> ListExample.main,
    "paragraph" -> ParagraphExample.main,
    "popup" -> PopupExample.main,
    "sparkline" -> SparklineExample.main,
    "table" -> TableExample.main,
    "tabs" -> TabsExample.main,
    "user_input" -> UserInputExample.main
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
