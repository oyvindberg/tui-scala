package jatatui.examples.constraintexplorer;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Constraint;
import jatatui.core.layout.Flex;
import jatatui.core.layout.Layout;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.style.Style;
import jatatui.core.style.palette.Tailwind;
import jatatui.core.symbols.Border;
import jatatui.core.symbols.Line;
import jatatui.core.terminal.Terminal;
import jatatui.core.text.Span;
import jatatui.core.text.Text;
import jatatui.core.widgets.Widget;
import jatatui.crossterm.CrosstermBackend;
import jatatui.crossterm.Jatatui;
import jatatui.widgets.Borders;
import jatatui.widgets.block.Block;
import jatatui.widgets.paragraph.Paragraph;
import jatatui.widgets.paragraph.Wrap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Event;
import tui.crossterm.KeyCode;
import tui.crossterm.KeyEventKind;

/// A jatatui example that demonstrates how different layout constraints work.
///
/// It also supports swapping constraints, adding and removing blocks, and changing the spacing
/// between blocks.
///
/// Java port of `examples/apps/constraint-explorer/src/main.rs` from ratatui v0.30.
public final class ConstraintExplorerExample {

  private static final CrosstermJni JNI = new CrosstermJni();

  private ConstraintExplorerExample() {}

  public static void main(String[] args) throws IOException {
    Jatatui.runIo(terminal -> new App().run(terminal));
  }

  // ---- AppMode ----

  private enum AppMode {
    Running,
    Quit
  }

  // ---- ConstraintName ----

  private enum ConstraintName {
    Length,
    Percentage,
    Ratio,
    Min,
    Max,
    Fill;

    static ConstraintName of(Constraint constraint) {
      return switch (constraint) {
        case Constraint.Length l -> Length;
        case Constraint.Percentage p -> Percentage;
        case Constraint.Ratio r -> Ratio;
        case Constraint.Min m -> Min;
        case Constraint.Max m -> Max;
        case Constraint.Fill f -> Fill;
      };
    }

    Color color() {
      return switch (this) {
        case Length -> Tailwind.SLATE.c700();
        case Percentage -> Tailwind.SLATE.c800();
        case Ratio -> Tailwind.SLATE.c900();
        case Fill -> Tailwind.SLATE.c950();
        case Min -> Tailwind.BLUE.c800();
        case Max -> Tailwind.BLUE.c900();
      };
    }

    Color lighterColor() {
      return switch (this) {
        case Length -> Tailwind.STONE.c500();
        case Percentage -> Tailwind.STONE.c600();
        case Ratio -> Tailwind.STONE.c700();
        case Fill -> Tailwind.STONE.c800();
        case Min -> Tailwind.SKY.c600();
        case Max -> Tailwind.SKY.c700();
      };
    }
  }

  // ---- App ----

  private static final class App implements Widget {
    private static final Color HEADER_COLOR = Tailwind.SLATE.c200();
    private static final Color TEXT_COLOR = Tailwind.SLATE.c400();
    private static final Color AXIS_COLOR = Tailwind.SLATE.c500();

    private AppMode mode;
    private int spacing;
    private final List<Constraint> constraints;
    private int selectedIndex;
    private int value;

    App() {
      this.mode = AppMode.Running;
      this.spacing = 0;
      this.constraints = new ArrayList<>();
      this.selectedIndex = 0;
      this.value = 0;
    }

    void run(Terminal<CrosstermBackend> terminal) throws IOException {
      insertTestDefaults();
      while (isRunning()) {
        terminal.draw(frame -> frame.renderWidget(this, frame.area()));
        handleEvents();
      }
    }

    /// Mirrors upstream's `insert_test_defaults` (the upstream-acknowledged temporary scaffold).
    private void insertTestDefaults() {
      constraints.clear();
      constraints.add(new Constraint.Length(20));
      constraints.add(new Constraint.Length(20));
      constraints.add(new Constraint.Length(20));
      value = 20;
    }

    private boolean isRunning() {
      return mode == AppMode.Running;
    }

