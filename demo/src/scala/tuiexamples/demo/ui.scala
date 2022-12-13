package tuiexamples
package demo

import tui._
import tui.widgets._
import tui.widgets.canvas._
import tui.widgets.ListWidget
import tui.widgets.ParagraphWidget
import tui.widgets.tabs.TabsWidget

object ui {
  def draw(f: Frame, app: App): Unit = {
    val chunks = Layout(constraints = Array(Constraint.Length(3), Constraint.Min(0))).split(f.size)
    val titles = app.tabs.titles.map(t => Spans.from(Span.styled(t, Style.DEFAULT.fg(Color.Green))))

    val tabs = TabsWidget(
      titles = titles,
      block = Some(BlockWidget(borders = Borders.ALL, title = Some(Spans.nostyle(app.title)))),
      highlight_style = Style.DEFAULT.fg(Color.Yellow),
      selected = app.tabs.index
    )
    f.render_widget(tabs, chunks(0))
    app.tabs.index match {
      case 0 => draw_first_tab(f, app, chunks(1))
      case 1 => draw_second_tab(f, app, chunks(1))
      case 2 => draw_third_tab(f, chunks(1))
      case _ =>
    }
  }

  def draw_first_tab(f: Frame, app: App, area: Rect): Unit = {
    val chunks = Layout(constraints = Array(Constraint.Length(9), Constraint.Min(8), Constraint.Length(7))).split(area)
    draw_gauges(f, app, chunks(0))
    draw_charts(f, app, chunks(1))
    draw_text(f, chunks(2))
  }

  def draw_gauges(f: Frame, app: App, area: Rect): Unit = {
    val chunks = Layout(constraints = Array(Constraint.Length(2), Constraint.Length(3), Constraint.Length(1)), margin = Margin(1)).split(area)
    val block = BlockWidget(borders = Borders.ALL, title = Some(Spans.nostyle("Graphs")))
    f.render_widget(block, area)

    val label = "%.2f".format(app.progress * 100.0)
    val gauge = GaugeWidget(
      block = Some(BlockWidget(title = Some(Spans.nostyle("Gauge:")))),
      gauge_style = Style.DEFAULT.fg(Color.Magenta).bg(Color.Black).add_modifier(Modifier.ITALIC | Modifier.BOLD),
      label = Some(Span.nostyle(label)),
      ratio = GaugeWidget.Ratio(app.progress)
    )
    f.render_widget(gauge, chunks(0))

    val sparkline = SparklineWidget(
      block = Some(BlockWidget(title = Some(Spans.nostyle("Sparkline:")))),
      style = Style.DEFAULT.fg(Color.Green),
      data = app.sparkline.points,
      bar_set = if (app.enhanced_graphics) symbols.bar.NINE_LEVELS else symbols.bar.THREE_LEVELS
    )
    f.render_widget(sparkline, chunks(1))

    val line_gauge = LineGaugeWidget(
      block = Some(BlockWidget(title = Some(Spans.nostyle("LineGauge:")))),
      gauge_style = Style.DEFAULT.fg(Color.Magenta),
      line_set = if (app.enhanced_graphics) symbols.line.THICK else symbols.line.NORMAL,
      ratio = GaugeWidget.Ratio(app.progress)
    )
    f.render_widget(line_gauge, chunks(2))
  }

