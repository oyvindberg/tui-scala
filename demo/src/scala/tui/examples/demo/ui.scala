package tui
package examples
package demo

import tui.widgets._
import tui.widgets.canvas._
import tui.widgets.list.ListItem
import tui.widgets.paragraph.{Paragraph, Wrap}
import tui.widgets.tabs.Tabs

object ui {
  def draw(f: Frame, app: App): Unit = {
    val chunks = Layout(constraints = Array(Constraint.Length(3), Constraint.Min(0))).split(f.size);
    val titles = app.tabs.titles.map(t => Spans.from(Span.styled(t, Style.DEFAULT.fg(Color.Green))))

    val tabs = Tabs(
      titles = titles,
      block = Some(Block(borders = Borders.ALL, title = Some(Spans.from(app.title)))),
      highlight_style = Style.DEFAULT.fg(Color.Yellow),
      selected = app.tabs.index
    )
    f.render_widget(tabs, chunks(0));
    app.tabs.index match {
      case 0 => draw_first_tab(f, app, chunks(1))
      case 1 => draw_second_tab(f, app, chunks(1))
      case 2 => draw_third_tab(f, chunks(1))
      case _ =>
    };
  }

  def draw_first_tab(f: Frame, app: App, area: Rect): Unit = {
    val chunks = Layout(constraints = Array(Constraint.Length(9), Constraint.Min(8), Constraint.Length(7))).split(area);
    draw_gauges(f, app, chunks(0));
    draw_charts(f, app, chunks(1));
    draw_text(f, chunks(2));
  }

  def draw_gauges(f: Frame, app: App, area: Rect): Unit = {
    val chunks = Layout(constraints = Array(Constraint.Length(2), Constraint.Length(3), Constraint.Length(1)), margin = Margin(1)).split(area);
    val block = Block(borders = Borders.ALL, title = Some(Spans.from("Graphs")))
    f.render_widget(block, area);

    val label = "%.2f".format(app.progress * 100.0)
    val gauge = Gauge(
      block = Some(Block(title = Some(Spans.from("Gauge:")))),
      gauge_style = Style.DEFAULT.fg(Color.Magenta).bg(Color.Black).add_modifier(Modifier.ITALIC | Modifier.BOLD),
      label = Some(Span.from(label)),
      ratio = Ratio(app.progress)
    )
    f.render_widget(gauge, chunks(0));

    val sparkline = Sparkline(
      block = Some(Block(title = Some(Spans.from("Sparkline:")))),
      style = Style.DEFAULT.fg(Color.Green),
      data = app.sparkline.points,
      bar_set = if (app.enhanced_graphics) symbols.bar.NINE_LEVELS else symbols.bar.THREE_LEVELS
    )
    f.render_widget(sparkline, chunks(1));

    val line_gauge = LineGauge(
      block = Some(Block(title = Some(Spans.from("LineGauge:")))),
      gauge_style = Style.DEFAULT.fg(Color.Magenta),
      line_set = if (app.enhanced_graphics) symbols.line.THICK else symbols.line.NORMAL,
      ratio = Ratio(app.progress)
    )
    f.render_widget(line_gauge, chunks(2));
  }

