package tui

object demo {
  val Demos = Map[String, Array[String] => Unit](
    "sparkline" -> tui.examples.sparkline.Main.main,
    "barchart" -> tui.examples.barchart.Main.main,
    "tabs" -> tui.examples.tabs.Main.main,
    "list" -> tui.examples.list.App.main,
    "paragraph" -> tui.examples.paragraph.App.main,
    "table" -> tui.examples.table.Main.main
  )

  def main(args: Array[String]): Unit =
    args.headOption match {
      case Some(name) =>
        Demos.get(name) match {
          case Some(demo) => demo(args.drop(1))
          case None =>
            System.err.println(s"$name is not among ${Demos.keys.mkString(",")}")

        }
      case None => System.err.println(s"specify which demo one of ${Demos.keys.mkString(",")} as parameter")
    }
}
