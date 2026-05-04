package jatatui.widgets.list;

/// This option allows the user to configure the "highlight symbol" column width spacing.
///
/// Mirrors `ratatui_widgets::table::HighlightSpacing` (v0.30). The upstream type is exported
/// from the `table` module and shared by both [List] and `Table`. Until both widgets are ported
/// in jatatui this lives under the [List] package; it can be moved if needed once the table
/// widget is ported.
public enum HighlightSpacing {
  /// Always add spacing for the selection symbol column.
  ///
  /// With this variant, the column for the selection symbol will always be allocated, and so the
  /// table will never change size, regardless of if a row is selected or not.
  Always,

  /// Only add spacing for the selection symbol column if a row is selected.
  ///
  /// With this variant, the column for the selection symbol will only be allocated if there is a
  /// selection, causing the table to shift if selected / unselected. This is the default for
  /// backwards compatibility.
  WhenSelected,

  /// Never add spacing for the selection symbol column, regardless of whether something is
  /// selected or not.
  ///
  /// This means that the highlight symbol will never be drawn.
  Never;

  /// Returns the default value ([#WhenSelected]).
  public static HighlightSpacing defaultValue() {
    return WhenSelected;
  }

  /// Determine if a selection column should be displayed.
  ///
  /// `hasSelection`: true if a row/item is selected.
  ///
  /// Returns true if a selection column should be displayed.
  public boolean shouldAdd(boolean hasSelection) {
    return switch (this) {
      case Always -> true;
      case WhenSelected -> hasSelection;
      case Never -> false;
    };
  }
}
