package tuiexamples;

import tui.Alignment;
import tui.Borders;
import tui.Color;
import tui.Constraint;
import tui.Direction;
import tui.Frame;
import tui.Layout;
import tui.Margin;
import tui.Rect;
import tui.Spans;
import tui.Style;
import tui.Terminal;
import tui.Text;
import tui.WithTerminal;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Event;
import tui.crossterm.KeyCode;
import tui.widgets.BlockWidget;
import tui.widgets.ParagraphWidget;

public final class LayoutExample {
  private LayoutExample() {}

  public static void main(String[] args) {
    WithTerminal.apply((jni, terminal) -> {
      runApp(terminal, jni);
      return null;
    });
  }

  public static void runApp(Terminal terminal, CrosstermJni jni) {
    while (true) {
      terminal.draw(LayoutExample::ui);
      Event ev = jni.read();
      if (ev instanceof Event.Key key) {
        if (key.keyEvent().code() instanceof KeyCode.Char c && c.c() == 'q') {
          return;
        }
      }
    }
  }

  // The five constraint kinds, with six values each.
  // Order: Length, Min, Max, Percentage, Ratio (matches upstream `examples/layout.rs`).
  private static final String[] EXAMPLE_NAMES = {"Len", "Min", "Max", "Perc", "Ratio"};
  private static final Constraint[][] EXAMPLE_VALUES = {
    {
      new Constraint.Length(0),
      new Constraint.Length(2),
      new Constraint.Length(3),
      new Constraint.Length(6),
      new Constraint.Length(10),
      new Constraint.Length(15)
    },
    {
      new Constraint.Min(0),
      new Constraint.Min(2),
      new Constraint.Min(3),
      new Constraint.Min(6),
      new Constraint.Min(10),
      new Constraint.Min(15)
    },
    {
      new Constraint.Max(0),
      new Constraint.Max(2),
      new Constraint.Max(3),
      new Constraint.Max(6),
      new Constraint.Max(10),
      new Constraint.Max(15)
    },
    {
      new Constraint.Percentage(0),
      new Constraint.Percentage(25),
      new Constraint.Percentage(50),
      new Constraint.Percentage(75),
      new Constraint.Percentage(100),
      new Constraint.Percentage(150)
    },
    {
      new Constraint.Ratio(0, 4),
      new Constraint.Ratio(1, 4),
      new Constraint.Ratio(2, 4),
      new Constraint.Ratio(3, 4),
      new Constraint.Ratio(4, 4),
      new Constraint.Ratio(6, 4)
    }
  };

  public static void ui(Frame frame) {
    Layout main =
        new Layout(
            Direction.Vertical,
            Margin.of(0),
            new Constraint[] {
              new Constraint.Length(4),
              new Constraint.Length(50),
              new Constraint.Min(0)
            },
            true);
    Rect[] mainChunks = main.split(frame.size);

    ParagraphWidget title =
        ParagraphWidget.empty(
            Text.fromMany(
                Spans.styled(
                    "Horizontal Layout Example. Press q to quit",
                    Style.empty().withFg(Color.DarkGray)),
                Spans.nostyle(
                    "Each line has 2 constraints, plus Min(0) to fill the remaining space."),
                Spans.nostyle(
                    "E.g. the second line of the Len/Min box is [Length(2), Min(2), Min(0)]"),
                Spans.nostyle("Note: constraint labels that don't fit are truncated")));
    frame.renderWidget(title, mainChunks[0]);

    Layout rows =
        new Layout(
            Direction.Vertical,
            Margin.of(0),
            new Constraint[] {
              new Constraint.Length(9),
              new Constraint.Length(9),
              new Constraint.Length(9),
              new Constraint.Length(9),
              new Constraint.Length(9),
              new Constraint.Min(0)
            },
            true);
    Rect[] rowAreas = rows.split(mainChunks[1]);

    Rect[][] exampleAreas = new Rect[5][];
    for (int i = 0; i < 5; i++) {
      Layout row =
          new Layout(
              Direction.Horizontal,
              Margin.of(0),
              new Constraint[] {
                new Constraint.Length(14),
                new Constraint.Length(14),
                new Constraint.Length(14),
                new Constraint.Length(14),
                new Constraint.Length(14),
                new Constraint.Min(0)
              },
              true);
      Rect[] cells = row.split(rowAreas[i]);
      // Drop the trailing Min(0) area.
      Rect[] trimmed = new Rect[5];
      System.arraycopy(cells, 0, trimmed, 0, 5);
      exampleAreas[i] = trimmed;
    }

    // Cartesian product over (row, col) of (kind, kind).
    for (int a = 0; a < EXAMPLE_NAMES.length; a++) {
      for (int b = 0; b < EXAMPLE_NAMES.length; b++) {
        Constraint[] aVals = EXAMPLE_VALUES[a];
        Constraint[] bVals = EXAMPLE_VALUES[b];
        Constraint[][] pairs = new Constraint[aVals.length][];
        for (int k = 0; k < aVals.length; k++) {
          pairs[k] = new Constraint[] {aVals[k], bVals[k]};
        }
        renderExampleCombination(
            frame,
            exampleAreas[a][b],
            EXAMPLE_NAMES[a] + "/" + EXAMPLE_NAMES[b],
            pairs);
      }
    }
  }

