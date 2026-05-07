package jatatui.components.toast;

import static jatatui.react.Components.*;

import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.style.Style;
import jatatui.react.Element;
import jatatui.react.State;
import jatatui.widgets.Borders;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/// Wrap your app with [ToastsProvider] near the root. It:
///   - owns the active toast list (via `useState`)
///   - exposes a [ToastApi] via `Context` so children can `useToasts(ctx).info("...")`
///   - renders the active toasts in the bottom-right corner via [Components#portal]
///   - schedules a re-render at the next toast expiry (via a single-thread scheduler), so toasts
///     auto-dismiss without requiring user input
///
/// The child Element is rendered as the main content; toasts overlay it.
public final class ToastsProvider {
  private ToastsProvider() {}

  public static Element of(Element child) {
    return component(
        ctx -> {
          State<List<ActiveToast>> toastsState = ctx.useState(ArrayList::new);
          var idGen = ctx.useRef(() -> new AtomicLong(0));
          var scheduler = ctx.useRef(ToastsProvider::makeScheduler);
          var requestRerender = ctx.useRef(ctx::requestRerender);

          // Sweep expired toasts on each render.
          long now = System.currentTimeMillis();
          List<ActiveToast> active = new ArrayList<>();
          for (ActiveToast t : toastsState.get()) {
            if (t.expiresAt > now) active.add(t);
          }
          if (active.size() != toastsState.get().size()) {
            // Set new value; this triggers another render.
            toastsState.set(List.copyOf(active));
          }

          // Schedule a re-render at the next expiry, so the loop wakes up to drop expired toasts
          // even without user input.
          if (!active.isEmpty()) {
            long minExpiry = active.stream().mapToLong(t -> t.expiresAt).min().getAsLong();
            long delay = Math.max(1, minExpiry - now);
            Runnable rr = requestRerender.get();
            scheduler.get().schedule(rr, delay, TimeUnit.MILLISECONDS);
          }

          ToastApi api =
              new ToastApi() {
                @Override
                public String add(Toast toast) {
                  String id = "t" + idGen.get().incrementAndGet();
                  long expiresAt = System.currentTimeMillis() + toast.durationMs();
                  ActiveToast at = new ActiveToast(toast.withId(id), expiresAt);
                  toastsState.update(
                      prev -> {
                        List<ActiveToast> next = new ArrayList<>(prev);
                        next.add(at);
                        return List.copyOf(next);
                      });
                  return id;
                }

                @Override
                public void dismiss(String id) {
                  toastsState.update(
                      prev -> {
                        List<ActiveToast> next = new ArrayList<>(prev);
                        next.removeIf(t -> t.toast.id().equals(id));
                        return List.copyOf(next);
                      });
                }

                @Override
                public void dismissAll() {
                  toastsState.set(List.of());
                }
              };

          // Compose: provide the API to children, and overlay the toast portal on top.
          Rect screen = ctx.frame().area();
          Rect overlay = bottomRightStrip(screen, 36, Math.min(active.size(), 6));

          return column(
              provide(ToastApi.CONTEXT, api, child),
              when(!active.isEmpty(), portal(toastsLayer(active), overlay)));
        });
  }

  private static Element toastsLayer(List<ActiveToast> toasts) {
    return component(
        c -> {
          // Eat clicks/keys on the toast surface so they don't bleed into background.
          c.onClick(e -> e.stopPropagation());
          List<Element> rows = new ArrayList<>(toasts.size());
          for (ActiveToast t : toasts) {
            rows.add(
                length(
                    3,
                    box(
                        " " + iconFor(t.toast.kind()) + " ",
                        Borders.ALL,
                        text(t.toast.message(), styleFor(t.toast.kind())))));
          }
          return column(rows.toArray(new Element[0])).with(p -> p.withSpacing(0));
        });
  }

  private static String iconFor(Toast.ToastKind kind) {
    return switch (kind) {
      case INFO -> "info";
      case SUCCESS -> "ok";
      case WARN -> "warn";
      case ERROR -> "err";
    };
  }

  private static Style styleFor(Toast.ToastKind kind) {
    return switch (kind) {
      case INFO -> Style.empty().withFg(new Color.Cyan());
      case SUCCESS -> Style.empty().withFg(new Color.Green());
      case WARN -> Style.empty().withFg(new Color.Yellow());
      case ERROR -> Style.empty().withFg(new Color.Red());
    };
  }

  private static Rect bottomRightStrip(Rect screen, int width, int rowsCount) {
    int w = Math.min(width, screen.width());
    int h = Math.min(rowsCount * 3, screen.height());
    int x = screen.x() + screen.width() - w;
    int y = screen.y() + screen.height() - h;
    return new Rect(x, y, w, h);
  }

  private static ScheduledExecutorService makeScheduler() {
    return Executors.newSingleThreadScheduledExecutor(
        r -> {
          Thread t = new Thread(r, "jatatui-toasts");
          t.setDaemon(true);
          return t;
        });
  }

  /// One toast plus its absolute expiry timestamp. Internal — public only because [java.util.List]
  /// of records work better with `useState` typing.
  public record ActiveToast(Toast toast, long expiresAt) {}
}
