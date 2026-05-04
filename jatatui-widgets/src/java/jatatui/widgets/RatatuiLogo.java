package jatatui.widgets;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Rect;
import jatatui.core.style.Style;
import jatatui.core.widgets.Widget;

/// A widget that renders the Ratatui logo.
///
/// Mirrors `ratatui_widgets::logo::RatatuiLogo` (v0.30).
///
/// The Ratatui logo takes up two lines of text and comes in two sizes: [Size#Tiny] and
/// [Size#Small].
public final class RatatuiLogo implements Widget {

  /// The size of the logo.
  public enum Size {
    /// The tiny logo (2x15 characters).
    Tiny,
    /// A slightly larger version of the logo (2x27 characters).
    Small;

    /// Returns the ASCII-art string for this size (two lines, terminated by `\n`).
    public String asString() {
      return switch (this) {
        case Tiny -> TINY_LOGO;
        case Small -> SMALL_LOGO;
      };
    }
  }

  private static final String TINY_LOGO = "▛▚▗▀▖▜▘▞▚▝▛▐ ▌▌\n" + "▛▚▐▀▌▐ ▛▜ ▌▝▄▘▌\n";

  private static final String SMALL_LOGO =
      "█▀▀▄ ▄▀▀▄▝▜▛▘▄▀▀▄▝▜▛▘█  █ █\n" + "█▀▀▄ █▀▀█ ▐▌ █▀▀█ ▐▌ ▀▄▄▀ █\n";

  /// The size of this logo.
  public final Size size;

  private RatatuiLogo(Size size) {
    this.size = size;
  }

  /// Creates a new Ratatui logo widget with the given [Size].
  public static RatatuiLogo of(Size size) {
    return new RatatuiLogo(size);
  }

  /// Returns a copy with the [Size] replaced.
  public RatatuiLogo withSize(Size size) {
    return new RatatuiLogo(size);
  }

  /// Creates a new Ratatui logo widget with the [Size#Tiny] size.
  public static RatatuiLogo tiny() {
    return new RatatuiLogo(Size.Tiny);
  }

  /// Creates a new Ratatui logo widget with the [Size#Small] size.
  public static RatatuiLogo small() {
    return new RatatuiLogo(Size.Small);
  }

  @Override
  public void render(Rect area, Buffer buf) {
    Rect clipped = area.intersection(buf.area());
    if (clipped.isEmpty()) {
      return;
    }
    String[] lines = size.asString().split("\n", -1);
    int y = clipped.top();
    for (int i = 0; i < lines.length && y < clipped.bottom(); i++) {
      String line = lines[i];
      if (line.isEmpty()) continue;
      buf.setStringn(clipped.left(), y, line, clipped.width(), Style.empty());
      y++;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof RatatuiLogo other)) return false;
    return size == other.size;
  }

  @Override
  public int hashCode() {
    return size.hashCode();
  }

  @Override
  public String toString() {
    return "RatatuiLogo[size=" + size + "]";
  }
}