  private static void renderExampleCombination(
      Frame frame, Rect area, String title, Constraint[][] pairs) {
    BlockWidget block =
        BlockWidget.empty()
            .withTitle(Spans.styled(title, Style.empty().withFg(Color.Gray)))
            .withBorders(Borders.ALL)
            .withBorderStyle(Style.empty().withFg(Color.DarkGray));
    Rect inner = block.inner(area);
    frame.renderWidget(block, area);

    Constraint[] rowConstraints = new Constraint[pairs.length + 1];
    for (int i = 0; i < rowConstraints.length; i++) rowConstraints[i] = new Constraint.Length(1);
    Layout layout = new Layout(Direction.Vertical, Margin.of(0), rowConstraints, true);
    Rect[] rows = layout.split(inner);
    for (int i = 0; i < pairs.length; i++) {
      renderSingleExample(
          frame,
          rows[i],
          new Constraint[] {pairs[i][0], pairs[i][1], new Constraint.Min(0)});
    }
    // alignment ruler
    if (rows.length > pairs.length) {
      ParagraphWidget ruler =
          ParagraphWidget.empty(Text.nostyle("123456789012")).withAlignment(Alignment.Left);
      frame.renderWidget(ruler, rows[pairs.length]);
    }
  }

  private static void renderSingleExample(Frame frame, Rect area, Constraint[] constraints) {
    Layout layout = new Layout(Direction.Horizontal, Margin.of(0), constraints, true);
    Rect[] chunks = layout.split(area);
    ParagraphWidget red =
        ParagraphWidget.empty(Text.nostyle(constraintLabel(constraints[0])))
            .withStyle(Style.empty().withBg(Color.Red));
    ParagraphWidget blue =
        ParagraphWidget.empty(Text.nostyle(constraintLabel(constraints[1])))
            .withStyle(Style.empty().withBg(Color.Blue));
    ParagraphWidget green =
        ParagraphWidget.empty(Text.nostyle("·".repeat(12)))
            .withStyle(Style.empty().withBg(Color.Green));
    frame.renderWidget(red, chunks[0]);
    frame.renderWidget(blue, chunks[1]);
    frame.renderWidget(green, chunks[2]);
  }

  private static String constraintLabel(Constraint c) {
    return switch (c) {
      case Constraint.Length l -> Integer.toString(l.l());
      case Constraint.Min m -> Integer.toString(m.m());
      case Constraint.Max m -> Integer.toString(m.m());
      case Constraint.Percentage p -> Integer.toString(p.p());
      case Constraint.Ratio r -> r.num() + ":" + r.den();
    };
  }
}
