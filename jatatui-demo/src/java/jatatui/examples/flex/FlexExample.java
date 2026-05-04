package jatatui.examples.flex;

import jatatui.core.buffer.Buffer;
import jatatui.core.buffer.Cell;
import jatatui.core.layout.Constraint;
import jatatui.core.layout.Flex;
import jatatui.core.layout.HorizontalAlignment;
import jatatui.core.layout.Layout;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import jatatui.core.style.palette.Tailwind;
import jatatui.core.symbols.Border;
import jatatui.core.symbols.Line;
import jatatui.core.terminal.Terminal;
import jatatui.core.text.Text;
import jatatui.core.widgets.Widget;
import jatatui.crossterm.CrosstermBackend;
import jatatui.crossterm.Jatatui;
import jatatui.widgets.block.Block;
import jatatui.widgets.paragraph.Paragraph;
import jatatui.widgets.scrollbar.Scrollbar;
import jatatui.widgets.scrollbar.ScrollbarOrientation;
import jatatui.widgets.scrollbar.ScrollbarState;
import jatatui.widgets.tabs.Tabs;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Event;
import tui.crossterm.KeyCode;
import tui.crossterm.KeyEventKind;

/// A jatatui example that demonstrates different types of flex layouts.
///
/// Java port of `examples/apps/flex/src/main.rs` from ratatui v0.30. Use ◄ ► to change tabs,
/// ▲ ▼ to scroll, and - + to change spacing. Press 'q' or Esc to quit.
public final class FlexExample {

  private static final CrosstermJni JNI = new CrosstermJni();

  /// One row in the demo: a description and a set of constraints to lay out horizontally.
  record ExampleData(String description, List<Constraint> constraints) {}

  private static final List<ExampleData> EXAMPLE_DATA = buildExampleData();

