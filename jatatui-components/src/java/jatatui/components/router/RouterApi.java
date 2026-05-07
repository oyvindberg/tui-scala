package jatatui.components.router;

import jatatui.react.Context;
import jatatui.react.RenderContext;
import java.util.List;

/// Stack-based navigation API. Held in Context by [Router]; read via [#useRouter] in descendants.
public interface RouterApi {

  /// Current top-of-stack screen.
  Screen current();

  /// All screens, base first.
  List<Screen> stack();

  /// Push a new screen on top.
  void push(Screen screen);

  /// Convenience: push by label + body.
  default void push(String label, jatatui.react.Element body) {
    push(Screen.of(label, body));
  }

  /// Pop the current screen and return to the previous one. No-op when at the base screen.
  void pop();

  /// Replace the current screen with `screen`. Stack depth unchanged.
  void replace(Screen screen);

  default void replace(String label, jatatui.react.Element body) {
    replace(Screen.of(label, body));
  }

  /// Reset the stack to just the base screen (the screen passed at mount time).
  void reset();

  /// Stack depth (>= 1).
  int depth();

  /// Default no-op when no provider is mounted.
  RouterApi NO_OP =
      new RouterApi() {
        @Override
        public Screen current() {
          return Screen.of("", jatatui.react.Components.empty());
        }

        @Override
        public List<Screen> stack() {
          return List.of();
        }

        @Override
        public void push(Screen screen) {}

        @Override
        public void pop() {}

        @Override
        public void replace(Screen screen) {}

        @Override
        public void reset() {}

        @Override
        public int depth() {
          return 0;
        }
      };

  Context<RouterApi> CONTEXT = Context.create(NO_OP);

  /// Read the router API from context.
  static RouterApi useRouter(RenderContext ctx) {
    return ctx.useContext(CONTEXT);
  }
}
