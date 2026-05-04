package jatatui.react.examples.list;

import static jatatui.components.list.ListComponents.list;
import static jatatui.react.Components.box;
import static jatatui.react.Components.column;
import static jatatui.react.Components.component;
import static jatatui.react.Components.fill;
import static jatatui.react.Components.length;
import static jatatui.react.Components.text;

import jatatui.core.style.Color;
import jatatui.core.style.Style;
import jatatui.react.Element;
import jatatui.react.ReactApp;
import jatatui.widgets.Borders;
import java.io.IOException;
import java.util.List;

/// Runnable demo for [jatatui.components.list.ListComponent].
///
/// Demonstrates:
///   - `list(...)` with controlled selection driven by `useState`
///   - Up/Down or k/j to move; Home/End to jump; Enter or click to "activate" an item
///   - A status line that records the most recently activated item
///   - Esc / Ctrl-C to quit (handled globally by [ReactApp])
public final class ListExample {

  public static void main(String[] args) throws IOException {
    ReactApp.run(app());
  }

  private static final List<String> FRUITS =
      List.of(
          "Apple",
          "Banana",
          "Cherry",
          "Date",
          "Elderberry",
          "Fig",
          "Grape",
          "Honeydew",
          "Kiwi",
          "Lemon");

  static Element app() {
    return component(
        ctx -> {
          var selected = ctx.useState(() -> 0);
          var lastActivated = ctx.useState(() -> "(none)");

          Element listEl =
              list(
                  " Fruits ",
                  FRUITS,
                  selected.get(),
                  selected::set,
                  () -> lastActivated.set(FRUITS.get(selected.get())));

          Element status =
              box(
                  " Status ",
                  Borders.ALL,
                  text(
                      "Selected: " + FRUITS.get(selected.get()),
                      Style.empty().withFg(Color.YELLOW)),
                  text(
                      "Last activated: " + lastActivated.get(),
                      Style.empty().withFg(Color.LIGHT_GREEN)),
                  text(
                      "(Up/Down or k/j to move - Enter or click to activate - Esc/Ctrl-C to quit)",
                      Style.empty().withFg(Color.GRAY)));

          return column(fill(1, listEl), length(6, status));
        });
  }
}
