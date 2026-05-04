package jatatui.react;

import jatatui.core.layout.Rect;
import java.util.function.Function;

/// "Pure" component: takes immutable props (typically a Java record), memoizes its output by
/// `props.equals(...)`. The body is skipped while equal props are passed across renders.
///
/// Records get value-based equality for free, so any record-shaped props gives you free
/// React.memo semantics:
///
/// ```java
/// record TableProps(java.util.List<Row> rows, String filter, int selected) {}
///
/// Element table(TableProps p) {
///   return new PureComponent<>(p, props -> column(/* ... */));
/// }
/// ```
public record PureComponent<P>(P props, Function<P, Element> body) implements Element {
  @Override
  public void render(RenderContext ctx, Rect area) {
    Element produced = ctx.hooks.memoOrCompute(ctx.fiber, new Object[] {props}, () -> body.apply(props));
    ctx.renderChild(0, produced, area);
  }
}
