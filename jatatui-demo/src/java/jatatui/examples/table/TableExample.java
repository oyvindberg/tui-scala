package jatatui.examples.table;

import jatatui.core.internal.Wcwidth;
import jatatui.core.layout.Constraint;
import jatatui.core.layout.Layout;
import jatatui.core.layout.Margin;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import jatatui.core.style.palette.Tailwind;
import jatatui.core.terminal.Frame;
import jatatui.core.terminal.Terminal;
import jatatui.core.text.Line;
import jatatui.core.text.Text;
import jatatui.crossterm.CrosstermBackend;
import jatatui.crossterm.Jatatui;
import jatatui.widgets.block.Block;
import jatatui.widgets.block.BorderType;
import jatatui.widgets.paragraph.Paragraph;
import jatatui.widgets.scrollbar.Scrollbar;
import jatatui.widgets.scrollbar.ScrollbarOrientation;
import jatatui.widgets.scrollbar.ScrollbarState;
import jatatui.widgets.table.HighlightSpacing;
import jatatui.widgets.table.Row;
import jatatui.widgets.table.Table;
import jatatui.widgets.table.TableCell;
import jatatui.widgets.table.TableState;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Event;
import tui.crossterm.KeyCode;
import tui.crossterm.KeyEvent;
import tui.crossterm.KeyEventKind;
import tui.crossterm.KeyModifiers;

/// A jatatui example that demonstrates how to create an interactive table with a scrollbar.
///
/// Mirrors `examples/apps/table/src/main.rs` from ratatui v0.30.0.
///
/// Note: upstream uses the `fakeit` crate to generate fake names/addresses/emails. There is no
/// equivalent in our build, so the rows are a small, fixed dataset chosen to exercise the table
/// layout without external dependencies.
public final class TableExample {

  private static final CrosstermJni JNI = new CrosstermJni();

  private static final List<Tailwind.Palette> PALETTES =
      List.of(Tailwind.BLUE, Tailwind.EMERALD, Tailwind.INDIGO, Tailwind.RED);

  private static final List<String> INFO_TEXT =
      List.of(
          "(Esc) quit | (↑) move up | (↓) move down | (←) move left | (→) move right",
          "(Shift + →) next color | (Shift + ←) previous color");

  private static final int ITEM_HEIGHT = 4;

  private TableExample() {}

  /// `(name, address, email)` triple for a single row of fake data.
  ///
  /// Replaces upstream's `Data` struct. The `address` may contain `\n` so addresses span two lines.
  record Data(String name, String address, String email) {
    String[] refArray() {
      return new String[] {name, address, email};
    }
  }

  /// Lengths of the longest name / address / email strings.
  ///
  /// Replaces upstream's `(u16, u16, u16)` tuple per the project's "tuples get dedicated record
  /// types" rule.
  record ColumnWidths(int name, int address, int email) {}

  static final class TableColors {
    final Color bufferBg;
    final Color headerBg;
    final Color headerFg;
    final Color rowFg;
    final Color selectedRowStyleFg;
    final Color selectedColumnStyleFg;
    final Color selectedCellStyleFg;
    final Color normalRowColor;
    final Color altRowColor;
    final Color footerBorderColor;

    TableColors(Tailwind.Palette palette) {
      this.bufferBg = Tailwind.SLATE.c950();
      this.headerBg = palette.c900();
      this.headerFg = Tailwind.SLATE.c200();
      this.rowFg = Tailwind.SLATE.c200();
      this.selectedRowStyleFg = palette.c400();
      this.selectedColumnStyleFg = palette.c400();
      this.selectedCellStyleFg = palette.c600();
      this.normalRowColor = Tailwind.SLATE.c950();
      this.altRowColor = Tailwind.SLATE.c900();
      this.footerBorderColor = palette.c400();
    }
  }

  static final class App {
    final TableState state = new TableState().withSelected(0);
    final List<Data> items;
    final ColumnWidths longestItemLens;
    ScrollbarState scrollState;
    TableColors colors;
    int colorIndex = 0;

    App() {
      this.items = generateFakeNames();
      this.longestItemLens = constraintLenCalculator(items);
      this.scrollState = ScrollbarState.of(Math.max(0, items.size() - 1) * ITEM_HEIGHT);
      this.colors = new TableColors(PALETTES.get(0));
    }