    private void handleEvents() {
      Event event = JNI.read();
      if (!(event instanceof Event.Key key)
          || key.keyEvent().kind() != KeyEventKind.Press) {
        return;
      }
      KeyCode code = key.keyEvent().code();
      if (code instanceof KeyCode.Char ch) {
        char c = ch.c();
        switch (c) {
          case 'q' -> exit();
          case '1' -> swapConstraint(ConstraintName.Min);
          case '2' -> swapConstraint(ConstraintName.Max);
          case '3' -> swapConstraint(ConstraintName.Length);
          case '4' -> swapConstraint(ConstraintName.Percentage);
          case '5' -> swapConstraint(ConstraintName.Ratio);
          case '6' -> swapConstraint(ConstraintName.Fill);
          case '+' -> incrementSpacing();
          case '-' -> decrementSpacing();
          case 'x' -> deleteBlock();
          case 'a' -> insertBlock();
          case 'k' -> incrementValue();
          case 'j' -> decrementValue();
          case 'h' -> prevBlock();
          case 'l' -> nextBlock();
          default -> {
            // ignore
          }
        }
      } else if (code instanceof KeyCode.Esc) {
        exit();
      } else if (code instanceof KeyCode.Up) {
        incrementValue();
      } else if (code instanceof KeyCode.Down) {
        decrementValue();
      } else if (code instanceof KeyCode.Left) {
        prevBlock();
      } else if (code instanceof KeyCode.Right) {
        nextBlock();
      }
    }

    private void incrementValue() {
      if (selectedIndex < 0 || selectedIndex >= constraints.size()) {
        return;
      }
      Constraint c = constraints.get(selectedIndex);
      Constraint updated = switch (c) {
        case Constraint.Length l -> new Constraint.Length(saturatingAdd(l.v(), 1));
        case Constraint.Min m -> new Constraint.Min(saturatingAdd(m.v(), 1));
        case Constraint.Max m -> new Constraint.Max(saturatingAdd(m.v(), 1));
        case Constraint.Fill f -> new Constraint.Fill(saturatingAdd(f.v(), 1));
        case Constraint.Percentage p -> new Constraint.Percentage(saturatingAdd(p.v(), 1));
        case Constraint.Ratio r -> new Constraint.Ratio(r.numerator(), saturatingAdd(r.denominator(), 1));
      };
      constraints.set(selectedIndex, updated);
    }

    private void decrementValue() {
      if (selectedIndex < 0 || selectedIndex >= constraints.size()) {
        return;
      }
      Constraint c = constraints.get(selectedIndex);
      Constraint updated = switch (c) {
        case Constraint.Length l -> new Constraint.Length(saturatingSub(l.v(), 1));
        case Constraint.Min m -> new Constraint.Min(saturatingSub(m.v(), 1));
        case Constraint.Max m -> new Constraint.Max(saturatingSub(m.v(), 1));
        case Constraint.Fill f -> new Constraint.Fill(saturatingSub(f.v(), 1));
        case Constraint.Percentage p -> new Constraint.Percentage(saturatingSub(p.v(), 1));
        case Constraint.Ratio r -> new Constraint.Ratio(r.numerator(), saturatingSub(r.denominator(), 1));
      };
      constraints.set(selectedIndex, updated);
    }

    /// select the next block with wrap around
    private void nextBlock() {
      if (constraints.isEmpty()) {
        return;
      }
      selectedIndex = (selectedIndex + 1) % constraints.size();
    }

    /// select the previous block with wrap around
    private void prevBlock() {
      if (constraints.isEmpty()) {
        return;
      }
      int len = constraints.size();
      selectedIndex = (selectedIndex + len - 1) % len;
    }

    /// delete the selected block
    private void deleteBlock() {
      if (constraints.isEmpty()) {
        return;
      }
      constraints.remove(selectedIndex);
      selectedIndex = Math.max(0, selectedIndex - 1);
    }

    /// insert a block after the selected block
    private void insertBlock() {
      int index = Math.min(selectedIndex + 1, constraints.size());
      Constraint constraint = new Constraint.Length(value);
      constraints.add(index, constraint);
      selectedIndex = index;
    }

    private void incrementSpacing() {
      spacing = saturatingAdd(spacing, 1);
    }

    private void decrementSpacing() {
      spacing = saturatingSub(spacing, 1);
    }

    private void exit() {
      mode = AppMode.Quit;
    }

    private void swapConstraint(ConstraintName name) {
      if (constraints.isEmpty()) {
        return;
      }
      Constraint constraint = switch (name) {
        case Length -> new Constraint.Length(value);
        case Percentage -> new Constraint.Percentage(value);
        case Min -> new Constraint.Min(value);
        case Max -> new Constraint.Max(value);
        case Fill -> new Constraint.Fill(value);
        case Ratio -> new Constraint.Ratio(1, value / 4); // for balance
      };
      constraints.set(selectedIndex, constraint);
    }

    // ---- Rendering ----

