package tuiexamples
package demo

import tui._
import tui.widgets._
import tui.widgets.canvas._
import tui.widgets.ListWidget
import tui.widgets.ParagraphWidget
import tui.widgets.tabs.TabsWidget

object ui {
  case class draw(app: App) extends Widget {
    override def render(area: Rect, buf: Buffer): Unit = {
      val titles = app.tabs.titles.map(t => Spans.from(Span.styled(t, Style.DEFAULT.fg(Color.Green))))

      Layout
        .detailed()(
          Constraint.Length(3) ->
            BlockWidget(borders = Borders.ALL, title = Some(Spans.nostyle(app.title)))(
              TabsWidget(
                titles = titles,
                highlightStyle = Style.DEFAULT.fg(Color.Yellow),
                selected = app.tabs.index
              )
            ),
          Constraint.Min(0) -> (
            app.tabs.index match {
              case 0 => draw_first_tab(app)
              case 1 => draw_second_tab(app)
              case 2 => draw_third_tab
              case _ => Widget.Empty
            }
          )
        )
        .render(area, buf)
    }
  }

  case class draw_first_tab(app: App) extends Widget {
    override def render(area: Rect, buf: Buffer): Unit =
      Layout
        .detailed()(
          Constraint.Length(9) -> draw_gauges(app),
          Constraint.Min(8) -> draw_charts(app),
          Constraint.Length(7) -> draw_text
        )
        .render(area, buf)
  }

  case class draw_gauges(app: App) extends Widget {
    override def render(area: Rect, buf: Buffer): Unit = {
      val block = BlockWidget.noChildren(borders = Borders.ALL, title = Some(Spans.nostyle("Graphs")))
      block.render(area, buf)

      Layout
        .detailed(margin = Margin(1))(
          Constraint.Length(2) ->
            BlockWidget(title = Some(Spans.nostyle("Gauge:")))(
              GaugeWidget(
                style = Style.DEFAULT.fg(Color.Magenta).bg(Color.Black).addModifier(Modifier.ITALIC | Modifier.BOLD),
                label = Some(Span.nostyle("%.2f".format(app.progress * 100.0))),
                ratio = GaugeWidget.Ratio(app.progress)
              )
            ),
          Constraint.Length(3) ->
            BlockWidget(title = Some(Spans.nostyle("Sparkline:")))(
              SparklineWidget(
                style = Style.DEFAULT.fg(Color.Green),
                data = app.sparkline.points,
                barSet = if (app.enhanced_graphics) symbols.bar.NINE_LEVELS else symbols.bar.THREE_LEVELS
              )
            ),
          Constraint.Length(1) ->
            BlockWidget(title = Some(Spans.nostyle("LineGauge:")))(
              LineGaugeWidget(
                gaugeStyle = Style.DEFAULT.fg(Color.Magenta),
                lineSet = if (app.enhanced_graphics) symbols.line.THICK else symbols.line.NORMAL,
                ratio = GaugeWidget.Ratio(app.progress)
              )
            )
        )
        .render(area, buf)
    }
  }