  def draw_charts(f: Frame, app: App, area: Rect): Unit = {
    val constraints: Array[Constraint] =
      if (app.show_chart) Array(Constraint.Percentage(50), Constraint.Percentage(50))
      else Array(Constraint.Percentage(100))

    val chunks0 = Layout(constraints = constraints, direction = Direction.Horizontal).split(area);
    {
      val chunks1 = Layout(constraints = Array(Constraint.Percentage(50), Constraint.Percentage(50))).split(chunks0(0));
      {
        val chunks2 = Layout(constraints = Array(Constraint.Percentage(50), Constraint.Percentage(50)), direction = Direction.Horizontal).split(chunks1(0));

        // Draw tasks
        val items: Array[ListItem] = app.tasks.items
          .map(i => ListItem(content = Text.from(i)))
          .toArray
        val tasks = tui.widgets.list.List(
          items = items,
          block = Some(Block(borders = Borders.ALL, title = Some(Spans.from("List")))),
          highlight_style = Style.DEFAULT.add_modifier(Modifier.BOLD),
          highlight_symbol = Some("> ")
        )
        f.render_stateful_widget(tasks, chunks2(0))(app.tasks.state);

        val logMessages = app.logs.items.map { case (evt, level) =>
          val s = level match {
            case "ERROR"    => Style.DEFAULT.fg(Color.Magenta)
            case "CRITICAL" => Style.DEFAULT.fg(Color.Red)
            case "WARNING"  => Style.DEFAULT.fg(Color.Yellow)
            case _          => Style.DEFAULT.fg(Color.Blue)
          }
          val content = Array(Span.styled(level.padTo(9, ' '), s), Span.raw(evt))
          ListItem(content = Text.from(content))
        }

        val logs = tui.widgets.list.List(
          items = logMessages.toArray,
          block = Some(Block(borders = Borders.ALL, title = Some(Spans.from("List"))))
        )
        f.render_stateful_widget(logs, chunks2(1))(app.logs.state);
      }

      val barchart = BarChart(
        block = Some(Block(borders = Borders.ALL, title = Some(Spans.from("Bar chart")))),
        data = app.barchart.toArray,
        bar_width = 3,
        bar_gap = 2,
        bar_set = if (app.enhanced_graphics) symbols.bar.NINE_LEVELS else symbols.bar.THREE_LEVELS,
        value_style = Style.DEFAULT.fg(Color.Black).bg(Color.Green).add_modifier(Modifier.ITALIC),
        label_style = Style.DEFAULT.fg(Color.Yellow),
        bar_style = Style.DEFAULT.fg(Color.Green)
      )
      f.render_widget(barchart, chunks1(1));
    }
    if (app.show_chart) {
      val x_labels = Array(
        Span.styled(app.signals.window.x.toString, Style.DEFAULT.add_modifier(Modifier.BOLD)),
        Span.raw(((app.signals.window.x + app.signals.window.y) / 2.0).toString),
        Span.styled(app.signals.window.y.toString, Style.DEFAULT.add_modifier(Modifier.BOLD))
      )
      val datasets = Array(
        Dataset(name = "data2", marker = symbols.Marker.Dot, style = Style.DEFAULT.fg(Color.Cyan), data = app.signals.sin1.points),
        Dataset(
          name = "data3",
          marker = if (app.enhanced_graphics) symbols.Marker.Braille else symbols.Marker.Dot,
          style = Style.DEFAULT.fg(Color.Yellow),
          data = app.signals.sin2.points
        )
      );
      val chart = Chart(
        datasets = datasets,
        block = {
          val title = Span.styled("Chart", Style.DEFAULT.fg(Color.Cyan).add_modifier(Modifier.BOLD))
          Some(Block(title = Some(Spans.from(title)), borders = Borders.ALL))
        },
        x_axis = Axis(
          title = Some(Spans.from("X Axis")),
          style = Style.DEFAULT.fg(Color.Gray),
          bounds = app.signals.window,
          labels = Some(x_labels)
        ),
        y_axis = Axis(
          title = Some(Spans.from("Y Axis")),
          style = Style.DEFAULT.fg(Color.Gray),
          bounds = Point(-20.0, 20.0),
          labels = Some(
            Array(
              Span.styled("-20", Style.DEFAULT.add_modifier(Modifier.BOLD)),
              Span.raw("0"),
              Span.styled("20", Style.DEFAULT.add_modifier(Modifier.BOLD))
            )
          )
        )
      )
      f.render_widget(chart, chunks0(1));
    }
  }

