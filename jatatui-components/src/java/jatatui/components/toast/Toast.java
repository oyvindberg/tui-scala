package jatatui.components.toast;

/// One transient message shown by [ToastsProvider]. `id` is assigned by the provider when added;
/// [Toast#info]/[#success]/[#warn]/[#error] factories leave it blank.
public record Toast(String id, String message, long durationMs, ToastKind kind) {

  public static final long DEFAULT_DURATION_MS = 3_000L;

  public enum ToastKind {
    INFO,
    SUCCESS,
    WARN,
    ERROR
  }

  public static Toast info(String message) {
    return new Toast("", message, DEFAULT_DURATION_MS, ToastKind.INFO);
  }

  public static Toast success(String message) {
    return new Toast("", message, DEFAULT_DURATION_MS, ToastKind.SUCCESS);
  }

  public static Toast warn(String message) {
    return new Toast("", message, DEFAULT_DURATION_MS, ToastKind.WARN);
  }

  public static Toast error(String message) {
    return new Toast("", message, DEFAULT_DURATION_MS, ToastKind.ERROR);
  }

  public Toast withId(String id) {
    return new Toast(id, message, durationMs, kind);
  }

  public Toast withDurationMs(long durationMs) {
    return new Toast(id, message, durationMs, kind);
  }
}