    void nextRow() {
      int i;
      if (state.selected().isPresent()) {
        int s = state.selected().get();
        i = s >= items.size() - 1 ? 0 : s + 1;
      } else {
        i = 0;
      }
      state.select(i);
      scrollState = scrollState.withPosition(i * ITEM_HEIGHT);
    }

    void previousRow() {
      int i;
      if (state.selected().isPresent()) {
        int s = state.selected().get();
        i = s == 0 ? items.size() - 1 : s - 1;
      } else {
        i = 0;
      }
      state.select(i);
      scrollState = scrollState.withPosition(i * ITEM_HEIGHT);
    }

    void nextColumn() {
      state.selectNextColumn();
    }

    void previousColumn() {
      state.selectPreviousColumn();
    }

    void nextColor() {
      colorIndex = (colorIndex + 1) % PALETTES.size();
    }

    void previousColor() {
      int n = PALETTES.size();
      colorIndex = (colorIndex + n - 1) % n;
    }

    void setColors() {
      colors = new TableColors(PALETTES.get(colorIndex));
    }

    void run(Terminal<CrosstermBackend> terminal) throws IOException {
      while (true) {
        terminal.draw(frame -> render(frame));
        Event ev = JNI.read();
        if (!(ev instanceof Event.Key keyEv)) {
          continue;
        }
        KeyEvent key = keyEv.keyEvent();
        if (key.kind() != KeyEventKind.Press) {
          continue;
        }
        boolean shiftPressed = (key.modifiers().bits() & KeyModifiers.SHIFT) != 0;
        KeyCode code = key.code();
        if (code instanceof KeyCode.Esc) {
          return;
        }
        if (code instanceof KeyCode.Char ch) {
          char c = ch.c();
          if (c == 'q') return;
          if (c == 'j') {
            nextRow();
          } else if (c == 'k') {
            previousRow();
          } else if (c == 'l') {
            if (shiftPressed) nextColor();
            else nextColumn();
          } else if (c == 'h') {
            if (shiftPressed) previousColor();
            else previousColumn();
          }
        } else if (code instanceof KeyCode.Down) {
          nextRow();
        } else if (code instanceof KeyCode.Up) {
          previousRow();
        } else if (code instanceof KeyCode.Right) {
          if (shiftPressed) nextColor();
          else nextColumn();
        } else if (code instanceof KeyCode.Left) {
          if (shiftPressed) previousColor();
          else previousColumn();
        }
      }
    }

    void render(Frame frame) {
      Layout layout = Layout.vertical(new Constraint.Min(5), new Constraint.Length(4));
      List<Rect> rects = frame.area().layoutVec(layout);

      setColors();

      renderTable(frame, rects.get(0));
      renderScrollbar(frame, rects.get(0));
      renderFooter(frame, rects.get(1));
    }

    void renderTable(Frame frame, Rect area) {
      Style headerStyle = Style.empty().withFg(colors.headerFg).withBg(colors.headerBg);
      Style selectedRowStyle =
          Style.empty().withAddModifier(Modifier.REVERSED).withFg(colors.selectedRowStyleFg);
      Style selectedColStyle = Style.empty().withFg(colors.selectedColumnStyleFg);
      Style selectedCellStyle =
          Style.empty().withAddModifier(Modifier.REVERSED).withFg(colors.selectedCellStyleFg);

      Row header =
          Row.of(TableCell.of("Name"), TableCell.of("Address"), TableCell.of("Email"))
              .withStyle(headerStyle)
              .withHeight(1);

      List<Row> rows = new ArrayList<>(items.size());
      for (int i = 0; i < items.size(); i++) {
        Data data = items.get(i);
        Color rowBg = i % 2 == 0 ? colors.normalRowColor : colors.altRowColor;
        String[] item = data.refArray();
        List<TableCell> cells = new ArrayList<>(item.length);
        for (String content : item) {
          cells.add(TableCell.of(Text.from("\n" + content + "\n")));
        }
        rows.add(
            Row.of(cells)
                .withStyle(Style.empty().withFg(colors.rowFg).withBg(rowBg))
                .withHeight(4));
      }

      String bar = " █ ";
      Table t =
          Table.of(
                  rows,
                  List.of(
                      // + 1 is for padding.
                      new Constraint.Length(longestItemLens.name() + 1),
                      new Constraint.Min(longestItemLens.address() + 1),
                      new Constraint.Min(longestItemLens.email())))
              .withHeader(header)
              .withRowHighlightStyle(selectedRowStyle)
              .withColumnHighlightStyle(selectedColStyle)
              .withCellHighlightStyle(selectedCellStyle)
              .withHighlightSymbol(
                  Text.fromLines(List.of(Line.from(""), Line.from(bar), Line.from(bar), Line.from(""))))
              .withStyle(Style.empty().withBg(colors.bufferBg))
              .withHighlightSpacing(HighlightSpacing.Always);
      frame.renderStatefulWidget(t, area, state);
    }

