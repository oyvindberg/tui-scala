package jatatui.components.selectablelist;

import static jatatui.react.Components.*;

import jatatui.core.layout.Rect;
import jatatui.react.Element;
import jatatui.react.KeyEvent;
import jatatui.react.MouseEvent;
import jatatui.react.Ref;
import jatatui.react.State;
import jatatui.widgets.scrollbar.ScrollbarOrientation;
import java.util.ArrayList;
import java.util.List;
import tui.crossterm.KeyCode;

/// Keyboard + mouse + wheel-scrollable list of arbitrary [Element] rows with selection.
///
/// Differs from [jatatui.components.list.ListComponent]:
///   - rows are [Element]s, not String labels (caller supplies [SelectableListProps.RowRenderer]);
///   - rows can be **non-activatable** (decoration: headers, dividers, totals). Up/Down skips
///     them, Enter / Enter-on-click ignores them.
///
/// Selection is controlled (parent owns `selected`, child notifies via `onSelectChange`); scroll
/// offset is internal and clamped so the selected row is always visible. When the content
/// overflows the viewport and `props.showScrollbar()` is `true` (the default), a vertical-right
/// scrollbar is rendered alongside the rows so the user can see their position in the list.
///
/// Triggers:
///   - **Up / Down**: move selection to next/prev activatable row.
///   - **Enter**: invokes `onActivate(items.get(selected))`, if `onActivate` was set AND the
///     selected row is activatable.
///   - **Click on an activatable row**: selects it (if not already), or activates if it's the
///     current selection.
///   - **Mouse wheel**: scrolls the viewport without moving selection.
///
/// Keyboard handlers register on this component's fiber via [bubble-phase onKey], so a wrapping
/// screen's own arrow handlers won't double-fire while the list is focused. Wraps via
/// `stopPropagation()`.
public final class SelectableList {
  private SelectableList() {}

  private static final int SCROLL_STEP = 3;

  /// How long after a single click a click on the same row counts as a double-click. 500ms
  /// matches the OS double-click default on macOS / Windows / GTK at their stock settings.
  private static final long DOUBLE_CLICK_MS = 500;

  /// Internal: most-recently-clicked row and its timestamp. Shared across all row components
  /// of one list so clicking row A then row B doesn't trigger a "double-click" on B.
  record LastClick(int row, long timeMs) {
    static final LastClick NONE = new LastClick(-1, 0L);
  }

  public static <T> Element of(SelectableListProps<T> props) {
    return component(
        ctx -> {
          List<T> items = props.items();
          int n = items.size();

          // Indices of activatable rows — Up/Down skip everything else.
          List<Integer> activatableIdxs = new ArrayList<>();
          for (int i = 0; i < n; i++) {
            if (props.isActivatable().test(items.get(i))) activatableIdxs.add(i);
          }

          State<Integer> offsetState = ctx.useState(() -> 0);
          // Shared across all row components — see LastClick. Captured into renderRow so each
          // row's onClick can consult/update the same mutable state.
          Ref<LastClick> lastClickRef = ctx.useRef(() -> LastClick.NONE);
          int sel = n == 0 ? 0 : Math.max(0, Math.min(props.selected(), n - 1));

          int areaHeight = ctx.area().map(Rect::height).orElse(20);
          int visibleH = Math.max(1, areaHeight);

          // Auto-scroll only when selection actually changed (or on first render). Without this
          // gate, mouse-wheel scrolls would be reverted on the next render — sel hadn't moved
          // but the auto-scroll would re-pull offset back to keep sel in view.
          var prevSelRef = ctx.useRef(() -> -1);
          int prevSel = prevSelRef.get();
          prevSelRef.set(sel);
          boolean selChanged = sel != prevSel;

          int offset = offsetState.get();
          if (selChanged) {
            int newOffset =
                (sel < offset) ? sel : (sel >= offset + visibleH) ? sel - visibleH + 1 : offset;
            if (newOffset != offset) {
              offsetState.set(newOffset);
              offset = newOffset;
            }
          }

          // Claim focus so onKey for Up/Down/Enter fires when the user types into the list. We
          // treat the list itself as one focusable entity rather than wiring each row into the
          // FocusManager.
          boolean focused = ctx.useFocus(props.focusId(), props.autoFocus());

          if (focused) {
            ctx.onKey(
                new KeyCode.Up(),
                (KeyEvent e) -> {
                  Integer next = moveSelection(activatableIdxs, sel, -1);
                  if (next != null) props.onSelectChange().accept(next);
                  e.stopPropagation();
                });
            ctx.onKey(
                new KeyCode.Down(),
                (KeyEvent e) -> {
                  Integer next = moveSelection(activatableIdxs, sel, +1);
                  if (next != null) props.onSelectChange().accept(next);
                  e.stopPropagation();
                });
            props
                .onActivate()
                .ifPresent(
                    cb ->
                        ctx.onKey(
                            new KeyCode.Enter(),
                            (KeyEvent e) -> {
                              if (n > 0 && props.isActivatable().test(items.get(sel))) {
                                cb.accept(items.get(sel));
                              }
                              e.stopPropagation();
                            }));
          }

          final int maxOffset = Math.max(0, n - 1);
          final int liveOffset = offset;
          ctx.onScroll(
              (MouseEvent e) -> {
                switch (e.kind()) {
                  case SCROLL_UP -> offsetState.set(Math.max(0, liveOffset - SCROLL_STEP));
                  case SCROLL_DOWN ->
                      offsetState.set(Math.min(maxOffset, liveOffset + SCROLL_STEP));
                  default -> {}
                }
              });

          // Render the visible window.
          List<Element> visible = new ArrayList<>();
          int upper = Math.min(n, offset + visibleH);
          for (int i = offset; i < upper; i++) {
            final int idx = i;
            final T item = items.get(i);
            visible.add(length(1, renderRow(props, item, idx, sel, lastClickRef)));
          }

          if (visible.isEmpty()) return empty();

          Element rowsColumn = column(visible.toArray(new Element[0]));

          // Render a vertical-right scrollbar when content overflows and the caller wants it.
          // We reserve the rightmost column for the scrollbar via row(fill, length) rather than
          // stacking it on top of the rows. Stacking causes a colour-bleed bug: paragraphs
          // (`text(content, style)`) call `buf.setStyle(area, style)`, painting the row's fg
          // onto every cell — including the rightmost. The scrollbar's thumb writes its glyph
          // with `Style.empty()`, and `Cell.setStyle` only patches what the style has set, so
          // the thumb inherits the row's fg. Reserving the column avoids the overpaint, and
          // the trade-off — content reflows by 1 column when overflow toggles — matches what
          // every GUI scrollbar does.
          //
          // Content overflows when n > visibleH; otherwise we render nothing (no scrollbar is
          // visually noise for short lists) so callers get the same behaviour as before.
          boolean overflows = props.showScrollbar() && n > visibleH;
          if (overflows) {
            Element bar =
                jatatui.components.scrollbar.Components.scrollbar(
                    offset, n, visibleH, ScrollbarOrientation.VerticalRight);
            return row(fill(1, rowsColumn), length(1, bar));
          }
          return rowsColumn;
        });
  }

