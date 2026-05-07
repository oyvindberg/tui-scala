package jatatui.components.toast;

import jatatui.react.Context;
import jatatui.react.RenderContext;

/// API exposed by the [ToastsProvider] for adding toasts. Read it via [#useToasts]; the default
/// (when no provider is in scope) is a no-op so client code doesn't crash if you forget to mount
/// the provider.
public interface ToastApi {

  /// Add a toast and return its id. Use the returned id with [#dismiss] for early dismissal.
  String add(Toast toast);

  /// Convenience: add an INFO toast with the default duration.
  default String info(String message) {
    return add(Toast.info(message));
  }

  /// Convenience: add a SUCCESS toast.
  default String success(String message) {
    return add(Toast.success(message));
  }

  /// Convenience: add a WARN toast.
  default String warn(String message) {
    return add(Toast.warn(message));
  }

  /// Convenience: add an ERROR toast.
  default String error(String message) {
    return add(Toast.error(message));
  }

  /// Dismiss a toast by id. No-op if the id isn't currently active.
  void dismiss(String id);

  /// Dismiss all currently active toasts.
  void dismissAll();

  /// Default no-op implementation used when no [ToastsProvider] is mounted.
  ToastApi NO_OP =
      new ToastApi() {
        @Override
        public String add(Toast toast) {
          return "";
        }

        @Override
        public void dismiss(String id) {}

        @Override
        public void dismissAll() {}
      };

  /// Context key for the toast API. The [ToastsProvider] supplies the value; consumers read it
  /// via `ctx.useContext(ToastApi.CONTEXT)` or [#useToasts].
  Context<ToastApi> CONTEXT = Context.create(NO_OP);

  /// Read the toast API from context. Equivalent to `ctx.useContext(ToastApi.CONTEXT)`.
  static ToastApi useToasts(RenderContext ctx) {
    return ctx.useContext(CONTEXT);
  }
}