  def draw_text(f: Frame, area: Rect): Unit = {
    val text = Array(
      Spans.from("This is a paragraph with several lines. You can change style your text the way you want"),
      Spans.from(""),
      Spans.from(
        Array(
          Span.from("For example: "),
          Span.styled("under", Style.DEFAULT.fg(Color.Red)),
          Span.raw(" "),
          Span.styled("the", Style.DEFAULT.fg(Color.Green)),
          Span.raw(" "),
          Span.styled("rainbow", Style.DEFAULT.fg(Color.Blue)),
          Span.raw(".")
        )
      ),
      Spans.from(
        Array(
          Span.raw("Oh and if you didn't "),
          Span.styled("notice", Style.DEFAULT.add_modifier(Modifier.ITALIC)),
          Span.raw(" you can "),
          Span.styled("automatically", Style.DEFAULT.add_modifier(Modifier.BOLD)),
          Span.raw(" "),
          Span.styled("wrap", Style.DEFAULT.add_modifier(Modifier.REVERSED)),
          Span.raw(" your "),
          Span.styled("text", Style.DEFAULT.add_modifier(Modifier.UNDERLINED)),
          Span.raw(".")
        )
      ),
      Spans.from(
        "One more thing is that it should display unicode characters: 10€"
      )
    );
    val block = {
      val titleStyle = Style.DEFAULT
        .fg(Color.Magenta)
        .add_modifier(Modifier.BOLD)
      Block(borders = Borders.ALL, title = Some(Spans.from(Span.styled("Footer", titleStyle))))
    }
    val paragraph = Paragraph(text = Text(text), block = Some(block), wrap = Some(Wrap(trim = true)))
    f.render_widget(paragraph, area);
  }

  def draw_second_tab(f: Frame, app: App, area: Rect): Unit = {
    val chunks = Layout(constraints = Array(Constraint.Percentage(30), Constraint.Percentage(70)), direction = Direction.Horizontal).split(area);
    val up_style = Style.DEFAULT.fg(Color.Green);
    val failure_style = Style.DEFAULT.fg(Color.Red).add_modifier(Modifier.RAPID_BLINK | Modifier.CROSSED_OUT);
    val rows = app.servers.map { s =>
      val style = if (s.status == "Up") up_style else failure_style;
      Row(cells = Array(s.name, s.location, s.status).map(Cell.from), style = style)
    };
    val table = Table(
      rows = rows,
      header = Some(Row(cells = Array("Server", "Location", "Status").map(Cell.from), style = Style.DEFAULT.fg(Color.Yellow), bottom_margin = 1)),
      block = Some(Block(title = Some(Spans.from("Servers")), borders = Borders.ALL)),
      widths = Array(Constraint.Length(15), Constraint.Length(15), Constraint.Length(10))
    )
    f.render_widget(table, chunks(0));

    val map = Canvas(
      block = Some(Block(title = Some(Spans.from("World")), borders = Borders.ALL)),
      painter = Some { ctx =>
        ctx.draw(
          WorldMap(
            color = Color.White,
            resolution = MapResolution.High
          )
        );
        ctx.layer();
        ctx.draw(
          Rectangle(
            x = 0.0,
            y = 30.0,
            width = 10.0,
            height = 10.0,
            color = Color.Yellow
          )
        );
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
          val color = if (server.status == "Up") Color.Green else Color.Red;
          ctx.print(
            server.coords.y,
            server.coords.x,
            Spans.from(Span.styled("X", Style.DEFAULT.fg(color)))
          );
        }
      },
      marker = if (app.enhanced_graphics) symbols.Marker.Braille else symbols.Marker.Dot,
      x_bounds = Point(-180.0, 180.0),
      y_bounds = Point(-90.0, 90.0)
    )
    f.render_widget(map, chunks(1));
  }

  def draw_third_tab(f: Frame, area: Rect): Unit = {
    val chunks = Layout(direction = Direction.Horizontal, constraints = Array(Constraint.Ratio(1, 2), Constraint.Ratio(1, 2))).split(area);
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
    );
    val items: Array[Row] = colors.map { c =>
      val cells = Array(
        Cell.from(Span.raw(c.toString)), // todo: replicate whatever debug was if necessary
        Cell.from(Span.styled("Foreground", Style.DEFAULT.fg(c))),
        Cell.from(Span.styled("Background", Style.DEFAULT.bg(c)))
      );
      Row(cells)

    }

    val table = Table(
      rows = items,
      block = Some(Block(title = Some(Spans.from("Colors")), borders = Borders.ALL)),
      widths = Array(
        Constraint.Ratio(1, 3),
        Constraint.Ratio(1, 3),
        Constraint.Ratio(1, 3)
      )
    );
    f.render_widget(table, chunks(0));
  }
}
