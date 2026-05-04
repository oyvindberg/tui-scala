package jatatui.components;

import jatatui.components.gauge.GaugeComponent;
import jatatui.components.gauge.GaugeProps;
import jatatui.components.gauge.LineGaugeComponent;
import jatatui.components.gauge.LineGaugeProps;
import jatatui.components.list.ListComponent;
import jatatui.components.list.ListProps;
import jatatui.components.table.Table;
import jatatui.components.table.TableProps;
import jatatui.core.layout.Constraint;
import jatatui.core.style.Style;
import jatatui.react.Element;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntConsumer;

/// Static factories for the higher-level components in `jatatui-components`. Mirrors the role of
/// [jatatui.react.Components] but for components that wrap concrete widgets rather than the
/// react-layer primitives.
///
/// Import statically: `import static jatatui.components.Components.*;`
public final class Components {
  private Components() {}

  // ---------- Table ----------

  /// Typed-row factory. See [TableProps].
  public static <T> Element table(TableProps<T> props) {
    return Table.of(props);
  }

  /// Quick-path factory taking pre-stringified rows.
  public static Element table(
      String title,
      List<String> headers,
      List<List<String>> rows,
      List<Constraint> columnWidths,
      int selectedRow,
      IntConsumer onSelectChange,
      Optional<IntConsumer> onActivate,
      Optional<String> focusId,
      boolean autoFocus) {
    return Table.ofStrings(
        title, headers, rows, columnWidths, selectedRow, onSelectChange, onActivate, focusId, autoFocus);
  }

  // ---------- List ----------

  /// Typed-item factory. See [ListProps].
  public static <T> Element list(ListProps<T> props) {
    return ListComponent.of(props);
  }

  /// Quick-path factory taking string items, with no activation callback. Auto-focused.
  public static Element list(
      String title, List<String> items, int selected, IntConsumer onSelectChange) {
    return ListComponent.ofStrings(title, items, selected, onSelectChange);
  }

  /// Quick-path factory taking string items, with an activation callback. Auto-focused.
  public static Element list(
      String title,
      List<String> items,
      int selected,
      IntConsumer onSelectChange,
      Runnable onActivate) {
    return ListComponent.ofStrings(title, items, selected, onSelectChange, onActivate);
  }

  /// Typed convenience: arbitrary `T` items rendered via `labelFn`. Auto-focused.
  public static <T> Element list(
      String title,
      List<T> items,
      Function<T, String> labelFn,
      int selected,
      IntConsumer onSelectChange,
      Runnable onActivate) {
    return ListComponent.of(
        new ListProps<>(
            title,
            items,
            labelFn,
            selected,
            onSelectChange,
            Optional.of(onActivate),
            Optional.empty(),
            true));
  }

  // ---------- Gauge ----------

  /// Full-control factory for the block-style gauge. See [GaugeProps].
  public static Element gauge(GaugeProps props) {
    return GaugeComponent.of(props);
  }

  /// Quick-path: just a ratio in `[0, 1]`. No title, no custom label, default styles.
  public static Element gauge(double ratio) {
    return GaugeComponent.of(GaugeProps.of(ratio));
  }

  /// Quick-path: title + ratio + custom label + filled-bar style.
  public static Element gauge(String title, double ratio, String label, Style gaugeStyle) {
    return GaugeComponent.of(
        GaugeProps.of(ratio).withTitle(title).withLabel(label).withGaugeStyle(gaugeStyle));
  }

  /// Full-control factory for the single-line gauge. See [LineGaugeProps].
  public static Element lineGauge(LineGaugeProps props) {
    return LineGaugeComponent.of(props);
  }

  /// Quick-path: just a ratio in `[0, 1]`.
  public static Element lineGauge(double ratio) {
    return LineGaugeComponent.of(LineGaugeProps.of(ratio));
  }

  /// Quick-path: title + ratio + filled-bar style.
  public static Element lineGauge(String title, double ratio, Style filledStyle) {
    return LineGaugeComponent.of(
        LineGaugeProps.of(ratio).withTitle(title).withFilledStyle(filledStyle));
  }
}
