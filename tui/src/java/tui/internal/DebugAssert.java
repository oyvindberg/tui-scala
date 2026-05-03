package tui.internal;

public final class DebugAssert {
  private DebugAssert() {}

  public static void apply(boolean pred, String msg, Object... details) {
    if (!pred) {
      String formatted;
      if (details.length == 0) {
        formatted = "";
      } else {
        StringBuilder sb = new StringBuilder(" (");
        for (int i = 0; i < details.length; i++) {
          if (i > 0) sb.append(", ");
          sb.append(details[i]);
        }
        sb.append(")");
        formatted = sb.toString();
      }
      throw new RuntimeException("assertion failed: " + msg + formatted);
    }
  }
}