    @Override
    public void render(Rect area, Buffer buf) {
      Rect[] rows = Layout.vertical(
          new Constraint.Length(2), // header
          new Constraint.Length(2), // instructions
          new Constraint.Length(1), // swap key legend
          new Constraint.Length(1), // gap
          new Constraint.Fill(1)    // blocks
      ).split(area);
      Rect headerArea = rows[0];
      Rect instructionsArea = rows[1];
      Rect swapLegendArea = rows[2];
      Rect blocksArea = rows[4];

      header().render(headerArea, buf);
      instructions().render(instructionsArea, buf);
      swapLegend().render(swapLegendArea, buf);
      renderLayoutBlocks(blocksArea, buf);
    }

    private static Widget header() {
      return Paragraph.of(Span.styled(
              "Constraint Explorer", Style.empty().bold().withFg(HEADER_COLOR))
          .intoCenteredLine());
    }

    private static Widget instructions() {
      String text =
          "◄ ►: select, ▲ ▼: edit, 1-6: swap, a: add, x: delete, q: quit, + -: spacing";
      return Paragraph.of(text)
          .withStyle(Style.empty().withFg(TEXT_COLOR))
          .centered()
          .withWrap(new Wrap(false));
    }

    private static Widget swapLegend() {
      ConstraintName[] names = {
          ConstraintName.Min,
          ConstraintName.Max,
          ConstraintName.Length,
          ConstraintName.Percentage,
          ConstraintName.Ratio,
          ConstraintName.Fill,
      };
      List<Span> spans = new ArrayList<>(names.length * 2 - 1);
      for (int i = 0; i < names.length; i++) {
        if (i > 0) {
          spans.add(Span.from(" "));
        }
        ConstraintName name = names[i];
        String label = "  " + (i + 1) + ": " + name + "  ";
        spans.add(Span.styled(label,
            Style.empty().withFg(Tailwind.SLATE.c200()).withBg(name.color())));
      }
      jatatui.core.text.Line line = jatatui.core.text.Line.fromSpans(spans).centered();
      return Paragraph.of(line).withWrap(new Wrap(false));
    }

    /// A bar like `<----- 80 px (gap: 2 px) ----->`.
    ///
    /// Only shows the gap when spacing is not zero
    private Widget axis(int width) {
      String label = spacing != 0
          ? width + " px (gap: " + spacing + " px)"
          : width + " px";
      int barWidth = Math.max(0, width - 2); // we want `<` and `>` at the ends
      String widthBar = "<" + center(label, barWidth, '-') + ">";
      return Paragraph.of(widthBar)
          .withStyle(Style.empty().withFg(AXIS_COLOR))
          .centered();
    }

    private void renderLayoutBlocks(Rect area, Buffer buf) {
      Layout mainLayout = Layout.vertical(
          new Constraint.Length(3), new Constraint.Fill(1)).withSpacing(1);
      Rect[] split = mainLayout.split(area);
      Rect userConstraints = split[0];
      Rect rest = split[1];

      renderUserConstraintsLegend(userConstraints, buf);

      Rect[] rows = Layout.vertical(
          new Constraint.Length(7),
          new Constraint.Length(7),
          new Constraint.Length(7),
          new Constraint.Length(7),
          new Constraint.Length(7),
          new Constraint.Length(7)).split(rest);

      renderLayoutBlock(Flex.Start, rows[0], buf);
      renderLayoutBlock(Flex.Center, rows[1], buf);
      renderLayoutBlock(Flex.End, rows[2], buf);
      renderLayoutBlock(Flex.SpaceBetween, rows[3], buf);
      renderLayoutBlock(Flex.SpaceAround, rows[4], buf);
      renderLayoutBlock(Flex.SpaceEvenly, rows[5], buf);
    }

    private void renderUserConstraintsLegend(Rect area, Buffer buf) {
      List<Constraint> fills = new ArrayList<>(constraints.size());
      for (int i = 0; i < constraints.size(); i++) {
        fills.add(new Constraint.Fill(1));
      }
      Rect[] blocks = Layout.horizontal(fills).split(area);

      for (int i = 0; i < blocks.length && i < constraints.size(); i++) {
        boolean selected = selectedIndex == i;
        new ConstraintBlock(constraints.get(i), selected, true).render(blocks[i], buf);
      }
    }

