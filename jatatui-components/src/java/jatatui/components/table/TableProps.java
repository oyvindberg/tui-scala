package jatatui.components.table;

import jatatui.core.layout.Constraint;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntConsumer;

/// Props for the React-style [Table] component.
///
/// Generic over the row data type `T`. Each column is described by a header label and a
/// [Function] that extracts the cell text for that column from a row value.
///
/// Selection is "controlled": the parent owns the `selectedRow` index and is notified of
/// requested changes through `onSelectChange`. The component itself holds no selection
/// state — only the underlying widget's scroll offset (a [jatatui.widgets.table.TableState])
/// is kept across renders via `useRef`.
///
/// `onActivate` (optional) fires when Enter is pressed while focused, with the currently
/// selected row index.
public record TableProps<T>(
    String title,
    List<String> headers,
    List<T> rows,
    List<Function<T, String>> cellExtractors,
    List<Constraint> columnWidths,
    int selectedRow,
    IntConsumer onSelectChange,
    Optional<IntConsumer> onActivate,
    Optional<String> focusId,
    boolean autoFocus) {

  public TableProps {
    headers = List.copyOf(headers);
    rows = List.copyOf(rows);
    cellExtractors = List.copyOf(cellExtractors);
    columnWidths = List.copyOf(columnWidths);
    if (cellExtractors.size() != headers.size()) {
      throw new IllegalArgumentException(
          "headers ("
              + headers.size()
              + ") and cellExtractors ("
              + cellExtractors.size()
              + ") must have the same length");
    }
  }
}