  /// Move selection from `current` by `dir` (+1 / -1), skipping non-activatable rows. Returns
  /// `null` if there's no further activatable row in that direction.
  private static Integer moveSelection(List<Integer> activatableIdxs, int current, int dir) {
    if (activatableIdxs.isEmpty()) return null;
    int pos = activatableIdxs.indexOf(current);
    int newPos;
    if (pos < 0) {
      // current isn't activatable; find the nearest activatable in the given direction.
      if (dir > 0) {
        int found = -1;
        for (int i = 0; i < activatableIdxs.size(); i++) {
          if (activatableIdxs.get(i) > current) {
            found = i;
            break;
          }
        }
        newPos = found;
      } else {
        int found = -1;
        for (int i = activatableIdxs.size() - 1; i >= 0; i--) {
          if (activatableIdxs.get(i) < current) {
            found = i;
            break;
          }
        }
        newPos = found;
      }
    } else {
      newPos = pos + dir;
    }
    if (newPos < 0 || newPos >= activatableIdxs.size()) return null;
    return activatableIdxs.get(newPos);
  }

  private static <T> Element renderRow(
      SelectableListProps<T> props, T item, int idx, int selected, Ref<LastClick> lastClickRef) {
    final boolean isSel = idx == selected;
    final boolean activatable = props.isActivatable().test(item);
    return component(
        ctx -> {
          if (activatable) {
            ctx.onClick(
                (MouseEvent e) -> {
                  if (props.activateOnDoubleClick()) {
                    LastClick last = lastClickRef.get();
                    long now = System.currentTimeMillis();
                    if (idx == last.row() && (now - last.timeMs()) < DOUBLE_CLICK_MS) {
                      // Double-click: activate. Reset the click-tracking so a third quick
                      // click doesn't fire a second activation.
                      props.onActivate().ifPresent(cb -> cb.accept(item));
                      lastClickRef.set(LastClick.NONE);
                    } else {
                      // Single-click: select only. Note the row + time so a subsequent click
                      // can be detected as a double-click.
                      if (!isSel) props.onSelectChange().accept(idx);
                      lastClickRef.set(new LastClick(idx, now));
                    }
                  } else {
                    // Legacy: click on the already-selected row activates; else just select.
                    if (isSel) {
                      props.onActivate().ifPresent(cb -> cb.accept(item));
                    } else {
                      props.onSelectChange().accept(idx);
                    }
                  }
                  e.stopPropagation();
                });
          }
          return props.rowRenderer().render(item, isSel);
        });
  }
}
