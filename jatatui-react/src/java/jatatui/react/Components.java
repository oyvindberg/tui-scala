package jatatui.react;

import jatatui.core.layout.Constraint;
import jatatui.core.layout.Flex;
import jatatui.core.layout.Margin;
import jatatui.core.layout.Spacing;
import jatatui.core.style.Style;
import jatatui.widgets.Borders;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/// Static factories for [Element]s. Mirrors React's "create elements via type constructors"
/// (`React.createElement` / JSX) — Java has no JSX so this is the conventional alternative.
///
/// Import statically: `import static jatatui.react.Components.*;`
public final class Components {
  private Components() {}

  // ---- Function components ----

  /// User-defined function component. Body runs every render with hooks available.
  public static Element component(Function<RenderContext, Element> body) {
    return new Component(body);
  }

  /// `React.memo` — caches `body`'s output keyed by reference-equal `deps`.
  public static Element memo(Object[] deps, Supplier<Element> body) {
    return new Memo(deps, body);
  }

  /// Convenience for the common "memoize on these values" call site.
  public static Object[] deps(Object... values) {
    return values;
  }

  /// "Pure" component over an immutable props record. Memoized by `props.equals(...)`.
  public static <P> Element pureComponent(P props, Function<P, Element> body) {
    return new PureComponent<>(props, body);
  }

  // ---- Leaves ----

  public static Element empty() {
    return new Element.Empty();
  }

  public static Element text(String content) {
    return new Element.Text(content, Style.empty());
  }

  public static Element text(String content, Style style) {
    return new Element.Text(content, style);
  }

  public static Element paragraph(String content, Style style) {
    return new Element.Paragraph_(content, style, true);
  }

  public static Element button(String label, Style style, Runnable onClick) {
    return new Element.Button(label, style, onClick);
  }

  // ---- Containers ----

  /// Returns the concrete record so callers can chain `.withFlex(...)` / `.withSpacing(n)`.
  /// Records still implement [Element] so they assign cleanly to `Element` variables.
  public static Element.Box box(String title, Borders borders, Element... children) {
    return new Element.Box(title, borders, Flex.Legacy, Spacing.DEFAULT, List.of(children));
  }

  public static Element.Column column(Element... children) {
    return new Element.Column(List.of(children), Flex.Legacy, Spacing.DEFAULT, new Margin(0, 0));
  }

  public static Element.Row row(Element... children) {
    return new Element.Row(List.of(children), Flex.Legacy, Spacing.DEFAULT, new Margin(0, 0));
  }

  public static Element tabs(int selected, Element.Tabs.Tab... tabs) {
    return new Element.Tabs(selected, List.of(tabs));
  }

  public static Element.Tabs.Tab tab(String label, Element body) {
    return new Element.Tabs.Tab(label, body);
  }

  // ---- Per-child sizing ----

  /// Wrap an element with an explicit [Constraint]. Containers read these to feed the [Layout]
  /// solver; bare children default to `Constraint.Fill(1)`.
  public static Element sized(Constraint c, Element child) {
    return new Element.Sized(c, child);
  }

  public static Element length(int n, Element child) {
    return new Element.Sized(new Constraint.Length(n), child);
  }

  public static Element fill(int weight, Element child) {
    return new Element.Sized(new Constraint.Fill(weight), child);
  }

  public static Element min(int n, Element child) {
    return new Element.Sized(new Constraint.Min(n), child);
  }

  public static Element max(int n, Element child) {
    return new Element.Sized(new Constraint.Max(n), child);
  }

  public static Element percent(int p, Element child) {
    return new Element.Sized(new Constraint.Percentage(p), child);
  }

  public static Element ratio(int num, int den, Element child) {
    return new Element.Sized(new Constraint.Ratio(num, den), child);
  }

  // ---- Conditional / list ----

  public static Element when(boolean condition, Element then_) {
    return new Element.When(condition, then_);
  }

  public static Element ifElse(boolean condition, Element then_, Element else_) {
    return new Element.IfElse(condition, then_, else_);
  }

  public static <T> Element forEach(
      List<T> items, Function<T, String> keyFn, Function<T, Element> render) {
    return new Element.ForEach<>(items, keyFn, render, 1);
  }

  public static <T> Element forEach(
      List<T> items, Function<T, String> keyFn, Function<T, Element> render, int rowHeight) {
    return new Element.ForEach<>(items, keyFn, render, rowHeight);
  }

  // ---- Escape hatch ----

  public static Element widget(jatatui.core.widgets.Widget widget) {
    return new Element.WidgetWrap(widget);
  }
}