  private static List<ExampleData> buildExampleData() {
    List<ExampleData> out = new ArrayList<>();
    out.add(
        new ExampleData(
            "Min(u16) takes any excess space always",
            List.of(
                new Constraint.Length(10),
                new Constraint.Min(10),
                new Constraint.Max(10),
                new Constraint.Percentage(10),
                new Constraint.Ratio(1, 10))));
    out.add(
        new ExampleData(
            "Fill(u16) takes any excess space always",
            List.of(
                new Constraint.Length(20),
                new Constraint.Percentage(20),
                new Constraint.Ratio(1, 5),
                new Constraint.Fill(1))));
    out.add(
        new ExampleData(
            "Here's all constraints in one line",
            List.of(
                new Constraint.Length(10),
                new Constraint.Min(10),
                new Constraint.Max(10),
                new Constraint.Percentage(10),
                new Constraint.Ratio(1, 10),
                new Constraint.Fill(1))));
    out.add(new ExampleData("", List.of(new Constraint.Max(50), new Constraint.Min(50))));
    out.add(new ExampleData("", List.of(new Constraint.Max(20), new Constraint.Length(10))));
    out.add(new ExampleData("", List.of(new Constraint.Max(20), new Constraint.Length(10))));
    out.add(
        new ExampleData(
            "Min grows always but also allows Fill to grow",
            List.of(
                new Constraint.Percentage(50),
                new Constraint.Fill(1),
                new Constraint.Fill(2),
                new Constraint.Min(50))));
    out.add(
        new ExampleData(
            "In `Legacy`, the last constraint of lowest priority takes excess space",
            List.of(
                new Constraint.Length(20),
                new Constraint.Length(20),
                new Constraint.Percentage(20))));
    out.add(
        new ExampleData(
            "",
            List.of(
                new Constraint.Length(20),
                new Constraint.Percentage(20),
                new Constraint.Length(20))));
    out.add(
        new ExampleData(
            "A lowest priority constraint will be broken before a high priority constraint",
            List.of(new Constraint.Ratio(1, 4), new Constraint.Percentage(20))));
    out.add(
        new ExampleData(
            "`Length` is higher priority than `Percentage`",
            List.of(new Constraint.Percentage(20), new Constraint.Length(10))));
    out.add(
        new ExampleData(
            "`Min/Max` is higher priority than `Length`",
            List.of(new Constraint.Length(10), new Constraint.Max(20))));
    out.add(new ExampleData("", List.of(new Constraint.Length(100), new Constraint.Min(20))));
    out.add(
        new ExampleData(
            "`Length` is higher priority than `Min/Max`",
            List.of(new Constraint.Max(20), new Constraint.Length(10))));
    out.add(new ExampleData("", List.of(new Constraint.Min(20), new Constraint.Length(90))));
    out.add(
        new ExampleData(
            "Fill is the lowest priority and will fill any excess space",
            List.of(new Constraint.Fill(1), new Constraint.Ratio(1, 4))));
    out.add(
        new ExampleData(
            "Fill can be used to scale proportionally with other Fill blocks",
            List.of(
                new Constraint.Fill(1), new Constraint.Percentage(20), new Constraint.Fill(2))));
    out.add(
        new ExampleData(
            "",
            List.of(
                new Constraint.Ratio(1, 3),
                new Constraint.Percentage(20),
                new Constraint.Ratio(2, 3))));
    out.add(
        new ExampleData(
            "Legacy will stretch the last lowest priority constraint\n"
                + "Stretch will only stretch equal weighted constraints",
            List.of(new Constraint.Length(20), new Constraint.Length(15))));
    out.add(new ExampleData("", List.of(new Constraint.Percentage(20), new Constraint.Length(15))));
    out.add(
        new ExampleData(
            "`Fill(u16)` fills up excess space, but is lower priority to spacers.\n"
                + "i.e. Fill will only have widths in Flex::Stretch and Flex::Legacy",
            List.of(new Constraint.Fill(1), new Constraint.Fill(1))));
    out.add(new ExampleData("", List.of(new Constraint.Length(20), new Constraint.Length(20))));
    out.add(
        new ExampleData(
            "When not using `Flex::Stretch` or `Flex::Legacy`,\n"
                + "`Min(u16)` and `Max(u16)` collapse to their lowest values",
            List.of(new Constraint.Min(20), new Constraint.Max(20))));
    out.add(new ExampleData("", List.of(new Constraint.Max(20))));
    out.add(
        new ExampleData(
            "",
            List.of(
                new Constraint.Min(20),
                new Constraint.Max(20),
                new Constraint.Length(20),
                new Constraint.Length(20))));
    out.add(new ExampleData("", List.of(new Constraint.Fill(0), new Constraint.Fill(0))));
    out.add(
        new ExampleData(
            "`Fill(1)` can be to scale with respect to other `Fill(2)`",
            List.of(new Constraint.Fill(1), new Constraint.Fill(2))));
    out.add(
        new ExampleData(
            "",
            List.of(
                new Constraint.Fill(1),
                new Constraint.Min(10),
                new Constraint.Max(10),
                new Constraint.Fill(2))));
    out.add(
        new ExampleData(
            "`Fill(0)` collapses if there are other non-zero `Fill(_)`\n"
                + "constraints. e.g. `[Fill(0), Fill(0), Fill(1)]`:",
            List.of(new Constraint.Fill(0), new Constraint.Fill(0), new Constraint.Fill(1))));
    return List.copyOf(out);
  }

  enum SelectedTab {
    Legacy,
    Start,
    Center,
    End,
    SpaceAround,
    SpaceEvenly,
    SpaceBetween;

    SelectedTab previous() {
      int prev = Math.max(0, ordinal() - 1);
      return values()[prev];
    }

    SelectedTab next() {
      int next = Math.min(values().length - 1, ordinal() + 1);
      return values()[next];
    }

    Flex flex() {
      return switch (this) {
        case Legacy -> Flex.Legacy;
        case Start -> Flex.Start;
        case Center -> Flex.Center;
        case End -> Flex.End;
        case SpaceAround -> Flex.SpaceAround;
        case SpaceEvenly -> Flex.SpaceEvenly;
        case SpaceBetween -> Flex.SpaceBetween;
      };
    }

    jatatui.core.text.Line toTabTitle() {
      Color color =
          switch (this) {
            case Legacy -> Tailwind.ORANGE.c400();
            case Start -> Tailwind.SKY.c400();
            case Center -> Tailwind.SKY.c300();
            case End -> Tailwind.SKY.c200();
            case SpaceEvenly -> Tailwind.INDIGO.c400();
            case SpaceBetween -> Tailwind.INDIGO.c300();
            case SpaceAround -> Tailwind.INDIGO.c500();
          };
      String text = " " + name() + " ";
      return jatatui.core.text.Line.styled(text, Style.empty().withFg(color).withBg(Color.BLACK));
    }
  }