  def draw_charts(f: Frame, app: App, area: Rect): Unit = {
    val constraints: Array[Constraint] =
      if (app.show_chart) Array(Constraint.Percentage(50), Constraint.Percentage(50))
      else Array(Constraint.Percentage(100))

    val chunks0 = Layout(constraints = constraints, direction = Direction.Horizontal).split(area);
    {
      val chunks1 = Layout(constraints = Array(Constraint.Percentage(50), Constraint.Percentage(50))).split(chunks0(0));
      {
        val chunks2 = Layout(constraints = Array(Constraint.Percentage(50), Constraint.Percentage(50)), direction = Direction.Horizontal).split(chunks1(0))

        // Draw tasks
        val items: Array[ListWidget.Item] = app.tasks.items
          .map(i => ListWidget.Item(content = Text.nostyle(i)))
          .toArray
        val tasks = ListWidget(
          items = items,
          block = Some(BlockWidget(borders = Borders.ALL, title = Some(Spans.nostyle("List")))),
          highlight_style = Style.DEFAULT.add_modifier(Modifier.BOLD),
          highlight_symbol = Some("> ")
        )
        f.render_stateful_widget(tasks, chunks2(0))(app.tasks.state)

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

        val logs = ListWidget(
          items = logMessages.toArray,
          block = Some(BlockWidget(borders = Borders.ALL, title = Some(Spans.nostyle("List"))))
        )
        f.render_stateful_widget(logs, chunks2(1))(app.logs.state)
      }

      val barchart = BarChartWidget(
        block = Some(BlockWidget(borders = Borders.ALL, title = Some(Spans.nostyle("Bar chart")))),
        data = app.barchart.toArray,
        bar_width = 3,
        bar_gap = 2,
        bar_set = if (app.enhanced_graphics) symbols.bar.NINE_LEVELS else symbols.bar.THREE_LEVELS,
        value_style = Style.DEFAULT.fg(Color.Black).bg(Color.Green).add_modifier(Modifier.ITALIC),
        label_style = Style.DEFAULT.fg(Color.Yellow),
        bar_style = Style.DEFAULT.fg(Color.Green)
      )
      f.render_widget(barchart, chunks1(1))
    }
    if (app.show_chart) {
      val x_labels = Array(
        Span.styled(app.signals.window.x.toString, Style.DEFAULT.add_modifier(Modifier.BOLD)),
        Span.nostyle(((app.signals.window.x + app.signals.window.y) / 2.0).toString),
        Span.styled(app.signals.window.y.toString, Style.DEFAULT.add_modifier(Modifier.BOLD))
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
      val chart = ChartWidget(
        datasets = datasets,
        block = {
          val title = Span.styled("Chart", Style.DEFAULT.fg(Color.Cyan).add_modifier(Modifier.BOLD))
          Some(BlockWidget(title = Some(Spans.from(title)), borders = Borders.ALL))
        },
        x_axis = ChartWidget.Axis(
          title = Some(Spans.nostyle("X Axis")),
          style = Style.DEFAULT.fg(Color.Gray),
          bounds = app.signals.window,
          labels = Some(x_labels)
        ),
        y_axis = ChartWidget.Axis(
          title = Some(Spans.nostyle("Y Axis")),
          style = Style.DEFAULT.fg(Color.Gray),
          bounds = Point(-20.0, 20.0),
          labels = Some(
            Array(
              Span.styled("-20", Style.DEFAULT.add_modifier(Modifier.BOLD)),
              Span.nostyle("0"),
              Span.styled("20", Style.DEFAULT.add_modifier(Modifier.BOLD))
            )
          )
        )
      )
      f.render_widget(chart, chunks0(1))
    }
  }

  def draw_text(f: Frame, area: Rect): Unit = {
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
        Span.styled("notice", Style.DEFAULT.add_modifier(Modifier.ITALIC)),
        Span.nostyle(" you can "),
        Span.styled("automatically", Style.DEFAULT.add_modifier(Modifier.BOLD)),
        Span.nostyle(" "),
        Span.styled("wrap", Style.DEFAULT.add_modifier(Modifier.REVERSED)),
        Span.nostyle(" your "),
        Span.styled("text", Style.DEFAULT.add_modifier(Modifier.UNDERLINED)),
        Span.nostyle(".")
      ),
      Spans.nostyle(
        "One more thing is that it should display unicode characters: 10€"
      )
    )
    val block = {
      val titleStyle = Style.DEFAULT
        .fg(Color.Magenta)
        .add_modifier(Modifier.BOLD)
      BlockWidget(borders = Borders.ALL, title = Some(Spans.from(Span.styled("Footer", titleStyle))))
    }
    val paragraph = ParagraphWidget(text = text, block = Some(block), wrap = Some(ParagraphWidget.Wrap(trim = true)))
    f.render_widget(paragraph, area)
  }

  def draw_second_tab(f: Frame, app: App, area: Rect): Unit = {
    def cell(str: String) = TableWidget.Cell(Text.nostyle(str))
    val chunks = Layout(constraints = Array(Constraint.Percentage(30), Constraint.Percentage(70)), direction = Direction.Horizontal).split(area)
    val up_style = Style.DEFAULT.fg(Color.Green)
    val failure_style = Style.DEFAULT.fg(Color.Red).add_modifier(Modifier.RAPID_BLINK | Modifier.CROSSED_OUT)
    val rows = app.servers.map { s =>
      val style = if (s.status == "Up") up_style else failure_style
      TableWidget.Row(cells = Array(s.name, s.location, s.status).map(cell), style = style)
    }
    val table = TableWidget(
      rows = rows,
      header = Some(
        TableWidget.Row(cells = Array("Server", "Location", "Status").map(cell), style = Style.DEFAULT.fg(Color.Yellow), bottom_margin = 1)
      ),
      block = Some(BlockWidget(title = Some(Spans.nostyle("Servers")), borders = Borders.ALL)),
      widths = Array(Constraint.Length(15), Constraint.Length(15), Constraint.Length(10))
    )
    f.render_widget(table, chunks(0))

    val map = CanvasWidget(
      block = Some(BlockWidget(title = Some(Spans.nostyle("World")), borders = Borders.ALL)),
      marker = if (app.enhanced_graphics) symbols.Marker.Braille else symbols.Marker.Dot,
      x_bounds = Point(-180.0, 180.0),
      y_bounds = Point(-90.0, 90.0)
    ) { ctx =>
      ctx.draw(
        WorldMap(
          resolution = MapResolution.High,
          color = Color.White
        )
      )
      ctx.layer()
      ctx.draw(
        Rectangle(
          x = 0.0,
          y = 30.0,
          width = 10.0,
          height = 10.0,
          color = Color.Yellow
        )
      )
      app.servers.zipWithIndex.foreach { case (s1, i) =>
        app.servers.drop(i).foreach { s2 =>
          ctx.draw(
            Line(
              x1 = s1.coords.y,
              y1 = s1.coords.x,
              y2 = s2.coords.x,
              x2 = s2.coords.y,
              color = Color.Yellow
            )
          );
        }
      }
      app.servers.foreach { server =>
        val color = if (server.status == "Up") Color.Green else Color.Red
        ctx.print(
          server.coords.y,
          server.coords.x,
          Spans.from(Span.styled("X", Style.DEFAULT.fg(color)))
        );
      }
    }
    f.render_widget(map, chunks(1))
  }

  def draw_third_tab(f: Frame, area: Rect): Unit = {
    val chunks = Layout(direction = Direction.Horizontal, constraints = Array(Constraint.Ratio(1, 2), Constraint.Ratio(1, 2))).split(area)
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
    val items: Array[TableWidget.Row] = colors.map { c =>
      val cells = Array(
        TableWidget.Cell(Text.nostyle(c.toString)), // todo: replicate whatever debug was if necessary
        TableWidget.Cell(Text.from(Span.styled("Foreground", Style.DEFAULT.fg(c)))),
        TableWidget.Cell(Text.from(Span.styled("Background", Style.DEFAULT.bg(c))))
      )
      TableWidget.Row(cells)
    }

    val table = TableWidget(
      rows = items,
      block = Some(BlockWidget(title = Some(Spans.nostyle("Colors")), borders = Borders.ALL)),
      widths = Array(
        Constraint.Ratio(1, 3),
        Constraint.Ratio(1, 3),
        Constraint.Ratio(1, 3)
      )
    )
    f.render_widget(table, chunks(0))
  }
}
