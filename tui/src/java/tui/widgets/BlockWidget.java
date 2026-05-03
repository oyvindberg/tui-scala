package tui.widgets;

import java.util.Optional;
import tui.Alignment;
import tui.Borders;
import tui.Buffer;
import tui.Rect;
import tui.Spans;
import tui.Style;
import tui.Symbols;
import tui.Widget;
import tui.internal.Ranges;
import tui.internal.Saturating;

/// Base widget to be used with all upper level ones.
public final class BlockWidget implements Widget {
  public final Optional<Spans> title;
  public final Alignment titleAlignment;
  public final Borders borders;
  public final Style borderStyle;
  public final BorderType borderType;
  public final Style style;

  public BlockWidget(
      Optional<Spans> title,
      Alignment titleAlignment,
      Borders borders,
      Style borderStyle,
      BorderType borderType,
      Style style) {
    this.title = title;
    this.titleAlignment = titleAlignment;
    this.borders = borders;
    this.borderStyle = borderStyle;
    this.borderType = borderType;
    this.style = style;
  }

  public static BlockWidget empty() {
    return new BlockWidget(
        Optional.empty(),
        Alignment.Left,
        Borders.NONE,
        Style.empty(),
        BorderType.Plain,
        Style.empty());
  }

  /// Compute the inner area of a block based on its border visibility rules.
  public Rect inner(Rect area) {
    Rect inner = area;
    if (borders.intersects(Borders.LEFT)) {
      inner =
          new Rect(
              Math.min(Saturating.saturatingAdd(inner.x(), 1), inner.right()),
              inner.y(),
              Saturating.saturatingSubUnsigned(inner.width(), 1),
              inner.height());
    }
    if (borders.intersects(Borders.TOP) || title.isPresent()) {
      inner =
          new Rect(
              inner.x(),
              Math.min(Saturating.saturatingAdd(inner.y(), 1), inner.bottom()),
              inner.width(),
              Saturating.saturatingSubUnsigned(inner.height(), 1));
    }
    if (borders.intersects(Borders.RIGHT)) {
      inner =
          new Rect(
              inner.x(),
              inner.y(),
              Saturating.saturatingSubUnsigned(inner.width(), 1),
              inner.height());
    }
    if (borders.intersects(Borders.BOTTOM)) {
      inner =
          new Rect(
              inner.x(),
              inner.y(),
              inner.width(),
              Saturating.saturatingSubUnsigned(inner.height(), 1));
    }
    return inner;
  }

  @Override
  public void render(Rect area, Buffer buf) {
    if (area.area() == 0) {
      return;
    }
    buf.setStyle(area, style);
    Symbols.line.Set symbols = BorderType.lineSymbols(borderType);

    // Sides
    if (borders.intersects(Borders.LEFT)) {
      Ranges.range(
          area.top(),
          area.bottom(),
          y -> buf.get(area.left(), y).setSymbol(symbols.vertical()).setStyle(borderStyle));
    }

    if (borders.intersects(Borders.TOP)) {
      Ranges.range(
          area.left(),
          area.right(),
          x -> buf.get(x, area.top()).setSymbol(symbols.horizontal()).setStyle(borderStyle));
    }
    if (borders.intersects(Borders.RIGHT)) {
      int rx = area.right() - 1;
      Ranges.range(
          area.top(),
          area.bottom(),
          y -> buf.get(rx, y).setSymbol(symbols.vertical()).setStyle(borderStyle));
    }
    if (borders.intersects(Borders.BOTTOM)) {
      int by = area.bottom() - 1;
      Ranges.range(
          area.left(),
          area.right(),
          x -> buf.get(x, by).setSymbol(symbols.horizontal()).setStyle(borderStyle));
    }

    // Corners
    if (borders.contains(Borders.RIGHT.or(Borders.BOTTOM))) {
      buf.get(area.right() - 1, area.bottom() - 1)
          .setSymbol(symbols.bottomRight())
          .setStyle(borderStyle);
    }
    if (borders.contains(Borders.RIGHT.or(Borders.TOP))) {
      buf.get(area.right() - 1, area.top()).setSymbol(symbols.topRight()).setStyle(borderStyle);
    }
    if (borders.contains(Borders.LEFT.or(Borders.BOTTOM))) {
      buf.get(area.left(), area.bottom() - 1)
          .setSymbol(symbols.bottomLeft())
          .setStyle(borderStyle);
    }
    if (borders.contains(Borders.LEFT.or(Borders.TOP))) {
      buf.get(area.left(), area.top()).setSymbol(symbols.topLeft()).setStyle(borderStyle);
    }

    // Title
    if (title.isPresent()) {
      Spans titleVal = title.get();
      int leftBorderDx = borders.intersects(Borders.LEFT) ? 1 : 0;
      int rightBorderDx = borders.intersects(Borders.RIGHT) ? 1 : 0;

      int titleAreaWidth =
          Saturating.saturatingSubUnsigned(
              Saturating.saturatingSubUnsigned(area.width(), leftBorderDx), rightBorderDx);

      int titleDx = switch (titleAlignment) {
        case Left -> leftBorderDx;
        case Center -> Saturating.saturatingSubUnsigned(area.width(), titleVal.width()) / 2;
        case Right -> Saturating.saturatingSubUnsigned(
            Saturating.saturatingSubUnsigned(area.width(), titleVal.width()), rightBorderDx);
      };

      int titleX = area.left() + titleDx;
      int titleY = area.top();

      buf.setSpans(titleX, titleY, titleVal, titleAreaWidth);
    }
  }

  public BlockWidget withTitle(Spans t) {
    return new BlockWidget(Optional.of(t), titleAlignment, borders, borderStyle, borderType, style);
  }

  public BlockWidget withBorders(Borders b) {
    return new BlockWidget(title, titleAlignment, b, borderStyle, borderType, style);
  }

  public BlockWidget withBorderStyle(Style s) {
    return new BlockWidget(title, titleAlignment, borders, s, borderType, style);
  }

  public BlockWidget withBorderType(BorderType t) {
    return new BlockWidget(title, titleAlignment, borders, borderStyle, t, style);
  }

  public BlockWidget withStyle(Style s) {
    return new BlockWidget(title, titleAlignment, borders, borderStyle, borderType, s);
  }

  public BlockWidget withTitleAlignment(Alignment a) {
    return new BlockWidget(title, a, borders, borderStyle, borderType, style);
  }

  public enum BorderType {
    Plain,
    Rounded,
    Double,
    Thick;

    public static Symbols.line.Set lineSymbols(BorderType borderType) {
      return switch (borderType) {
        case Plain -> Symbols.line.NORMAL;
        case Rounded -> Symbols.line.ROUNDED;
        case Double -> Symbols.line.DOUBLE;
        case Thick -> Symbols.line.THICK;
      };
    }
  }
}