  enum AppState {
    Running,
    Quit
  }

  private FlexExample() {}

  public static void main(String[] args) throws IOException {
    Jatatui.runIo(terminal -> new App().run(terminal));
  }

  static final class App implements Widget {
    SelectedTab selectedTab = SelectedTab.Legacy;
    int scrollOffset = 0;
    int spacing = 0;
    AppState state = AppState.Running;

    void run(Terminal<CrosstermBackend> terminal) throws IOException {
      // Increase the layout cache to account for the number of layout events. The user changes
      // spacing about 100 times in a typical session.
      int cacheSize = EXAMPLE_DATA.size() * SelectedTab.values().length * 100;
      Layout.initCache(cacheSize);

      while (state == AppState.Running) {
        terminal.draw(frame -> frame.renderWidget(this, frame.area()));
        handleEvents();
      }
    }

    private void handleEvents() throws IOException {
      Event event = JNI.read();
      if (!(event instanceof Event.Key key) || key.keyEvent().kind() != KeyEventKind.Press) {
        return;
      }
      KeyCode code = key.keyEvent().code();
      if (code instanceof KeyCode.Char ch) {
        switch (ch.c()) {
          case 'q' -> state = AppState.Quit;
          case 'l' -> selectedTab = selectedTab.next();
          case 'h' -> selectedTab = selectedTab.previous();
          case 'j' -> down();
          case 'k' -> up();
          case 'g' -> scrollOffset = 0;
          case 'G' -> scrollOffset = maxScrollOffset();
          case '+' -> spacing = Math.min(Integer.MAX_VALUE, spacing + 1);
          case '-' -> spacing = Math.max(0, spacing - 1);
          default -> {}
        }
      } else if (code instanceof KeyCode.Esc) {
        state = AppState.Quit;
      } else if (code instanceof KeyCode.Right) {
        selectedTab = selectedTab.next();
      } else if (code instanceof KeyCode.Left) {
        selectedTab = selectedTab.previous();
      } else if (code instanceof KeyCode.Down) {
        down();
      } else if (code instanceof KeyCode.Up) {
        up();
      } else if (code instanceof KeyCode.Home) {
        scrollOffset = 0;
      } else if (code instanceof KeyCode.End) {
        scrollOffset = maxScrollOffset();
      }
    }

    private void up() {
      scrollOffset = Math.max(0, scrollOffset - 1);
    }

    private void down() {
      scrollOffset = Math.min(maxScrollOffset(), scrollOffset + 1);
    }

    @Override
    public void render(Rect area, Buffer buf) {
      Rect[] split =
          area.layout(
              Layout.vertical(
                  new Constraint.Length(3), new Constraint.Length(1), new Constraint.Fill(0)),
              3);
      Rect tabsArea = split[0];
      Rect axisArea = split[1];
      Rect demoArea = split[2];

      tabs().render(tabsArea, buf);
      boolean scrollNeeded = renderDemo(demoArea, buf);
      int axisWidth = scrollNeeded ? Math.max(0, axisArea.width() - 1) : axisArea.width();
      axis(axisWidth, spacing).render(axisArea, buf);
    }

    private Widget tabs() {
      List<jatatui.core.text.Line> tabTitles = new ArrayList<>();
      for (SelectedTab t : SelectedTab.values()) {
        tabTitles.add(t.toTabTitle());
      }
      Block block =
          Block.empty()
              .withTitle(jatatui.core.text.Line.styled("Flex Layouts ", Style.empty().bold()))
              .withTitle(
                  jatatui.core.text.Line.from(
                      " Use ◄ ► to change tab, ▲ ▼  to scroll, - + to change spacing "));
      return Tabs.of(tabTitles)
          .withBlock(block)
          .withHighlightStyle(Style.fromModifier(Modifier.REVERSED))
          .withSelected(selectedTab.ordinal())
          .withDivider(" ")
          .withPadding("", "");
    }