    private void renderLayoutBlock(Flex flex, Rect area, Buffer buf) {
      Layout layout = Layout.vertical(
          new Constraint.Length(1), new Constraint.Max(1), new Constraint.Length(4));
      Rect[] split = layout.split(area);
      Rect labelArea = split[0];
      Rect axisArea = split[1];
      Rect blocksArea = split[2];

      if (labelArea.height() > 0) {
        Span span = Span.styled("Flex::" + flex, Style.empty().bold());
        Paragraph.of(jatatui.core.text.Line.from(span)).render(labelArea, buf);
      }

      axis(area.width()).render(axisArea, buf);

      Layout.SplitResult split2 = Layout.horizontal(constraints)
          .withFlex(flex)
          .withSpacing(spacing)
          .splitWithSpacers(blocksArea);
      Rect[] blocks = split2.segments();
      Rect[] spacers = split2.spacers();

      for (int i = 0; i < blocks.length && i < constraints.size(); i++) {
        boolean selected = selectedIndex == i;
        new ConstraintBlock(constraints.get(i), selected, false).render(blocks[i], buf);
      }

      for (Rect spacer : spacers) {
        new SpacerBlock().render(spacer, buf);
      }
    }
  }

  // ---- ConstraintBlock ----

  private static final class ConstraintBlock implements Widget {
    private static final Color TEXT_COLOR = Tailwind.SLATE.c200();

    private final Constraint constraint;
    private final boolean selected;
    private final boolean legend;

    ConstraintBlock(Constraint constraint, boolean selected, boolean legend) {
      this.constraint = constraint;
      this.selected = selected;
      this.legend = legend;
    }

    @Override
    public void render(Rect area, Buffer buf) {
      switch (area.height()) {
        case 1 -> render1px(area, buf);
        case 2 -> render2px(area, buf);
        default -> render4px(area, buf);
      }
    }

    private String label(int width) {
      String longWidth = width + " px";
      String shortWidth = Integer.toString(width);
      // border takes up 2 columns
      int availableSpace = Math.max(0, width - 2);
      String widthLabel;
      if (longWidth.length() < availableSpace) {
        widthLabel = longWidth;
      } else if (shortWidth.length() < availableSpace) {
        widthLabel = shortWidth;
      } else {
        widthLabel = "";
      }
      return constraintToString(constraint) + "\n" + widthLabel;
    }

    private void render1px(Rect area, Buffer buf) {
      Color lighter = ConstraintName.of(constraint).lighterColor();
      Color main = ConstraintName.of(constraint).color();
      Color selectedColor = selected ? lighter : main;
      Block.empty()
          .withStyle(Style.empty().withFg(TEXT_COLOR).withBg(selectedColor))
          .render(area, buf);
    }

    private void render2px(Rect area, Buffer buf) {
      Color lighter = ConstraintName.of(constraint).lighterColor();
      Color main = ConstraintName.of(constraint).color();
      Color selectedColor = selected ? lighter : main;
      Block.bordered()
          .withBorderSet(Border.QUADRANT_OUTSIDE)
          .withBorderStyle(Style.reset().withFg(selectedColor).reversed())
          .render(area, buf);
    }

    private void render4px(Rect area, Buffer buf) {
      Color lighter = ConstraintName.of(constraint).lighterColor();
      Color main = ConstraintName.of(constraint).color();
      Color selectedColor = selected ? lighter : main;
      Color color = legend ? selectedColor : main;
      String labelText = label(area.width());
      Block block = Block.bordered()
          .withBorderSet(Border.QUADRANT_OUTSIDE)
          .withBorderStyle(Style.reset().withFg(color).reversed())
          .withStyle(Style.empty().withFg(TEXT_COLOR).withBg(color));
      Paragraph.of(labelText)
          .centered()
          .withStyle(Style.empty().withFg(TEXT_COLOR).withBg(color))
          .withBlock(block)
          .render(area, buf);

      if (!legend) {
        Color borderColor = selected ? lighter : main;
        Optional<Rect> lastRow = area.rows().nextBack();
        lastRow.ifPresent(r -> buf.setStyle(r, Style.empty().withFg(borderColor).withBg(borderColor)));
      }
    }
  }

  // ---- SpacerBlock ----

  private static final class SpacerBlock implements Widget {
    private static final Color TEXT_COLOR = Tailwind.SLATE.c500();
    private static final Color BORDER_COLOR = Tailwind.SLATE.c600();

    @Override
    public void render(Rect area, Buffer buf) {
      switch (area.height()) {
        case 1 -> {
          // nothing
        }
        case 2 -> render2px(area, buf);
        case 3 -> render3px(area, buf);
        default -> render4px(area, buf);
      }
    }

