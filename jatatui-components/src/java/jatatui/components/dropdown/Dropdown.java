package jatatui.components.dropdown;

import static jatatui.react.Components.*;

import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import jatatui.react.Element;
import jatatui.react.State;
import jatatui.widgets.Borders;
import java.util.ArrayList;
import java.util.List;
import tui.crossterm.KeyCode;

/// Single-select dropdown.
///
/// Closed: a focusable bordered cell showing `"label: <value> ▾"`. Enter / Space / Down opens it.
/// Open: a portal positioned just below the trigger renders the option list. Up/Down move the
/// highlight; Enter commits the highlighted item; Esc closes without committing. A backdrop
/// click outside also closes without committing.
public final class Dropdown {
  private Dropdown() {}

  public static Element of(DropdownProps props) {
    return component(
        ctx -> {
          State<Boolean> openState = ctx.useState(() -> false);
          State<Integer> highlightState = ctx.useState(props::selectedIndex);

          boolean focused = ctx.useFocus(props.focusId(), props.autoFocus());

          // Trigger key handlers.
          if (focused && !openState.get()) {
            Runnable openIt =
                () -> {
                  highlightState.set(props.selectedIndex());
                  openState.set(true);
                };
            ctx.onKey(new KeyCode.Enter(), openIt);
            ctx.onKey(new KeyCode.Char(' '), openIt);
            ctx.onKey(new KeyCode.Down(), openIt);
          }

          // Trigger click handler — clicking the trigger opens it.
          if (!openState.get()) {
            ctx.onClick(
                e -> {
                  highlightState.set(props.selectedIndex());
                  openState.set(true);
                  e.stopPropagation();
                });
          }

          // Build the trigger element + maybe an open-overlay.
          String selected =
              (props.selectedIndex() >= 0 && props.selectedIndex() < props.items().size())
                  ? props.items().get(props.selectedIndex())
                  : "";
          String triggerText = "  " + props.label() + ": " + selected + "  v";
          Style triggerStyle = focused ? props.focusedStyle() : props.style();

          Element trigger =
              box(
                  "",
                  Borders.ALL,
                  text(triggerText, triggerStyle));

          if (!openState.get()) {
            return trigger;
          }

          // Open: also render an overlay. Anchor to this fiber's area.
          Rect screen = ctx.frame().area();
          Rect anchor = ctx.area().orElse(screen);
          Rect listArea = listAreaBelow(anchor, props.items().size(), screen);

          int hi = clamp(highlightState.get(), 0, Math.max(0, props.items().size() - 1));

          Element overlay =
              component(
                  c -> {
                    c.onClick(e -> e.stopPropagation()); // eat clicks on the list itself
                    c.onKey(
                        new KeyCode.Up(),
                        e -> {
                          highlightState.set(Math.max(0, hi - 1));
                          e.stopPropagation();
                        });
                    c.onKey(
                        new KeyCode.Down(),
                        e -> {
                          highlightState.set(Math.min(props.items().size() - 1, hi + 1));
                          e.stopPropagation();
                        });
                    c.onKey(
                        new KeyCode.Enter(),
                        e -> {
                          props.onChange().accept(hi);
                          openState.set(false);
                          e.stopPropagation();
                        });
                    c.onKey(
                        new KeyCode.Esc(),
                        e -> {
                          openState.set(false);
                          e.stopPropagation();
                        });
                    return optionsList(props.items(), hi);
                  });

          Element backdrop =
              component(
                  c -> {
                    c.onClick(
                        e -> {
                          openState.set(false);
                          e.stopPropagation();
                        });
                    return empty();
                  });

          return column(trigger, portal(backdrop, screen), portal(overlay, listArea));
        });
  }

  private static Element optionsList(List<String> items, int highlightedIndex) {
    List<Element> rows = new ArrayList<>(items.size());
    for (int i = 0; i < items.size(); i++) {
      String item = items.get(i);
      boolean hi = i == highlightedIndex;
      Style rowStyle =
          hi
              ? Style.empty().withBg(new Color.Blue()).withFg(new Color.White()).withAddModifier(Modifier.BOLD)
              : Style.empty();
      String prefix = hi ? "> " : "  ";
      rows.add(length(1, text(prefix + item, rowStyle)));
    }
    return box("", Borders.ALL, column(rows.toArray(new Element[0])));
  }

  private static Rect listAreaBelow(Rect anchor, int itemCount, Rect screen) {
    int width = anchor.width();
    int height = Math.min(itemCount + 2, screen.height() - (anchor.y() + anchor.height())); // +2 for borders
    if (height < 3) height = Math.min(itemCount + 2, screen.height()); // fall back: clamp to screen
    int x = anchor.x();
    int y = anchor.y() + anchor.height();
    return new Rect(x, y, width, Math.max(3, height));
  }

  private static int clamp(int v, int lo, int hi) {
    return Math.max(lo, Math.min(hi, v));
  }
}
