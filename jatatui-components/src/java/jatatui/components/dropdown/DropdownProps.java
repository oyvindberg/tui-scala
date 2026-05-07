package jatatui.components.dropdown;

import jatatui.core.style.Style;
import java.util.List;
import java.util.Optional;
import java.util.function.IntConsumer;

/// Props for [Dropdown]. The component is **controlled** by `selectedIndex` / `onChange`; the
/// open/closed state is internal.
public record DropdownProps(
    String label,
    List<String> items,
    int selectedIndex,
    IntConsumer onChange,
    Optional<String> focusId,
    boolean autoFocus,
    Style style,
    Style focusedStyle) {

  public static DropdownProps of(String label, List<String> items, int selectedIndex, IntConsumer onChange) {
    return new DropdownProps(
        label, items, selectedIndex, onChange, Optional.empty(), false, Style.empty(), Style.empty());
  }

  public DropdownProps withFocusId(String focusId) {
    return new DropdownProps(label, items, selectedIndex, onChange, Optional.of(focusId), autoFocus, style, focusedStyle);
  }

  public DropdownProps withAutoFocus(boolean autoFocus) {
    return new DropdownProps(label, items, selectedIndex, onChange, focusId, autoFocus, style, focusedStyle);
  }

  public DropdownProps withStyle(Style style) {
    return new DropdownProps(label, items, selectedIndex, onChange, focusId, autoFocus, style, focusedStyle);
  }

  public DropdownProps withFocusedStyle(Style focusedStyle) {
    return new DropdownProps(label, items, selectedIndex, onChange, focusId, autoFocus, style, focusedStyle);
  }
}