  case class draw_charts(app: App) extends Widget {
    override def render(area: Rect, buf: Buffer): Unit = {
      val main = Layout()(
        Layout(direction = Direction.Horizontal)(makeTasks, makeLogs),
        makeBarchart
      )

      val combined =
        if (app.show_chart)
          Layout(direction = Direction.Horizontal)(main, makeChart)
        else main

      combined.render(area, buf)
    }

    def makeBarchart: Widget =
      BlockWidget(borders = Borders.ALL, title = Some(Spans.nostyle("Bar chart")))(
        BarChartWidget(
          data = app.barchart.toArray,
          barWidth = 3,
          barGap = 2,
          barSet = if (app.enhanced_graphics) symbols.bar.NINE_LEVELS else symbols.bar.THREE_LEVELS,
          valueStyle = Style.DEFAULT.fg(Color.Black).bg(Color.Green).addModifier(Modifier.ITALIC),
          labelStyle = Style.DEFAULT.fg(Color.Yellow),
          barStyle = Style.DEFAULT.fg(Color.Green)
        )
      )

    def makeLogs: Widget = {
      val logMessages = app.logs.items.map { case (evt, level) =>
        val s = level match {
          case "ERROR"    => Style.DEFAULT.fg(Color.Magenta)
          case "CRITICAL" => Style.DEFAULT.fg(Color.Red)
          case "WARNING"  => Style.DEFAULT.fg(Color.Yellow)
          case _          => Style.DEFAULT.fg(Color.Blue)
        }
        val content = Text.from(Span.styled(level.padTo(9, ' '), s), Span.nostyle(evt))
        ListWidget.Item(content = content)
      }
      BlockWidget(borders = Borders.ALL, title = Some(Spans.nostyle("List")))(
        ListWidget(state = app.logs.state, items = logMessages.toArray)
      )
    }

    def makeTasks: Widget = {
      val items: Array[ListWidget.Item] = app.tasks.items
        .map(i => ListWidget.Item(content = Text.nostyle(i)))
        .toArray
      BlockWidget(borders = Borders.ALL, title = Some(Spans.nostyle("List")))(
        ListWidget(
          state = app.tasks.state,
          items = items,
          highlightStyle = Style.DEFAULT.addModifier(Modifier.BOLD),
          highlightSymbol = Some("> ")
        )
      )
    }

    def makeChart: Widget = {
      val x_labels = Array(
        Span.styled(app.signals.window.x.toString, Style.DEFAULT.addModifier(Modifier.BOLD)),
        Span.nostyle(((app.signals.window.x + app.signals.window.y) / 2.0).toString),
        Span.styled(app.signals.window.y.toString, Style.DEFAULT.addModifier(Modifier.BOLD))
      )

      val datasets = Array(
        ChartWidget.Dataset(name = "data2", marker = symbols.Marker.Dot, style = Style.DEFAULT.fg(Color.Cyan), data = app.signals.sin1.points),
        ChartWidget.Dataset(
          name = "data3",
          marker = if (app.enhanced_graphics) symbols.Marker.Braille else symbols.Marker.Dot,
          style = Style.DEFAULT.fg(Color.Yellow),
          data = app.signals.sin2.points
        )
      )

      val title = Span.styled("Chart", Style.DEFAULT.fg(Color.Cyan).addModifier(Modifier.BOLD))
      BlockWidget(title = Some(Spans.from(title)), borders = Borders.ALL)(
        ChartWidget(
          datasets = datasets,
          xAxis = ChartWidget.Axis(
            title = Some(Spans.nostyle("X Axis")),
            style = Style.DEFAULT.fg(Color.Gray),
            bounds = app.signals.window,
            labels = Some(x_labels)
          ),
          yAxis = ChartWidget.Axis(
            title = Some(Spans.nostyle("Y Axis")),
            style = Style.DEFAULT.fg(Color.Gray),
            bounds = Point(-20.0, 20.0),
            labels = Some(
              Array(
                Span.styled("-20", Style.DEFAULT.addModifier(Modifier.BOLD)),
                Span.nostyle("0"),
                Span.styled("20", Style.DEFAULT.addModifier(Modifier.BOLD))
              )
            )
          )
        )
      )
    }
  }

  object draw_text extends Widget {
    override def render(area: Rect, buf: Buffer): Unit = {
      val text = Text.fromSpans(
        Spans.nostyle("This is a paragraph with several lines. You can change style your text the way you want"),
        Spans.nostyle(""),
        Spans.from(
          Span.nostyle("For example: "),
          Span.styled("under", Style.DEFAULT.fg(Color.Red)),
          Span.nostyle(" "),
          Span.styled("the", Style.DEFAULT.fg(Color.Green)),
          Span.nostyle(" "),
          Span.styled("rainbow", Style.DEFAULT.fg(Color.Blue)),
          Span.nostyle(".")
        ),
        Spans.from(
          Span.nostyle("Oh and if you didn't "),
          Span.styled("notice", Style.DEFAULT.addModifier(Modifier.ITALIC)),
          Span.nostyle(" you can "),
          Span.styled("automatically", Style.DEFAULT.addModifier(Modifier.BOLD)),
          Span.nostyle(" "),
          Span.styled("wrap", Style.DEFAULT.addModifier(Modifier.REVERSED)),
          Span.nostyle(" your "),
          Span.styled("text", Style.DEFAULT.addModifier(Modifier.UNDERLINED)),
          Span.nostyle(".")
        ),
        Spans.nostyle(
          "One more thing is that it should display unicode characters: 10€"
        )
      )
      val titleStyle = Style.DEFAULT.fg(Color.Magenta).addModifier(Modifier.BOLD)

      BlockWidget(borders = Borders.ALL, title = Some(Spans.from(Span.styled("Footer", titleStyle))))(
        ParagraphWidget(text = text, wrap = Some(ParagraphWidget.Wrap(trim = true)))
      ).render(area, buf)
    }
  }

