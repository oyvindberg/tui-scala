package jatatui.react;

import jatatui.core.layout.Constraint;
import jatatui.core.layout.Flex;
import jatatui.core.layout.Layout;
import jatatui.core.layout.Margin;
import jatatui.core.layout.Rect;
import jatatui.core.layout.Spacing;
import jatatui.core.style.Color;
import jatatui.core.style.Style;
import jatatui.core.text.Line;
import jatatui.widgets.Borders;
import jatatui.widgets.block.Block;
import jatatui.widgets.paragraph.Paragraph;
import jatatui.widgets.paragraph.Wrap;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/// React-style element — the data your components return.
///
/// Mirrors React's `ReactNode`: a sealed type with built-in records for layout primitives, plus
/// [Component] (function component), [Memo] (deps-keyed memoization), and [PureComponent]
/// (record-props memoization) cases for skipping re-renders.
///
/// Every element is `render`-able into a [Rect]. Rendering walks the tree depth-first; parent
/// elements assign child fiber identity via [RenderContext#renderChild] so hooks declared inside
/// each child stay isolated across siblings.
public sealed interface Element
    permits Component,
        Memo,
        PureComponent,
        Element.Empty,
        Element.Box,
        Element.Text,
        Element.Paragraph_,
        Element.Button,
        Element.Column,
        Element.Row,
        Element.Sized,
        Element.Tabs,
        Element.ForEach,
        Element.When,
        Element.IfElse,
        Element.WidgetWrap {

  void render(RenderContext ctx, Rect area);

  // ---------- Built-in elements ----------

  /// Renders nothing.
  record Empty() implements Element {
    @Override
    public void render(RenderContext ctx, Rect area) {}
  }

  /// Single-line text.
  record Text(String content, Style style) implements Element {
    @Override
    public void render(RenderContext ctx, Rect area) {
      Paragraph.of(content).withStyle(style).render(area, ctx.buffer());
    }
  }

  /// Multi-line wrapped paragraph.
  record Paragraph_(String content, Style style, boolean wrap) implements Element {
    @Override
    public void render(RenderContext ctx, Rect area) {
      var p = Paragraph.of(content).withStyle(style);
      if (wrap) p = p.withWrap(new Wrap(true));
      p.render(area, ctx.buffer());
    }
  }

  /// Clickable text. Registers an `onClick` handler over its area.
  record Button(String label, Style style, Runnable onClick) implements Element {
    @Override
    public void render(RenderContext ctx, Rect area) {
      Paragraph.of(label).withStyle(style).render(area, ctx.buffer());
      ctx.onClick(area, onClick);
    }
  }

  /// Bordered container with optional title, holding a vertical stack of children.
  record Box(String title, Borders borders, Flex flex, Spacing spacing, List<Element> children)
      implements Element {
    public Box withFlex(Flex f) {
      return new Box(title, borders, f, spacing, children);
    }

    public Box withSpacing(int cells) {
      return new Box(title, borders, flex, Spacing.fromSigned(cells), children);
    }

    @Override
    public void render(RenderContext ctx, Rect area) {
      Block block = Block.empty().withTitle(Line.from(title)).withBorders(borders);
      block.render(area, ctx.buffer());
      Rect inner = block.inner(area);
      Column.renderChildrenVertical(ctx, inner, children, flex, spacing);
    }
  }

  /// Vertical stack. Per-child [Constraint] comes from [Sized] wrappers; bare children default to
  /// [Constraint.Fill]`(1)`. Container-level Flex / Spacing / Margin map to the existing
  /// [Layout] solver.
  record Column(List<Element> children, Flex flex, Spacing spacing, Margin margin)
      implements Element {
    public Column withFlex(Flex f) {
      return new Column(children, f, spacing, margin);
    }

    public Column withSpacing(int cells) {
      return new Column(children, flex, Spacing.fromSigned(cells), margin);
    }

    public Column withMargin(Margin m) {
      return new Column(children, flex, spacing, m);
    }

    @Override
    public void render(RenderContext ctx, Rect area) {
      Rect inner = applyMargin(area, margin);
      renderChildrenVertical(ctx, inner, children, flex, spacing);
    }

    static void renderChildrenVertical(
        RenderContext ctx, Rect area, List<Element> kids, Flex flex, Spacing spacing) {
      if (kids.isEmpty()) return;
      Constraint[] cs = constraintsOf(kids);
      Layout layout = Layout.vertical(cs).withFlex(flex).withSpacing(spacing);
      Rect[] split = area.layout(layout, kids.size());
      for (int i = 0; i < kids.size(); i++) {
        ctx.renderChild(i, kids.get(i), split[i]);
      }
    }
  }

  /// Horizontal stack. Mirror of [Column].
  record Row(List<Element> children, Flex flex, Spacing spacing, Margin margin)
      implements Element {
    public Row withFlex(Flex f) {
      return new Row(children, f, spacing, margin);
    }

    public Row withSpacing(int cells) {
      return new Row(children, flex, Spacing.fromSigned(cells), margin);
    }

    public Row withMargin(Margin m) {
      return new Row(children, flex, spacing, m);
    }

    @Override
    public void render(RenderContext ctx, Rect area) {
      Rect inner = applyMargin(area, margin);
      if (children.isEmpty()) return;
      Constraint[] cs = constraintsOf(children);
      Layout layout = Layout.horizontal(cs).withFlex(flex).withSpacing(spacing);
      Rect[] split = inner.layout(layout, children.size());
      for (int i = 0; i < children.size(); i++) {
        ctx.renderChild(i, children.get(i), split[i]);
      }
    }
  }

  /// Attaches a [Constraint] to any Element. Containers ([Column], [Row], [Box]) read this when
  /// computing their child constraints; bare children default to [Constraint.Fill]`(1)`.
  record Sized(Constraint constraint, Element child) implements Element {
    @Override
    public void render(RenderContext ctx, Rect area) {
      child.render(ctx, area);
    }
  }

  /// Typed-children example: a tab strip + the selected tab's body.
  record Tabs(int selected, List<Tab> tabs) implements Element {
    public record Tab(String label, Element body) {}

    @Override
    public void render(RenderContext ctx, Rect area) {
      Rect[] split =
          area.layout(Layout.vertical(new Constraint.Length(1), new Constraint.Fill(1)), 2);
      StringBuilder header = new StringBuilder();
      for (int i = 0; i < tabs.size(); i++) {
        if (i > 0) header.append(" │ ");
        header.append(i == selected ? "[" + tabs.get(i).label + "]" : tabs.get(i).label);
      }
      Paragraph.of(header.toString())
          .withStyle(Style.empty().withFg(Color.GRAY))
          .render(split[0], ctx.buffer());
      if (selected >= 0 && selected < tabs.size()) {
        ctx.renderChild(selected, tabs.get(selected).body, split[1]);
      }
    }
  }

  /// `forEach` over a list. `keyFn` is required to keep state stable across reorders
  /// (React's `key=`). Each item gets `rowHeight` rows.
  record ForEach<T>(
      List<T> items, Function<T, String> keyFn, Function<T, Element> render, int rowHeight)
      implements Element {
    @Override
    public void render(RenderContext ctx, Rect area) {
      if (items.isEmpty()) return;
      Constraint[] cs = new Constraint[items.size()];
      for (int i = 0; i < cs.length; i++) cs[i] = new Constraint.Length(rowHeight);
      Rect[] split = area.layout(Layout.vertical(cs), items.size());
      for (int i = 0; i < items.size(); i++) {
        T item = items.get(i);
        ctx.renderChild(keyFn.apply(item), render.apply(item), split[i]);
      }
    }
  }

  record When(boolean condition, Element then_) implements Element {
    @Override
    public void render(RenderContext ctx, Rect area) {
      if (condition) then_.render(ctx, area);
    }
  }

  record IfElse(boolean condition, Element then_, Element else_) implements Element {
    @Override
    public void render(RenderContext ctx, Rect area) {
      (condition ? then_ : else_).render(ctx, area);
    }
  }

  /// Escape hatch — drop any [jatatui.core.widgets.Widget] into the tree.
  record WidgetWrap(jatatui.core.widgets.Widget widget) implements Element {
    @Override
    public void render(RenderContext ctx, Rect area) {
      widget.render(area, ctx.buffer());
    }
  }

  // ---------- Helpers used by container records ----------

  static Constraint[] constraintsOf(List<Element> kids) {
    Constraint[] cs = new Constraint[kids.size()];
    for (int i = 0; i < kids.size(); i++) {
      cs[i] = (kids.get(i) instanceof Sized s) ? s.constraint() : new Constraint.Fill(1);
    }
    return cs;
  }

  static Rect applyMargin(Rect area, Margin m) {
    if (m.horizontal() == 0 && m.vertical() == 0) return area;
    int x = area.x() + m.horizontal();
    int y = area.y() + m.vertical();
    int w = Math.max(0, area.width() - 2 * m.horizontal());
    int h = Math.max(0, area.height() - 2 * m.vertical());
    return new Rect(x, y, w, h);
  }

  // ---------- Suppress for forward-ref compilation ----------
  // (Memo / PureComponent / Component live in their own files but are part of the sealed permits)
  @SuppressWarnings("unused")
  Supplier<Object> __unused = () -> null;
}