    void renderScrollbar(Frame frame, Rect area) {
      frame.renderStatefulWidget(
          Scrollbar.of(ScrollbarOrientation.VerticalRight)
              .withBeginSymbol(java.util.Optional.empty())
              .withEndSymbol(java.util.Optional.empty()),
          area.inner(new Margin(1, 1)),
          scrollState);
    }

    void renderFooter(Frame frame, Rect area) {
      Paragraph footer =
          Paragraph.of(Text.fromIter(INFO_TEXT))
              .withStyle(Style.empty().withFg(colors.rowFg).withBg(colors.bufferBg))
              .centered()
              .withBlock(
                  Block.bordered()
                      .withBorderType(BorderType.Double)
                      .withBorderStyle(Style.empty().withFg(colors.footerBorderColor)));
      frame.renderWidget(footer, area);
    }
  }

  public static void main(String[] args) throws IOException {
    Jatatui.runIo(terminal -> new App().run(terminal));
  }

  /// Produces a small, fixed dataset for the table example.
  ///
  /// Upstream uses the `fakeit` crate; we have no equivalent in the build, so the rows are a
  /// curated mix that exercises both single-line and multi-line address rendering.
  static List<Data> generateFakeNames() {
    List<Data> data =
        new ArrayList<>(
            Arrays.asList(
                new Data(
                    "Emirhan Tala",
                    "Cambridgelaan 6XX\n3584 XX Utrecht",
                    "tala.emirhan@example.com"),
                new Data(
                    "Florian Dehau",
                    "12 rue de la Paix\n75002 Paris",
                    "florian@example.com"),
                new Data(
                    "Ada Lovelace",
                    "1 St James Square\nLondon SW1Y 4LB",
                    "ada.lovelace@example.com"),
                new Data(
                    "Grace Hopper",
                    "1234 Compiler St\nArlington, VA 22201",
                    "grace.hopper@example.com"),
                new Data(
                    "Linus Torvalds",
                    "5678 Penguin Way\nPortland, OR 97202",
                    "linus.torvalds@example.com"),
                new Data(
                    "Margaret Hamilton",
                    "42 Apollo Ave\nCambridge, MA 02139",
                    "margaret.hamilton@example.com"),
                new Data(
                    "Donald Knuth",
                    "TAOCP Lane 1\nStanford, CA 94305",
                    "donald.knuth@example.com"),
                new Data(
                    "Edsger Dijkstra",
                    "Postvak 13\n5612 AZ Eindhoven",
                    "edsger.dijkstra@example.com"),
                new Data(
                    "Alan Turing",
                    "Bletchley Park\nMilton Keynes MK3 6EB",
                    "alan.turing@example.com"),
                new Data(
                    "Barbara Liskov",
                    "77 Massachusetts Ave\nCambridge, MA 02139",
                    "barbara.liskov@example.com"),
                new Data(
                    "Niklaus Wirth",
                    "ETH Zentrum\n8092 Zürich",
                    "niklaus.wirth@example.com"),
                new Data(
                    "Tony Hoare",
                    "Wolfson Building\nOxford OX1 3QD",
                    "tony.hoare@example.com")));
    data.sort((a, b) -> a.name().compareTo(b.name()));
    return data;
  }

  /// Returns the maximum name / address-line / email widths in the dataset, mirroring upstream's
  /// `constraint_len_calculator`.
  static ColumnWidths constraintLenCalculator(List<Data> items) {
    int nameLen = 0;
    int addressLen = 0;
    int emailLen = 0;
    for (Data d : items) {
      int n = Wcwidth.width(d.name());
      if (n > nameLen) nameLen = n;
      for (String line : d.address().split("\n", -1)) {
        int w = Wcwidth.width(line);
        if (w > addressLen) addressLen = w;
      }
      int e = Wcwidth.width(d.email());
      if (e > emailLen) emailLen = e;
    }
    return new ColumnWidths(nameLen, addressLen, emailLen);
  }
}
