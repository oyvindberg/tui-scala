package jatatui.examples.constraints;

import jatatui.core.buffer.Buffer;
import jatatui.core.buffer.Cell;
import jatatui.core.layout.Constraint;
import jatatui.core.layout.HorizontalAlignment;
import jatatui.core.layout.Layout;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import jatatui.core.style.palette.Tailwind;
import jatatui.core.symbols.Border;
import jatatui.core.terminal.Terminal;
import jatatui.core.text.Line;
import jatatui.core.widgets.Widget;
import jatatui.crossterm.CrosstermBackend;
import jatatui.crossterm.Jatatui;
import jatatui.widgets.block.Block;
import jatatui.widgets.block.Padding;
import jatatui.widgets.paragraph.Paragraph;
import jatatui.widgets.scrollbar.Scrollbar;
import jatatui.widgets.scrollbar.ScrollbarOrientation;
import jatatui.widgets.scrollbar.ScrollbarState;
import jatatui.widgets.tabs.Tabs;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Event;
import tui.crossterm.KeyCode;
import tui.crossterm.KeyEventKind;

/// A jatatui example that demonstrates different types of constraints.
///
/// Java port of `examples/apps/constraints/src/main.rs` from ratatui v0.30. Use h / l (or arrow
/// keys) to switch tabs and j / k (or arrow keys) to scroll. Press 'q' or Esc to quit.
public final class ConstraintsExample {

  private static final CrosstermJni JNI = new CrosstermJni();

  private static final int SPACER_HEIGHT = 0;
  private static final int ILLUSTRATION_HEIGHT = 4;
  private static final int EXAMPLE_HEIGHT = ILLUSTRATION_HEIGHT + SPACER_HEIGHT;

  // priority 2
  private static final Color MIN_COLOR = Tailwind.BLUE.c900();
  private static final Color MAX_COLOR = Tailwind.BLUE.c800();
  // priority 3
  private static final Color LENGTH_COLOR = Tailwind.SLATE.c700();
  private static final Color PERCENTAGE_COLOR = Tailwind.SLATE.c800();
  private static final Color RATIO_COLOR = Tailwind.SLATE.c900();
  // priority 4
  private static final Color FILL_COLOR = Tailwind.SLATE.c950();

  private ConstraintsExample() {}

  public static void main(String[] args) throws IOException {
    Jatatui.runIo(terminal -> new App().run(terminal));
  }

  /// Tabs for the different examples. Order determines display order.
  enum SelectedTab {
    Min,
    Max,
    Length,
    Percentage,
    Ratio,
    Fill;

    /// Get the previous tab; returns the current tab when already at the start.
    SelectedTab previous() {
      int idx = ordinal();
      int prev = Math.max(0, idx - 1);
      return values()[prev];
    }

    /// Get the next tab; returns the current tab when already at the end.
    SelectedTab next() {
      SelectedTab[] all = values();
      int idx = ordinal();
      int next = Math.min(all.length - 1, idx + 1);
      return all[next];
    }

    int getExampleCount() {
      return switch (this) {
        case Length -> 4;
        case Percentage -> 5;
        case Ratio -> 4;
        case Fill -> 2;
        case Min -> 5;
        case Max -> 5;
      };
    }

    Line toTabTitle() {
      String text = "  " + name() + "  ";
      Color color =
          switch (this) {
            case Length -> LENGTH_COLOR;
            case Percentage -> PERCENTAGE_COLOR;
            case Ratio -> RATIO_COLOR;
            case Fill -> FILL_COLOR;
            case Min -> MIN_COLOR;
            case Max -> MAX_COLOR;
          };
      return Line.styled(text, Style.empty().withFg(Tailwind.SLATE.c200()).withBg(color));
    }

    void render(Rect area, Buffer buf) {
      switch (this) {
        case Length -> renderLengthExample(area, buf);
        case Percentage -> renderPercentageExample(area, buf);
        case Ratio -> renderRatioExample(area, buf);
        case Fill -> renderFillExample(area, buf);
        case Min -> renderMinExample(area, buf);
        case Max -> renderMaxExample(area, buf);
      }
    }

