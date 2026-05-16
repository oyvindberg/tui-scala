package jatatui.react.examples.selectablelist;

import static jatatui.react.Components.box;
import static jatatui.react.Components.column;
import static jatatui.react.Components.component;
import static jatatui.react.Components.fill;
import static jatatui.react.Components.length;
import static jatatui.react.Components.text;

import jatatui.components.selectablelist.SelectableListProps;
import jatatui.core.style.Color;
import jatatui.core.style.Style;
import jatatui.react.Element;
import jatatui.react.ReactApp;
import jatatui.widgets.Borders;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/// Runnable demo for [jatatui.components.selectablelist.SelectableList].
///
/// The list is intentionally long (60 rows in a 10-row viewport-ish area) and contains a mix of
/// non-activatable `Header` rows and activatable `Item` rows, so the demo exercises:
///   - the vertical-right scrollbar that auto-shows when content overflows,
///   - Up/Down skipping over non-activatable rows,
///   - mouse-wheel scrolling without moving selection,
///   - Enter/click to activate (records the most-recently-activated row in a status line).
public final class SelectableListExample {

  /// Heterogeneous payload — headers decorate the list, items are activatable.
  sealed interface Row {
    record Header(String title) implements Row {}

    record Item(String name) implements Row {}
  }

  public static void main(String[] args) throws IOException {
    ReactApp.run(app());
  }

  static Element app() {
    return component(
        ctx -> {
          // Build a long list: 6 groups × (1 header + 10 items) = 66 rows.
          List<Row> rows = buildRows();

          // Initial selection: the first activatable row (idx 1, skipping the first header).
          var selected = ctx.useState(() -> 1);
          var lastActivated = ctx.useState(() -> "(none)");

          SelectableListProps.RowRenderer<Row> renderer =
              (row, isSel) ->
                  switch (row) {
                    case Row.Header h ->
                        text(
                            "── " + h.title() + " ─────────────────────────",
                            Style.empty().withFg(Color.MAGENTA));
                    case Row.Item it ->
                        text(
                            (isSel ? "> " : "  ") + it.name(),
                            isSel
                                ? Style.empty().withFg(Color.BLACK).withBg(Color.LIGHT_YELLOW)
                                : Style.empty().withFg(Color.WHITE));
                  };

          Element listEl =
              box(
                  " SelectableList — long list with scrollbar ",
                  Borders.ALL,
                  jatatui.components.Components.selectableList(
                      SelectableListProps.of(
                              rows,
                              r -> r instanceof Row.Item,
                              renderer,
                              selected.get(),
                              selected::set)
                          .withOnActivate(
                              r -> {
                                if (r instanceof Row.Item it) lastActivated.set(it.name());
                              })
                          .withFocusId("selectable-list-demo")
                          .withAutoFocus(true)));

          Row sel = rows.get(Math.max(0, Math.min(selected.get(), rows.size() - 1)));
          String selLabel =
              switch (sel) {
                case Row.Header h -> "(header: " + h.title() + ")";
                case Row.Item it -> it.name();
              };

          Element status =
              box(
                  " Status ",
                  Borders.ALL,
                  text(
                      "Selected: "
                          + selLabel
                          + "   (idx "
                          + selected.get()
                          + " / "
                          + rows.size()
                          + ")",
                      Style.empty().withFg(Color.YELLOW)),
                  text(
                      "Last activated: " + lastActivated.get(),
                      Style.empty().withFg(Color.LIGHT_GREEN)),
                  text(
                      "Up/Down: move (skips headers) - Enter or click: activate - Wheel: scroll -"
                          + " Esc to quit",
                      Style.empty().withFg(Color.GRAY)));

          return column(fill(1, listEl), length(5, status));
        });
  }

  private static List<Row> buildRows() {
    List<Row> rows = new ArrayList<>();
    String[] groups = {"Alpha", "Bravo", "Charlie", "Delta", "Echo", "Foxtrot"};
    for (String g : groups) {
      rows.add(new Row.Header(g));
      for (int i = 1; i <= 10; i++) {
        rows.add(new Row.Item(g.toLowerCase() + "-item-" + i));
      }
    }
    return rows;
  }
}
