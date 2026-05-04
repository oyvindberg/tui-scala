package jatatui.react;

/// A Component is a function from `(props, ctx)` to an [Element]. The thing you author.
///
/// User-defined components are typically `static final Component<MyProps>` constants:
///
/// ```java
/// record CounterProps(int initial) {}
///
/// static final Component<CounterProps> Counter = (props, ctx) -> {
///   var count = ctx.useState(() -> props.initial());
///   return box(" Counter ", Borders.ALL,
///     text("Count: " + count.get()),
///     button("[+]", style, () -> count.update(n -> n + 1)));
/// };
///
/// // Apply with props:
/// Element myCounter = apply(Counter, new CounterProps(0));
/// ```
///
/// Built-in factories (`text`, `box`, `column`, `row`, ...) are themselves Components
/// applied to props records — there is no special "intrinsic" status at the user level.
/// Hooks declared inside `apply` live in this Component's fiber slot.
@FunctionalInterface
public interface Component<P> {
  Element apply(P props, RenderContext ctx);
}
