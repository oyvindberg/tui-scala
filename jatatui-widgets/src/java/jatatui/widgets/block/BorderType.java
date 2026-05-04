package jatatui.widgets.block;

import jatatui.core.symbols.Border;

/// The type of border of a [Block].
///
/// See [Block#withBorderType(BorderType)] to configure a block's border style.
public enum BorderType {
  /// A plain, simple border.
  ///
  /// ```text
  /// ┌───────┐
  /// │       │
  /// └───────┘
  /// ```
  Plain,
  /// A plain border with rounded corners.
  ///
  /// ```text
  /// ╭───────╮
  /// │       │
  /// ╰───────╯
  /// ```
  Rounded,
  /// A doubled border.
  ///
  /// Note: this uses one character that draws two lines.
  ///
  /// ```text
  /// ╔═══════╗
  /// ║       ║
  /// ╚═══════╝
  /// ```
  Double,
  /// A thick border.
  ///
  /// ```text
  /// ┏━━━━━━━┓
  /// ┃       ┃
  /// ┗━━━━━━━┛
  /// ```
  Thick,
  /// A light double-dashed border.
  LightDoubleDashed,
  /// A heavy double-dashed border.
  HeavyDoubleDashed,
  /// A light triple-dashed border.
  LightTripleDashed,
  /// A heavy triple-dashed border.
  HeavyTripleDashed,
  /// A light quadruple-dashed border.
  LightQuadrupleDashed,
  /// A heavy quadruple-dashed border.
  HeavyQuadrupleDashed,
  /// A border with a single line on the inside of a half block.
  ///
  /// ```text
  /// ▗▄▄▄▄▄▄▄▖
  /// ▐       ▌
  /// ▝▀▀▀▀▀▀▀▘
  /// ```
  QuadrantInside,
  /// A border with a single line on the outside of a half block.
  ///
  /// ```text
  /// ▛▀▀▀▀▀▀▀▜
  /// ▌       ▐
  /// ▙▄▄▄▄▄▄▄▟
  /// ```
  QuadrantOutside;

  /// Convert this `BorderType` into the corresponding [Border.Set] of border symbols.
  public Border.Set toBorderSet() {
    return borderSymbols(this);
  }

  /// Convert the given `BorderType` into the corresponding [Border.Set] of border symbols.
  public static Border.Set borderSymbols(BorderType borderType) {
    return switch (borderType) {
      case Plain -> Border.PLAIN;
      case Rounded -> Border.ROUNDED;
      case Double -> Border.DOUBLE;
      case Thick -> Border.THICK;
      case LightDoubleDashed -> Border.LIGHT_DOUBLE_DASHED;
      case HeavyDoubleDashed -> Border.HEAVY_DOUBLE_DASHED;
      case LightTripleDashed -> Border.LIGHT_TRIPLE_DASHED;
      case HeavyTripleDashed -> Border.HEAVY_TRIPLE_DASHED;
      case LightQuadrupleDashed -> Border.LIGHT_QUADRUPLE_DASHED;
      case HeavyQuadrupleDashed -> Border.HEAVY_QUADRUPLE_DASHED;
      case QuadrantInside -> Border.QUADRANT_INSIDE;
      case QuadrantOutside -> Border.QUADRANT_OUTSIDE;
    };
  }
}