    private static void renderLengthExample(Rect area, Buffer buf) {
      Layout layout =
          Layout.vertical(
              new Constraint.Length(EXAMPLE_HEIGHT),
              new Constraint.Length(EXAMPLE_HEIGHT),
              new Constraint.Length(EXAMPLE_HEIGHT),
              new Constraint.Length(EXAMPLE_HEIGHT));
      Rect[] split = area.layout(layout, 4);
      new Example(List.of(new Constraint.Length(20), new Constraint.Length(20)))
          .render(split[0], buf);
      new Example(List.of(new Constraint.Length(20), new Constraint.Min(20))).render(split[1], buf);
      new Example(List.of(new Constraint.Length(20), new Constraint.Max(20))).render(split[2], buf);
    }

    private static void renderPercentageExample(Rect area, Buffer buf) {
      Layout layout =
          Layout.vertical(
              new Constraint.Length(EXAMPLE_HEIGHT),
              new Constraint.Length(EXAMPLE_HEIGHT),
              new Constraint.Length(EXAMPLE_HEIGHT),
              new Constraint.Length(EXAMPLE_HEIGHT),
              new Constraint.Length(EXAMPLE_HEIGHT),
              new Constraint.Length(EXAMPLE_HEIGHT));
      Rect[] split = area.layout(layout, 6);
      new Example(List.of(new Constraint.Percentage(75), new Constraint.Fill(0)))
          .render(split[0], buf);
      new Example(List.of(new Constraint.Percentage(25), new Constraint.Fill(0)))
          .render(split[1], buf);
      new Example(List.of(new Constraint.Percentage(50), new Constraint.Min(20)))
          .render(split[2], buf);
      new Example(List.of(new Constraint.Percentage(0), new Constraint.Max(0)))
          .render(split[3], buf);
      new Example(List.of(new Constraint.Percentage(0), new Constraint.Fill(0)))
          .render(split[4], buf);
    }

    private static void renderRatioExample(Rect area, Buffer buf) {
      Layout layout =
          Layout.vertical(
              new Constraint.Length(EXAMPLE_HEIGHT),
              new Constraint.Length(EXAMPLE_HEIGHT),
              new Constraint.Length(EXAMPLE_HEIGHT),
              new Constraint.Length(EXAMPLE_HEIGHT),
              new Constraint.Length(EXAMPLE_HEIGHT));
      Rect[] split = area.layout(layout, 5);
      new Example(List.of(new Constraint.Ratio(1, 2), new Constraint.Ratio(1, 2)))
          .render(split[0], buf);
      new Example(
              List.of(
                  new Constraint.Ratio(1, 4),
                  new Constraint.Ratio(1, 4),
                  new Constraint.Ratio(1, 4),
                  new Constraint.Ratio(1, 4)))
          .render(split[1], buf);
      new Example(
              List.of(
                  new Constraint.Ratio(1, 2),
                  new Constraint.Ratio(1, 3),
                  new Constraint.Ratio(1, 4)))
          .render(split[2], buf);
      new Example(
              List.of(
                  new Constraint.Ratio(1, 2),
                  new Constraint.Percentage(25),
                  new Constraint.Length(10)))
          .render(split[3], buf);
    }

    private static void renderFillExample(Rect area, Buffer buf) {
      Rect[] split =
          area.layout(
              Layout.vertical(
                  new Constraint.Length(EXAMPLE_HEIGHT),
                  new Constraint.Length(EXAMPLE_HEIGHT),
                  new Constraint.Length(EXAMPLE_HEIGHT)),
              3);
      new Example(List.of(new Constraint.Fill(1), new Constraint.Fill(2), new Constraint.Fill(3)))
          .render(split[0], buf);
      new Example(
              List.of(
                  new Constraint.Fill(1), new Constraint.Percentage(50), new Constraint.Fill(1)))
          .render(split[1], buf);
    }

    private static void renderMinExample(Rect area, Buffer buf) {
      Layout layout =
          Layout.vertical(
              new Constraint.Length(EXAMPLE_HEIGHT),
              new Constraint.Length(EXAMPLE_HEIGHT),
              new Constraint.Length(EXAMPLE_HEIGHT),
              new Constraint.Length(EXAMPLE_HEIGHT),
              new Constraint.Length(EXAMPLE_HEIGHT),
              new Constraint.Length(EXAMPLE_HEIGHT));
      Rect[] split = area.layout(layout, 6);
      new Example(List.of(new Constraint.Percentage(100), new Constraint.Min(0)))
          .render(split[0], buf);
      new Example(List.of(new Constraint.Percentage(100), new Constraint.Min(20)))
          .render(split[1], buf);
      new Example(List.of(new Constraint.Percentage(100), new Constraint.Min(40)))
          .render(split[2], buf);
      new Example(List.of(new Constraint.Percentage(100), new Constraint.Min(60)))
          .render(split[3], buf);
      new Example(List.of(new Constraint.Percentage(100), new Constraint.Min(80)))
          .render(split[4], buf);
    }

