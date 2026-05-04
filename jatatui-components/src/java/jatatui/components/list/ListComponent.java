package jatatui.components.list;

import static jatatui.react.Components.component;

import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import jatatui.react.Element;
import jatatui.react.RenderContext;
import jatatui.widgets.Borders;
import jatatui.widgets.block.Block;
import jatatui.widgets.list.HighlightSpacing;
import jatatui.widgets.list.ListItem;
import jatatui.widgets.list.ListState;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import tui.crossterm.KeyCode;

/// React-style List component wrapping the [jatatui.widgets.list.List] stateful widget.
///
/// Selection is "controlled" via [ListProps#selected] / [ListProps#onSelectChange] — the
/// parent owns the index. The widget's [ListState] (which tracks the visible-window scroll
/// offset) is held as a `useRef` so it survives across renders; on each render the selection
/// is synced down from props.
///
/// Focus: registers via `useFocus` with the optional id from props. When focused:
///   - `Up` / `k`: move selection up
///   - `Down` / `j`: move selection down
///   - `Home` / `End`: jump to first / last
///   - `Enter`: invokes `onActivate` (when present)
///
/// Mouse: each visible row gets a click handler. Clicking an unselected row calls
/// `onSelectChange`; clicking the already-selected row calls `onActivate` (when present).
///
/// Memoization: not used here because `onSelectChange` / `onActivate` are typically fresh
/// lambdas per render — `pureComponent` would always miss the cache. Wrap with
/// `pureComponent(props, ListComponent::of)` if you keep callback identities stable.
public final class ListComponent {
  private ListComponent() {}

  /// Build the React Element from typed props.
  public static <T> Element of(ListProps<T> props) {
    return component(
        ctx -> {
          var stateRef = ctx.useRef(ListState::empty);
          boolean focused = ctx.useFocus(props.focusId(), props.autoFocus());

          int itemCount = props.items().size();
          int selected =
              itemCount == 0 ? -1 : clamp(props.selected(), 0, itemCount - 1);

          // Sync controlled selection into the widget state.
          if (selected < 0) {
            stateRef.get().select(Optional.empty());
          } else {
            stateRef.get().select(Optional.of(selected));
          }

          if (focused && itemCount > 0) {
            registerKeyHandlers(ctx, props, selected, itemCount);
          }

          jatatui.widgets.list.List widget = buildWidget(props, focused);
          ListState state = stateRef.get();

          // Capture the inner area + post-render offset so we can register per-row click
          // handlers that map (row -> item index) correctly even after scrolling.
          jatatui.core.widgets.Widget adapter =
              (area, buf) -> {
                Block block = widget.block.orElseGet(Block::empty);
                Rect inner = block.inner(area);
                widget.render(area, buf, state);
                if (!inner.isEmpty() && itemCount > 0) {
                  registerRowClickHandlers(ctx, props, inner, state.offset(), itemCount, selected);
                }
              };
          return jatatui.react.Components.widget(adapter);
        });
  }

  /// Convenience factory: minimum-arg "string items + onSelectChange" form.
  public static Element ofStrings(
      String title,
      List<String> items,
      int selected,
      java.util.function.IntConsumer onSelectChange) {
    return of(
        new ListProps<>(
            title,
            items,
            java.util.function.Function.identity(),
            selected,
            onSelectChange,
            Optional.empty(),
            Optional.empty(),
            true));
  }

  /// Convenience factory: string items with both selection-change and activation callbacks.
  public static Element ofStrings(
      String title,
      List<String> items,
      int selected,
      java.util.function.IntConsumer onSelectChange,
      Runnable onActivate) {
    return of(
        new ListProps<>(
            title,
            items,
            java.util.function.Function.identity(),
            selected,
            onSelectChange,
            Optional.of(onActivate),
            Optional.empty(),
            true));
  }

  // ---- Internal ----

  private static <T> void registerKeyHandlers(
      RenderContext ctx, ListProps<T> props, int selected, int itemCount) {
    Runnable up =
        () -> {
          int next = Math.max(0, selected - 1);
          if (next != selected) props.onSelectChange().accept(next);
        };
    Runnable down =
        () -> {
          int next = Math.min(itemCount - 1, selected + 1);
          if (next != selected) props.onSelectChange().accept(next);
        };
    Runnable first =
        () -> {
          if (selected != 0) props.onSelectChange().accept(0);
        };
    Runnable last =
        () -> {
          int target = itemCount - 1;
          if (selected != target) props.onSelectChange().accept(target);
        };
    ctx.onKey(new KeyCode.Up(), up);
    ctx.onKey(new KeyCode.Down(), down);
    ctx.onKey(new KeyCode.Char('k'), up);
    ctx.onKey(new KeyCode.Char('j'), down);
    ctx.onKey(new KeyCode.Home(), first);
    ctx.onKey(new KeyCode.End(), last);
    props.onActivate().ifPresent(cb -> ctx.onKey(new KeyCode.Enter(), cb));
  }

  private static <T> void registerRowClickHandlers(
      RenderContext ctx,
      ListProps<T> props,
      Rect inner,
      int offset,
      int itemCount,
      int selected) {
    int rows = inner.height();
    for (int i = 0; i < rows; i++) {
      int itemIndex = offset + i;
      if (itemIndex >= itemCount) break;
      Rect rowArea = new Rect(inner.x(), inner.y() + i, inner.width(), 1);
      int finalIndex = itemIndex;
      ctx.onClick(
          rowArea,
          () -> {
            if (finalIndex == selected) {
              props.onActivate().ifPresent(Runnable::run);
            } else {
              props.onSelectChange().accept(finalIndex);
            }
          });
    }
  }

  private static <T> jatatui.widgets.list.List buildWidget(ListProps<T> props, boolean focused) {
    Style frameStyle =
        focused ? Style.empty().withFg(Color.YELLOW) : Style.empty().withFg(Color.GRAY);
    Style highlightStyle =
        focused
            ? Style.empty().withBg(Color.BLUE).withFg(Color.WHITE).withAddModifier(Modifier.BOLD)
            : Style.empty().withBg(Color.DARK_GRAY).withFg(Color.WHITE);

    Block block =
        Block.empty()
            .withTitle(focused ? " " + props.title() + " * " : " " + props.title() + " ")
            .withTitleStyle(frameStyle)
            .withBorders(Borders.ALL)
            .withBorderStyle(frameStyle);

    List<ListItem> items = new ArrayList<>(props.items().size());
    for (T item : props.items()) {
      items.add(ListItem.of(props.labelFn().apply(item)));
    }

    return jatatui.widgets.list.List.of(items)
        .withBlock(block)
        .withHighlightStyle(highlightStyle)
        .withHighlightSymbol("> ")
        .withHighlightSpacing(HighlightSpacing.Always);
  }

  private static int clamp(int v, int lo, int hi) {
    if (v < lo) return lo;
    if (v > hi) return hi;
    return v;
  }
}