    /// A bar like `<----- 80 px (gap: 2 px)? ----->`.
    private static Widget axis(int width, int spacing) {
      String label = spacing != 0 ? width + " px (gap: " + spacing + " px)" : width + " px";
      int barWidth = Math.max(0, width - 2); // we want `<` and `>` at the ends
      String widthBar = "<" + center(label, '-', barWidth) + ">";
      return Paragraph.of(jatatui.core.text.Line.styled(widthBar, Style.empty().darkGray()))
          .centered();
    }

    /// Render the demo content. Returns `true` iff the scrollbar was rendered.
    private boolean renderDemo(Rect area, Buffer buf) {
      int height = exampleHeight();
      Rect demoArea = new Rect(0, 0, area.width(), height);
      Buffer demoBuf = Buffer.empty(demoArea);

      boolean scrollbarNeeded = scrollOffset != 0 || height > area.height();
      Rect contentArea =
          scrollbarNeeded
              ? new Rect(demoArea.x(), demoArea.y(), demoArea.width() - 1, demoArea.height())
              : demoArea;

      renderExamples(contentArea, demoBuf, selectedTab.flex(), spacing);

      Cell[] content = demoBuf.content();
      int skip = area.width() * scrollOffset;
      int take = (int) Math.min(area.area(), Math.max(0, content.length - skip));
      for (int k = 0; k < take; k++) {
        Cell cell = content[skip + k];
        int x = k % area.width();
        int y = k / area.width();
        Cell target = buf.cellAt(area.x() + x, area.y() + y);
        target
            .setSymbol(cell.symbol())
            .setFg(cell.fg)
            .setBg(cell.bg)
            .setUnderlineColor(cell.underlineColor)
            .setStyle(cell.style())
            .setSkip(cell.skip);
      }

      if (scrollbarNeeded) {
        Rect scrollbarArea = area.intersection(buf.area);
        ScrollbarState state = ScrollbarState.of(maxScrollOffset()).withPosition(scrollOffset);
        Scrollbar.of(ScrollbarOrientation.VerticalRight).render(scrollbarArea, buf, state);
      }
      return scrollbarNeeded;
    }
  }

  /// When scrolling, make sure we don't scroll past the last example.
  private static int maxScrollOffset() {
    int last =
        EXAMPLE_DATA.isEmpty()
            ? 0
            : getDescriptionHeight(EXAMPLE_DATA.get(EXAMPLE_DATA.size() - 1).description()) + 4;
    return Math.max(0, exampleHeight() - last);
  }

  /// The height of all examples combined.
  private static int exampleHeight() {
    int sum = 0;
    for (ExampleData ed : EXAMPLE_DATA) {
      sum += getDescriptionHeight(ed.description()) + 4;
    }
    return sum;
  }

  private static int getDescriptionHeight(String s) {
    if (s.isEmpty()) {
      return 0;
    }
    return s.split("\n", -1).length;
  }

  private static void renderExamples(Rect area, Buffer buf, Flex flex, int spacing) {
    List<Constraint> heights = new ArrayList<>(EXAMPLE_DATA.size());
    for (ExampleData ed : EXAMPLE_DATA) {
      heights.add(new Constraint.Length(getDescriptionHeight(ed.description()) + 4));
    }
    Rect[] areas = Layout.vertical(heights).withFlex(Flex.Start).split(area);
    for (int i = 0; i < areas.length && i < EXAMPLE_DATA.size(); i++) {
      ExampleData ed = EXAMPLE_DATA.get(i);
      new Example(ed.constraints(), ed.description(), flex, spacing).render(areas[i], buf);
    }
  }

  /// A single example row: title plus a set of horizontal blocks separated by spacers.
  static final class Example implements Widget {
    private final List<Constraint> constraints;
    private final String description;
    private final Flex flex;
    private final int spacing;

    Example(List<Constraint> constraints, String description, Flex flex, int spacing) {
      this.constraints = List.copyOf(constraints);
      this.description = description;
      this.flex = flex;
      this.spacing = spacing;
    }