    /// A block with corner borders (the four corners of the line set, with spaces filling the
    /// sides — mirrors the Rust `border_set` literal).
    private static Widget block() {
      Border.Set cornersOnly = new Border.Set(
          Line.NORMAL.topLeft(),
          Line.NORMAL.topRight(),
          Line.NORMAL.bottomLeft(),
          Line.NORMAL.bottomRight(),
          " ", " ", " ", " ");
      return Block.bordered()
          .withBorderSet(cornersOnly)
          .withBorderStyle(Style.empty().withFg(BORDER_COLOR));
    }

    /// A vertical line used if there is not enough space to render the block.
    private static Widget line() {
      List<jatatui.core.text.Line> lines = new ArrayList<>(4);
      lines.add(jatatui.core.text.Line.from(""));
      lines.add(jatatui.core.text.Line.from("│"));
      lines.add(jatatui.core.text.Line.from("│"));
      lines.add(jatatui.core.text.Line.from(""));
      return Paragraph.of(Text.fromLines(lines))
          .withStyle(Style.empty().withFg(BORDER_COLOR));
    }

    /// A label that says "Spacer" if there is enough space.
    private static Widget spacerLabel(int width) {
      String label = width >= 6 ? "Spacer" : "";
      return Paragraph.of(
          Span.styled(label, Style.empty().withFg(TEXT_COLOR)).intoCenteredLine());
    }

    /// A label that says "8 px" if there is enough space.
    private static Widget label(int width) {
      String longLabel = width + " px";
      String shortLabel = Integer.toString(width);
      String label;
      if (longLabel.length() < width) {
        label = longLabel;
      } else if (shortLabel.length() < width) {
        label = shortLabel;
      } else {
        label = "";
      }
      return Paragraph.of(
          jatatui.core.text.Line.styled(label, Style.empty().withFg(TEXT_COLOR)).centered());
    }

    private static void render2px(Rect area, Buffer buf) {
      if (area.width() > 1) {
        block().render(area, buf);
      } else {
        line().render(area, buf);
      }
    }

    private static void render3px(Rect area, Buffer buf) {
      if (area.width() > 1) {
        block().render(area, buf);
      } else {
        line().render(area, buf);
      }
      Rect row = nthRow(area, 1);
      spacerLabel(area.width()).render(row, buf);
    }

    private static void render4px(Rect area, Buffer buf) {
      if (area.width() > 1) {
        block().render(area, buf);
      } else {
        line().render(area, buf);
      }
      Rect row1 = nthRow(area, 1);
      spacerLabel(area.width()).render(row1, buf);
      Rect row2 = nthRow(area, 2);
      label(area.width()).render(row2, buf);
    }

    /// Returns the n-th row of `area`, or an empty rect at the area's origin when out-of-range —
    /// mirrors upstream's `area.rows().nth(n).unwrap_or_default()`.
    private static Rect nthRow(Rect area, int n) {
      Rect.Rows rows = area.rows();
      Rect cur = new Rect(0, 0, 0, 0);
      int i = 0;
      while (rows.hasNext()) {
        Rect r = rows.next();
        if (i == n) {
          return r;
        }
        i += 1;
      }
      return cur;
    }
  }

  // ---- Helpers ----

  private static String constraintToString(Constraint c) {
    // Mirrors the Rust `Display` impl on `Constraint` (e.g. "Length(20)").
    return switch (c) {
      case Constraint.Length l -> "Length(" + l.v() + ")";
      case Constraint.Percentage p -> "Percentage(" + p.v() + ")";
      case Constraint.Ratio r -> "Ratio(" + r.numerator() + ", " + r.denominator() + ")";
      case Constraint.Min m -> "Min(" + m.v() + ")";
      case Constraint.Max m -> "Max(" + m.v() + ")";
      case Constraint.Fill f -> "Fill(" + f.v() + ")";
    };
  }

  /// Format `s` centered in a field of `width` columns, padding both sides with `pad`.
  /// Mirrors Rust's `format!("{label:-^bar_width$}")`.
  private static String center(String s, int width, char pad) {
    if (s.length() >= width) {
      return s;
    }
    int total = width - s.length();
    int left = total / 2;
    int right = total - left;
    StringBuilder sb = new StringBuilder(width);
    for (int i = 0; i < left; i++) sb.append(pad);
    sb.append(s);
    for (int i = 0; i < right; i++) sb.append(pad);
    return sb.toString();
  }

  private static int saturatingAdd(int a, int b) {
    long r = (long) a + (long) b;
    if (r > Integer.MAX_VALUE) return Integer.MAX_VALUE;
    return (int) r;
  }

  private static int saturatingSub(int a, int b) {
    long r = (long) a - (long) b;
    if (r < 0) return 0;
    return (int) r;
  }
}
