package jatatui.components.list;

import jatatui.react.Element;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntConsumer;

/// Static factories for [ListComponent]. Mirrors the convention used by
/// [jatatui.react.Components] — import statically:
///
/// ```java
/// import static jatatui.components.list.ListComponents.*;
/// ```
public final class ListComponents {
  private ListComponents() {}

  /// Build a list from explicit props.
  public static <T> Element list(ListProps<T> props) {
    return ListComponent.of(props);
  }

  /// Convenience: title + string items + selected + onSelectChange. No activation callback,
  /// auto-focused.
  public static Element list(
      String title, List<String> items, int selected, IntConsumer onSelectChange) {
    return ListComponent.ofStrings(title, items, selected, onSelectChange);
  }

  /// Convenience: title + string items + selection + activation callback.
  public static Element list(
      String title,
      List<String> items,
      int selected,
      IntConsumer onSelectChange,
      Runnable onActivate) {
    return ListComponent.ofStrings(title, items, selected, onSelectChange, onActivate);
  }

  /// Typed convenience: arbitrary `T` items rendered via `labelFn`.
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

  /// Typed convenience with explicit focus id (for cycling focus across multiple lists or for
  /// imperative `FocusManager.focus(id)` calls).
  public static <T> Element list(
      String title,
      List<T> items,
      Function<T, String> labelFn,
      int selected,
      IntConsumer onSelectChange,
      Runnable onActivate,
      String focusId,
      boolean autoFocus) {
    return ListComponent.of(
        new ListProps<>(
            title,
            items,
            labelFn,
            selected,
            onSelectChange,
            Optional.of(onActivate),
            Optional.of(focusId),
            autoFocus));
  }
}