    @Override
    public void render(Rect area, Buffer buf) {
      int titleHeight = getDescriptionHeight(description);
      Rect[] split =
          area.layout(
              Layout.vertical(new Constraint.Length(titleHeight), new Constraint.Fill(0)), 2);
      Rect title = split[0];
      Rect illustrations = split[1];

      Layout.SplitResult sr =
          Layout.horizontal(constraints)
              .withFlex(flex)
              .withSpacing(spacing)
              .splitWithSpacers(illustrations);
      Rect[] blocks = sr.segments();
      Rect[] spacers = sr.spacers();

      if (!description.isEmpty()) {
        List<jatatui.core.text.Line> lines = new ArrayList<>();
        for (String line : description.split("\n", -1)) {
          jatatui.core.text.Line styled =
              jatatui.core.text.Line.styled(
                  "// " + line, Style.empty().italic().withFg(Tailwind.SLATE.c400()));
          lines.add(styled);
        }
        Paragraph.of(lines).render(title, buf);
      }

      for (int i = 0; i < blocks.length && i < constraints.size(); i++) {
        illustration(constraints.get(i), blocks[i].width()).render(blocks[i], buf);
      }

      for (Rect spacer : spacers) {
        renderSpacer(spacer, buf);
      }
    }

    private static void renderSpacer(Rect spacer, Buffer buf) {
      if (spacer.width() > 1) {
        Border.Set cornersOnly =
            new Border.Set(
                Line.NORMAL.topLeft(),
                Line.NORMAL.topRight(),
                Line.NORMAL.bottomLeft(),
                Line.NORMAL.bottomRight(),
                " ",
                " ",
                " ",
                " ");
        Block.bordered()
            .withBorderSet(cornersOnly)
            .withBorderStyle(Style.reset().darkGray())
            .render(spacer, buf);
      } else {
        Paragraph.of(
                Text.from(
                    jatatui.core.text.Line.from(""),
                    jatatui.core.text.Line.from("│"),
                    jatatui.core.text.Line.from("│"),
                    jatatui.core.text.Line.from("")))
            .withStyle(Style.reset().darkGray())
            .render(spacer, buf);
      }
      int width = spacer.width();
      String label;
      if (width > 4) {
        label = width + " px";
      } else if (width > 2) {
        label = String.valueOf(width);
      } else {
        label = "";
      }
      Text text =
          Text.from(
              jatatui.core.text.Line.raw(""),
              jatatui.core.text.Line.raw(""),
              jatatui.core.text.Line.styled(label, Style.reset().darkGray()));
      Paragraph.of(text)
          .withStyle(Style.reset().darkGray())
          .withAlignment(HorizontalAlignment.Center)
          .render(spacer, buf);
    }

    private static Widget illustration(Constraint constraint, int width) {
      Color mainColor = colorForConstraint(constraint);
      Color fgColor = Color.WHITE;
      String title = describe(constraint);
      String content = width + " px";
      String text = title + "\n" + content;
      Block block =
          Block.bordered()
              .withBorderSet(Border.QUADRANT_OUTSIDE)
              .withBorderStyle(Style.reset().withFg(mainColor).reversed())
              .withStyle(Style.empty().withFg(fgColor).withBg(mainColor));
      return Paragraph.of(text).centered().withBlock(block);
    }
  }

  private static Color colorForConstraint(Constraint c) {
    return switch (c) {
      case Constraint.Min m -> Tailwind.BLUE.c900();
      case Constraint.Max m -> Tailwind.BLUE.c800();
      case Constraint.Length l -> Tailwind.SLATE.c700();
      case Constraint.Percentage p -> Tailwind.SLATE.c800();
      case Constraint.Ratio r -> Tailwind.SLATE.c900();
      case Constraint.Fill f -> Tailwind.SLATE.c950();
    };
  }

  /// Mirrors Rust's `format!("{constraint}")`.
  static String describe(Constraint c) {
    return switch (c) {
      case Constraint.Length l -> "Length(" + l.v() + ")";
      case Constraint.Percentage p -> "Percentage(" + p.v() + ")";
      case Constraint.Ratio r -> "Ratio(" + r.numerator() + ", " + r.denominator() + ")";
      case Constraint.Fill f -> "Fill(" + f.v() + ")";
      case Constraint.Min m -> "Min(" + m.v() + ")";
      case Constraint.Max m -> "Max(" + m.v() + ")";
    };
  }

  private static String center(String s, char pad, int width) {
    if (s.length() >= width) return s;
    int total = width - s.length();
    int left = total / 2;
    int right = total - left;
    char[] padded = new char[width];
    Arrays.fill(padded, pad);
    System.arraycopy(s.toCharArray(), 0, padded, left, s.length());
    return new String(padded);
  }
}