    private static void renderMaxExample(Rect area, Buffer buf) {
      Layout layout =
          Layout.vertical(
              new Constraint.Length(EXAMPLE_HEIGHT),
              new Constraint.Length(EXAMPLE_HEIGHT),
              new Constraint.Length(EXAMPLE_HEIGHT),
              new Constraint.Length(EXAMPLE_HEIGHT),
              new Constraint.Length(EXAMPLE_HEIGHT),
              new Constraint.Length(EXAMPLE_HEIGHT));
      Rect[] split = area.layout(layout, 6);
      new Example(List.of(new Constraint.Percentage(0), new Constraint.Max(0)))
          .render(split[0], buf);
      new Example(List.of(new Constraint.Percentage(0), new Constraint.Max(20)))
          .render(split[1], buf);
      new Example(List.of(new Constraint.Percentage(0), new Constraint.Max(40)))
          .render(split[2], buf);
      new Example(List.of(new Constraint.Percentage(0), new Constraint.Max(60)))
          .render(split[3], buf);
      new Example(List.of(new Constraint.Percentage(0), new Constraint.Max(80)))
          .render(split[4], buf);
    }
  }

  enum AppState {
    Running,
    Quit
  }

  static final class App implements Widget {
    SelectedTab selectedTab = SelectedTab.Min;
    int scrollOffset = 0;
    int maxScrollOffset = 0;
    AppState state = AppState.Running;

    void run(Terminal<CrosstermBackend> terminal) throws IOException {
      updateMaxScrollOffset();
      while (state == AppState.Running) {
        terminal.draw(frame -> frame.renderWidget(this, frame.area()));
        handleEvents();
      }
    }

