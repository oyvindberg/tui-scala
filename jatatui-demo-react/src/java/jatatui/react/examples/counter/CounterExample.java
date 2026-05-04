package jatatui.react.examples.counter;

import static jatatui.react.Components.*;

import jatatui.core.style.Color;
import jatatui.core.style.Style;
import jatatui.react.Element;
import jatatui.react.ReactApp;
import jatatui.widgets.Borders;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import tui.crossterm.KeyCode;

/// What user code looks like with this API.
///
/// Demonstrates:
///   - `component(ctx -> ...)` for stateful function components
///   - `useState`, `useFocus`, `useEffect`, `onKey` (focus-scoped)
///   - `column` / `row` / `box` with per-child `length(...)` / `fill(...)` constraints
///   - `pureComponent` with a record props for free memoization
public final class CounterExample {

  public static void main(String[] args) throws IOException {
    ReactApp.run(app());
  }

  // ---- Top-level layout ----

  static Element app() {
    return column(
        length(9, counter()),                     // counter wants exactly 9 rows
        fill(1, todoList(List.of("Port ratatui",  // todoList expands
                                  "Add React layer",
                                  "Wire memo + sized")))
    ).withSpacing(1).withMargin(new jatatui.core.layout.Margin(2, 1));
  }

  // ---- Counter (stateful, focusable) ----

  static Element counter() {
    return component(
        ctx -> {
          var count = ctx.useState(() -> 0);
          boolean focused = ctx.useFocus(Optional.of("counter"), true);

          if (focused) {
            ctx.onKey(new KeyCode.Up(), () -> count.update(n -> n + 1));
            ctx.onKey(new KeyCode.Down(), () -> count.update(n -> n - 1));
          }

          return box(
              focused ? " Counter * " : " Counter ",
              Borders.ALL,
              text("Count: " + count.get(),
                   Style.empty().withFg(focused ? Color.YELLOW : Color.CYAN)),
              row(
                  button(
                      "[ + ]",
                      Style.empty().withFg(Color.GREEN),
                      () -> count.update(n -> n + 1)),
                  button(
                      "[ - ]",
                      Style.empty().withFg(Color.RED),
                      () -> count.update(n -> n - 1))),
              text("(Tab to focus, ↑/↓ when focused, click ± buttons, Esc to quit)",
                   Style.empty().withFg(Color.GRAY)));
        });
  }

  // ---- Todo list (pure component over an immutable props record) ----

  /// Props is a record → equals() is structural → memoized for free. Body skipped while equal.
  record TodoListProps(List<String> items) {}

  static Element todoList(List<String> items) {
    return pureComponent(
        new TodoListProps(items),
        props ->
            box(
                " Todo (pure-memoized) ",
                Borders.ALL,
                forEach(
                    props.items(),
                    /* keyFn */ s -> s,
                    /* render */ s -> text("• " + s, Style.empty().withFg(Color.WHITE)))));
  }
}
