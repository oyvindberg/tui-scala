package jatatui.components.router;

import static jatatui.react.Components.component;
import static jatatui.react.Components.provide;

import jatatui.react.Element;
import jatatui.react.State;
import java.util.ArrayList;
import java.util.List;

/// Mount near the app root with a base [Screen]. Provides a [RouterApi] via Context. Renders the
/// current top-of-stack screen.
///
/// **Focus contract:** every stack transition (`push` / `pop` / `replace` / `reset`) clears
/// focus before the new top-of-stack renders, so the new screen's `useFocus(autoFocus=true)`
/// claims same-frame via the eager-claim path. Without this, `focused` would still point at the
/// outgoing screen's id, the new screen's first frame would paint as nothing-focused, and
/// commit's post-frame re-pick would only become visible on the second frame (a perceptible
/// one-frame flicker).
public final class Router {
  private Router() {}

  public static Element of(Screen base) {
    return component(
        ctx -> {
          State<List<Screen>> stackState = ctx.useState(() -> List.of(base));

          RouterApi api =
              new RouterApi() {
                @Override
                public Screen current() {
                  List<Screen> s = stackState.latest();
                  return s.get(s.size() - 1);
                }

                @Override
                public List<Screen> stack() {
                  return stackState.latest();
                }

                @Override
                public void push(Screen screen) {
                  ctx.blur();
                  stackState.update(
                      prev -> {
                        List<Screen> next = new ArrayList<>(prev);
                        next.add(screen);
                        return List.copyOf(next);
                      });
                }

                @Override
                public void pop() {
                  ctx.blur();
                  stackState.update(
                      prev -> {
                        if (prev.size() <= 1) return prev;
                        return List.copyOf(prev.subList(0, prev.size() - 1));
                      });
                }

                @Override
                public void replace(Screen screen) {
                  ctx.blur();
                  stackState.update(
                      prev -> {
                        List<Screen> next = new ArrayList<>(prev);
                        next.set(next.size() - 1, screen);
                        return List.copyOf(next);
                      });
                }

                @Override
                public void reset() {
                  ctx.blur();
                  stackState.set(List.of(base));
                }

                @Override
                public int depth() {
                  return stackState.latest().size();
                }
              };

          Screen top = api.current();
          return provide(RouterApi.CONTEXT, api, top.body());
        });
  }
}
