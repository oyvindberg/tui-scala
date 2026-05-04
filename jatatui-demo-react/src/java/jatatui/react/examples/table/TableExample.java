package jatatui.react.examples.table;

import static jatatui.react.Components.column;
import static jatatui.react.Components.component;
import static jatatui.react.Components.fill;
import static jatatui.react.Components.length;
import static jatatui.react.Components.text;

import jatatui.components.Components;
import jatatui.components.table.TableProps;
import jatatui.core.layout.Constraint;
import jatatui.core.layout.Margin;
import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import jatatui.react.Element;
import jatatui.react.ReactApp;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/// Runnable smoke test for the React-style [jatatui.components.table.Table] component.
///
///   - Up / Down: move selection
///   - Enter: emit "activated row N" to the status line
///   - Esc / Ctrl-C: quit
public final class TableExample {

  public static void main(String[] args) throws IOException {
    ReactApp.run(app());
  }

  // ---- Demo data ----

  record Host(int id, String name, String status, String lastSeen) {}

  static final List<Host> HOSTS =
      List.of(
          new Host(1, "alpha", "online", "2026-05-04 09:14"),
          new Host(2, "bravo", "online", "2026-05-04 09:13"),
          new Host(3, "charlie", "degraded", "2026-05-04 08:58"),
          new Host(4, "delta", "online", "2026-05-04 09:14"),
          new Host(5, "echo", "offline", "2026-05-03 22:01"),
          new Host(6, "foxtrot", "online", "2026-05-04 09:12"),
          new Host(7, "golf", "online", "2026-05-04 09:14"),
          new Host(8, "hotel", "rebooting", "2026-05-04 09:10"),
          new Host(9, "india", "online", "2026-05-04 09:14"),
          new Host(10, "juliet", "online", "2026-05-04 09:14"),
          new Host(11, "kilo", "offline", "2026-05-02 14:22"),
          new Host(12, "lima", "online", "2026-05-04 09:14"));

  static final List<String> HEADERS = List.of("ID", "Name", "Status", "Last Seen");

  static final List<Function<Host, String>> EXTRACTORS =
      List.of(h -> Integer.toString(h.id()), Host::name, Host::status, Host::lastSeen);

  static final List<Constraint> COLUMN_WIDTHS =
      List.of(
          new Constraint.Length(4),
          new Constraint.Length(10),
          new Constraint.Length(10),
          new Constraint.Fill(1));

  // ---- Top-level component ----

  static Element app() {
    return component(
        ctx -> {
          var selected = ctx.useState(() -> 0);
          var lastActivated = ctx.useState(() -> Optional.<Host>empty());

          Element tableEl =
              Components.table(
                  new TableProps<>(
                      "Hosts",
                      HEADERS,
                      HOSTS,
                      EXTRACTORS,
                      COLUMN_WIDTHS,
                      selected.get(),
                      selected::set,
                      Optional.of(idx -> lastActivated.set(Optional.of(HOSTS.get(idx)))),
                      Optional.of("hosts-table"),
                      true));

          return column(
                  fill(1, tableEl),
                  length(1, statusLine(selected.get(), lastActivated.get())),
                  length(
                      1,
                      text(
                          "  Up/Down to move, Enter to activate, Esc to quit",
                          Style.empty().withFg(Color.DARK_GRAY))))
              .withMargin(new Margin(1, 1));
        });
  }

  static Element statusLine(int selectedIdx, Optional<Host> activated) {
    Host current = HOSTS.get(selectedIdx);
    String activatedSuffix =
        activated.map(h -> "  |  last activated: #" + h.id() + " " + h.name()).orElse("");
    String line =
        "  selected: #"
            + current.id()
            + " "
            + current.name()
            + " ("
            + current.status()
            + ", "
            + current.lastSeen()
            + ")"
            + activatedSuffix;
    return text(line, Style.empty().withFg(Color.LIGHT_CYAN).withAddModifier(Modifier.BOLD));
  }
}
