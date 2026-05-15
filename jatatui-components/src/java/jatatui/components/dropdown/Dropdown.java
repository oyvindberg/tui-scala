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
          boolean open = openState.get();

          // Auto-close when focus moves elsewhere — Tab to next field, programmatic focus
          // change, etc. The backdrop closes on outside click; this catches every other path.
          if (open && !focused) {
            openState.set(false);
            open = false;
          }

          // Trigger key handlers (focused + closed → Enter/Space/Down opens).
          if (focused && !open) {
            Runnable openIt =
                () -> {
                  highlightState.set(props.selectedIndex());
                  openState.set(true);
                };
            ctx.onKey(new KeyCode.Enter(), openIt);
            ctx.onKey(new KeyCode.Char(' '), openIt);
            ctx.onKey(new KeyCode.Down(), openIt);
          }

          // Trigger click — opens the dropdown AND focuses self so subsequent keys land here.
          if (!open) {
            ctx.onClick(
                e -> {
                  highlightState.set(props.selectedIndex());
                  openState.set(true);
                  props.focusId().ifPresent(ctx::focus);
                  e.stopPropagation();
                });
          }

          // Open-list key handlers — registered on the DROPDOWN'S fiber (not on the overlay's
          // portal subtree) so they sit in the focused bubble chain and actually fire.
          if (focused && open) {
            int hi = clamp(highlightState.get(), 0, Math.max(0, props.items().size() - 1));
            ctx.onKey(
                new KeyCode.Up(),
                e -> {
                  highlightState.set(Math.max(0, hi - 1));
                  e.stopPropagation();
                });
            ctx.onKey(
                new KeyCode.Down(),
                e -> {
                  highlightState.set(Math.min(props.items().size() - 1, hi + 1));
                  e.stopPropagation();
                });
            ctx.onKey(
                new KeyCode.Enter(),
                e -> {
                  props.onChange().accept(hi);
                  openState.set(false);
                  e.stopPropagation();
                });
            ctx.onKey(
                new KeyCode.Esc(),
                e -> {
                  openState.set(false);
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

          Element trigger = box("", Borders.ALL, text(triggerText, triggerStyle));

          if (!open) {
            return trigger;
          }

          // Open: render the option list as a portal anchored just below the trigger.
          Rect screen = ctx.frame().area();
          Rect anchor = ctx.area().orElse(screen);
          Rect listArea = listAreaBelow(anchor, props.items().size(), screen);

          int hi = clamp(highlightState.get(), 0, Math.max(0, props.items().size() - 1));

          Element overlay =
              component(
                  c -> {
                    // Eat clicks on the list background (gaps between rows). Per-row clicks are
                    // attached to each row inside optionsList.
                    c.onClick(e -> e.stopPropagation());
                    // Stack Clear under the option list so the overlay is opaque — without
                    // this, underlying widgets' borders / text bleed through gaps in our rows.
                    return stack(
                        widget(jatatui.widgets.Clear.instance()),
                        optionsList(props.items(), hi, props.onChange(), openState));
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

  private static Element optionsList(
      List<String> items,
      int highlightedIndex,
      java.util.function.IntConsumer onChange,
      State<Boolean> openState) {
    List<Element> rows = new ArrayList<>(items.size());
    for (int i = 0; i < items.size(); i++) {
      String item = items.get(i);
      boolean hi = i == highlightedIndex;
      Style rowStyle =
          hi
              ? Style.empty().withBg(new Color.Blue()).withFg(new Color.White()).withAddModifier(Modifier.BOLD)
              : Style.empty();
      String prefix = hi ? "> " : "  ";
      int idx = i;
      Element row =
          component(
              c -> {
                c.onClick(
                    e -> {
                      onChange.accept(idx);
                      openState.set(false);
                      e.stopPropagation();
                    });
                return text(prefix + item, rowStyle);
              });
      rows.add(length(1, row));
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
