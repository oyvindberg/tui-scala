package jatatui.components.table;

import static jatatui.react.Components.component;
import static jatatui.react.Components.widget;

import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import jatatui.core.widgets.Widget;
import jatatui.react.Element;
import jatatui.widgets.Borders;
import jatatui.widgets.block.Block;
import jatatui.widgets.table.HighlightSpacing;
import jatatui.widgets.table.Row;
import jatatui.widgets.table.TableCell;
import jatatui.widgets.table.TableState;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import tui.crossterm.KeyCode;

/// React-style Table component.
///
/// Wraps the existing [jatatui.widgets.table.Table] widget. Selection is "controlled" via
/// [TableProps#selectedRow] / [TableProps#onSelectChange] — the parent owns the index. The
/// widget's [TableState] (which also tracks the visible-window scroll offset) is held as a
/// `useRef` so it survives across renders; on each render the selection is synced down from
/// props.
///
/// Focus: registers via `useFocus` with the optional id from props. When focused:
///   - `Up` / `Down`: invokes `onSelectChange` with the next/previous row index (clamped to
///     `[0, rows.size())`).
///   - `Enter`: invokes `onActivate` (when present) with the current selected row.
public final class Table {
  private Table() {}

  /// Builds the Table element from typed-row props.
  public static <T> Element of(TableProps<T> props) {
    return component(
        ctx -> {
          var stateRef = ctx.useRef(TableState::new);
          boolean focused = ctx.useFocus(props.focusId(), props.autoFocus());

          int rowCount = props.rows().size();
          int selected = clamp(props.selectedRow(), 0, Math.max(0, rowCount - 1));
          // Sync controlled selection into the underlying widget state.
          if (rowCount == 0) {
            stateRef.get().select(Optional.empty());
          } else {
            stateRef.get().select(Optional.of(selected));
          }

          if (focused && rowCount > 0) {
            ctx.onKey(
                new KeyCode.Up(),
                () -> {
                  int next = Math.max(0, selected - 1);
                  if (next != selected) props.onSelectChange().accept(next);
                });
            ctx.onKey(
                new KeyCode.Down(),
                () -> {
                  int next = Math.min(rowCount - 1, selected + 1);
                  if (next != selected) props.onSelectChange().accept(next);
                });
            props
                .onActivate()
                .ifPresent(cb -> ctx.onKey(new KeyCode.Enter(), () -> cb.accept(selected)));
          }

          jatatui.widgets.table.Table widgetTable = buildWidget(props, focused);
          TableState state = stateRef.get();
          Widget adapter = (area, buf) -> widgetTable.render(area, buf, state);
          return widget(adapter);
        });
  }

  /// Convenience factory for the "quick path": a list of stringly-typed rows.
  ///
  /// Each row is a `List<String>` whose length should match `headers.size()`. Cell extractors
  /// are derived as positional getters into the row list (missing cells render as empty).
  public static Element ofStrings(
      String title,
      List<String> headers,
      List<List<String>> rows,
      List<jatatui.core.layout.Constraint> columnWidths,
      int selectedRow,
      java.util.function.IntConsumer onSelectChange,
      Optional<java.util.function.IntConsumer> onActivate,
      Optional<String> focusId,
      boolean autoFocus) {
    List<java.util.function.Function<List<String>, String>> extractors =
        new ArrayList<>(headers.size());
    for (int i = 0; i < headers.size(); i++) {
      final int idx = i;
      extractors.add(row -> idx < row.size() ? row.get(idx) : "");
    }
    return of(
        new TableProps<>(
            title,
            headers,
            rows,
            extractors,
            columnWidths,
            selectedRow,
            onSelectChange,
            onActivate,
            focusId,
            autoFocus));
  }

  // ---- Internal ----

  private static <T> jatatui.widgets.table.Table buildWidget(TableProps<T> props, boolean focused) {
    Style headerStyle = Style.empty().withFg(Color.YELLOW).withAddModifier(Modifier.BOLD);
    Style highlightStyle =
        focused
            ? Style.empty().withBg(Color.BLUE).withFg(Color.WHITE).withAddModifier(Modifier.BOLD)
            : Style.empty().withBg(Color.DARK_GRAY).withFg(Color.WHITE);

    Row header = Row.ofStrings(props.headers()).withStyle(headerStyle);

    List<Row> rows = new ArrayList<>(props.rows().size());
    for (T row : props.rows()) {
      List<TableCell> cells = new ArrayList<>(props.cellExtractors().size());
      for (java.util.function.Function<T, String> extractor : props.cellExtractors()) {
        cells.add(TableCell.of(extractor.apply(row)));
      }
      rows.add(Row.of(cells));
    }

    Block block =
        Block.empty()
            .withTitle(focused ? " " + props.title() + " * " : " " + props.title() + " ")
            .withBorders(Borders.ALL);

    return jatatui.widgets.table.Table.of(rows, props.columnWidths())
        .withHeader(header)
        .withBlock(block)
        .withRowHighlightStyle(highlightStyle)
        .withHighlightSymbol("> ")
        .withHighlightSpacing(HighlightSpacing.Always);
  }

  private static int clamp(int v, int lo, int hi) {
    if (v < lo) return lo;
    if (v > hi) return hi;
    return v;
  }
}