    private void updateMaxScrollOffset() {
      maxScrollOffset = (selectedTab.getExampleCount() - 1) * EXAMPLE_HEIGHT;
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
          case 'l' -> nextTab();
          case 'h' -> previousTab();
          case 'j' -> down();
          case 'k' -> up();
          case 'g' -> top();
          case 'G' -> bottom();
          default -> {}
        }
      } else if (code instanceof KeyCode.Esc) {
        state = AppState.Quit;
      } else if (code instanceof KeyCode.Right) {
        nextTab();
      } else if (code instanceof KeyCode.Left) {
        previousTab();
      } else if (code instanceof KeyCode.Down) {
        down();
      } else if (code instanceof KeyCode.Up) {
        up();
      } else if (code instanceof KeyCode.Home) {
        top();
      } else if (code instanceof KeyCode.End) {
        bottom();
      }
    }

    private void nextTab() {
      selectedTab = selectedTab.next();
      updateMaxScrollOffset();
      scrollOffset = 0;
    }

    private void previousTab() {
      selectedTab = selectedTab.previous();
      updateMaxScrollOffset();
      scrollOffset = 0;
    }

    private void up() {
      scrollOffset = Math.max(0, scrollOffset - 1);
    }

    private void down() {
      scrollOffset = Math.min(maxScrollOffset, scrollOffset + 1);
    }

    private void top() {
      scrollOffset = 0;
    }

    private void bottom() {
      scrollOffset = maxScrollOffset;
    }

    @Override
    public void render(Rect area, Buffer buf) {
      Rect[] split =
          area.layout(
              Layout.vertical(
                  new Constraint.Length(3), new Constraint.Length(3), new Constraint.Fill(0)),
              3);
      Rect tabsArea = split[0];
      Rect axisArea = split[1];
      Rect demoArea = split[2];
      renderTabs(tabsArea, buf);
      renderAxis(axisArea, buf);
      renderDemo(demoArea, buf);
    }

    private void renderTabs(Rect area, Buffer buf) {
      List<Line> titles = new ArrayList<>();
      for (SelectedTab t : SelectedTab.values()) {
        titles.add(t.toTabTitle());
      }
      Block block =
          Block.empty()
              .withTitle(Line.styled("Constraints ", Style.empty().bold()))
              .withTitle(Line.from(" Use h l or ◄ ► to change tab and j k or ▲ ▼  to scroll"));
      Tabs tabs =
          Tabs.of(titles)
              .withBlock(block)
              .withHighlightStyle(Style.fromModifier(Modifier.REVERSED))
              .withSelected(selectedTab.ordinal())
              .withPadding("", "")
              .withDivider(" ");
      tabs.render(area, buf);
    }

    private static void renderAxis(Rect area, Buffer buf) {
      int width = area.width();
      // a bar like `<----- 80 px ----->`
      String widthLabel = width + " px";
      String widthBar =
          "<" + center(widthLabel, '-', Math.max(0, width - widthLabel.length() / 2)) + ">";
      Paragraph.of(Line.styled(widthBar, Style.empty().darkGray()))
          .centered()
          .withBlock(Block.empty().withPadding(new Padding(0, 0, 1, 0)))
          .render(area, buf);
    }

    private void renderDemo(Rect area, Buffer buf) {
      // Render demo content into a separate buffer so all examples fit; we add an extra
      // area.height to make sure the last example is fully visible even when the scroll offset is
      // at the max.
      int height = selectedTab.getExampleCount() * EXAMPLE_HEIGHT;
      Rect demoArea = new Rect(0, 0, area.width(), height + area.height());
      Buffer demoBuf = Buffer.empty(demoArea);

      boolean scrollbarNeeded = scrollOffset != 0 || height > area.height();
      Rect contentArea =
          scrollbarNeeded
              ? new Rect(demoArea.x(), demoArea.y(), demoArea.width() - 1, demoArea.height())
              : demoArea;
      selectedTab.render(contentArea, demoBuf);

      Cell[] content = demoBuf.content();
      int skip = demoArea.width() * scrollOffset;
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
        ScrollbarState scrollState = ScrollbarState.of(maxScrollOffset).withPosition(scrollOffset);
        Scrollbar.of(ScrollbarOrientation.VerticalRight).render(area, buf, scrollState);
      }
    }
  }

  /// Renders one row of the demo: the constraints' label and a colored block of the actual width.
  static final class Example implements Widget {
    private final List<Constraint> constraints;

    Example(List<Constraint> constraints) {
      this.constraints = List.copyOf(constraints);
    }

    @Override
    public void render(Rect area, Buffer buf) {
      Layout vertical =
          Layout.vertical(
              new Constraint.Length(ILLUSTRATION_HEIGHT), new Constraint.Length(SPACER_HEIGHT));
      Layout horizontal = Layout.horizontal(constraints);
      Rect[] verticalSplit = area.layout(vertical, 2);
      Rect illustration = verticalSplit[0];
      List<Rect> blocks = illustration.layoutVec(horizontal);

      for (int i = 0; i < blocks.size() && i < constraints.size(); i++) {
        Rect block = blocks.get(i);
        illustration(constraints.get(i), block.width()).render(block, buf);
      }
    }

    static Widget illustration(Constraint constraint, int width) {
      Color color =
          switch (constraint) {
            case Constraint.Length l -> LENGTH_COLOR;
            case Constraint.Percentage p -> PERCENTAGE_COLOR;
            case Constraint.Ratio r -> RATIO_COLOR;
            case Constraint.Fill f -> FILL_COLOR;
            case Constraint.Min m -> MIN_COLOR;
            case Constraint.Max m -> MAX_COLOR;
          };
      Color fg = Color.WHITE;
      String title = describe(constraint);
      String content = width + " px";
      String text = title + "\n" + content;
      Block block =
          Block.bordered()
              .withBorderSet(Border.QUADRANT_OUTSIDE)
              .withBorderStyle(Style.reset().withFg(color).reversed())
              .withStyle(Style.empty().withFg(fg).withBg(color));
      return Paragraph.of(text).withAlignment(HorizontalAlignment.Center).withBlock(block);
    }
  }

  /// Mirrors Rust's `format!("{constraint}")` which uses each constraint variant's `Display` impl.
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

  /// Center-pad `s` with `pad` until it has `width` columns. Mirrors Rust's
  /// `format!("{s:-^width$}")`.
  private static String center(String s, char pad, int width) {
    if (s.length() >= width) return s;
    int total = width - s.length();
    int left = total / 2;
    int right = total - left;
    return repeat(pad, left) + s + repeat(pad, right);
  }

  private static String repeat(char ch, int count) {
    if (count <= 0) return "";
    char[] arr = new char[count];
    java.util.Arrays.fill(arr, ch);
    return new String(arr);
  }
}