  case class draw_second_tab(app: App) extends Widget {
    override def render(area: Rect, buf: Buffer): Unit = {
      def cell(str: String) = TableWidget.Cell(Text.nostyle(str))
      val up_style = Style.DEFAULT.fg(Color.Green)
      val failure_style = Style.DEFAULT.fg(Color.Red).addModifier(Modifier.RAPID_BLINK | Modifier.CROSSED_OUT)
      val rows = app.servers.map { s =>
        val style = if (s.status == "Up") up_style else failure_style
        TableWidget.Row(cells = Array(s.name, s.location, s.status).map(cell), style = style)
      }

      val table = BlockWidget(title = Some(Spans.nostyle("Servers")), borders = Borders.ALL)(
        TableWidget(
          rows = rows,
          header = Some(
            TableWidget.Row(cells = Array("Server", "Location", "Status").map(cell), style = Style.DEFAULT.fg(Color.Yellow), bottomMargin = 1)
          ),
          widths = Array(Constraint.Length(15), Constraint.Length(15), Constraint.Length(10))
        )
      )

      val map = BlockWidget(title = Some(Spans.nostyle("World")), borders = Borders.ALL)(
        CanvasWidget(
          marker = if (app.enhanced_graphics) symbols.Marker.Braille else symbols.Marker.Dot,
          xBounds = Point(-180.0, 180.0),
          yBounds = Point(-90.0, 90.0)
        ) { ctx =>
          ctx.draw(WorldMap(resolution = MapResolution.High, color = Color.White))
          ctx.layer()
          ctx.draw(Rectangle(x = 0.0, y = 30.0, width = 10.0, height = 10.0, color = Color.Yellow))
          app.servers.zipWithIndex.foreach { case (s1, i) =>
            app.servers.drop(i).foreach { s2 =>
              ctx.draw(Line(x1 = s1.coords.y, y1 = s1.coords.x, y2 = s2.coords.x, x2 = s2.coords.y, color = Color.Yellow))
            }
          }
          app.servers.foreach { server =>
            val color = if (server.status == "Up") Color.Green else Color.Red
            ctx.print(server.coords.y, server.coords.x, Spans.from(Span.styled("X", Style.DEFAULT.fg(color))))
          }
        }
      )

      Layout
        .detailed(direction = Direction.Horizontal)(
          Constraint.Percentage(30) -> table,
          Constraint.Percentage(70) -> map
        )
        .render(area, buf)
    }
  }

  object draw_third_tab extends Widget {
    val colors = Array(
      Color.Reset,
      Color.Black,
      Color.Red,
      Color.Green,
      Color.Yellow,
      Color.Blue,
      Color.Magenta,
      Color.Cyan,
      Color.Gray,
      Color.DarkGray,
      Color.LightRed,
      Color.LightGreen,
      Color.LightYellow,
      Color.LightBlue,
      Color.LightMagenta,
      Color.LightCyan,
      Color.White
    )

    override def render(area: Rect, buf: Buffer): Unit = {
      val items: Array[TableWidget.Row] = colors.map { c =>
        val cells = Array(
          TableWidget.Cell(Text.nostyle(c.toString)), // todo: replicate whatever debug was if necessary
          TableWidget.Cell(Text.from(Span.styled("Foreground", Style.DEFAULT.fg(c)))),
          TableWidget.Cell(Text.from(Span.styled("Background", Style.DEFAULT.bg(c))))
        )
        TableWidget.Row(cells)
      }

      val table = BlockWidget(title = Some(Spans.nostyle("Colors")), borders = Borders.ALL)(
        TableWidget(
          rows = items,
          widths = Array(Constraint.Ratio(1, 3), Constraint.Ratio(1, 3), Constraint.Ratio(1, 3))
        )
      )

      Layout(direction = Direction.Horizontal)(table, Widget.Empty).render(area, buf)
    }
  }
}
