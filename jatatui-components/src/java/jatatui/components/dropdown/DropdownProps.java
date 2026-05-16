package jatatui.components.dropdown;

import jatatui.core.style.Style;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntConsumer;

/// Props for [Dropdown].
///
/// Generic over the option type `T`. Each option is rendered as a label string produced by
/// [#labelFn] — so callers can model options as records, enums, domain types, etc., without
/// stringly-typing the wire format. The String quick-paths on [jatatui.components.Components]
/// cover the common `List<String>` case.
///
/// Selection is **controlled** via [#selectedIndex] / [#onChange] (index-based, matching
/// [jatatui.components.list.ListProps] and [jatatui.components.table.TableProps]). The
/// open/closed state is internal.
public record DropdownProps<T>(
    String label,
    List<T> items,
    Function<T, String> labelFn,
    int selectedIndex,
    IntConsumer onChange,
    Optional<String> focusId,
    boolean autoFocus,
    Style style,
    Style focusedStyle) {

  public DropdownProps {
    items = List.copyOf(items);
  }

  /// Generic factory.
  public static <T> DropdownProps<T> of(
      String label,
      List<T> items,
      Function<T, String> labelFn,
      int selectedIndex,
      IntConsumer onChange) {
    return new DropdownProps<>(
        label,
        items,
        labelFn,
        selectedIndex,
        onChange,
        Optional.empty(),
        false,
        Style.empty(),
        Style.empty());
  }

  /// Quick-path for `List<String>` — labelFn = identity.
  public static DropdownProps<String> ofStrings(
      String label, List<String> items, int selectedIndex, IntConsumer onChange) {
    return of(label, items, Function.identity(), selectedIndex, onChange);
  }

  public DropdownProps<T> withFocusId(String focusId) {
    return new DropdownProps<>(
        label,
        items,
        labelFn,
        selectedIndex,
        onChange,
        Optional.of(focusId),
        autoFocus,
        style,
        focusedStyle);
  }

  public DropdownProps<T> withAutoFocus(boolean autoFocus) {
    return new DropdownProps<>(
        label, items, labelFn, selectedIndex, onChange, focusId, autoFocus, style, focusedStyle);
  }

  public DropdownProps<T> withStyle(Style style) {
    return new DropdownProps<>(
        label, items, labelFn, selectedIndex, onChange, focusId, autoFocus, style, focusedStyle);
  }

  public DropdownProps<T> withFocusedStyle(Style focusedStyle) {
    return new DropdownProps<>(
        label, items, labelFn, selectedIndex, onChange, focusId, autoFocus, style, focusedStyle);
  }
}
