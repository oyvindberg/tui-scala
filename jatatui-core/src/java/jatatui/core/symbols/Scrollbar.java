package jatatui.core.symbols;

/// Scrollbar symbol sets.
///
/// ```text
/// <--▮------->
/// ^  ^   ^   ^
/// │  │   │   └ end
/// │  │   └──── track
/// │  └──────── thumb
/// └─────────── begin
/// ```
public final class Scrollbar {

  private Scrollbar() {}

  public record Set(String track, String thumb, String begin, String end) {}

  public static final Set DOUBLE_VERTICAL = new Set(Line.DOUBLE_VERTICAL, Block.FULL, "▲", "▼");

  public static final Set DOUBLE_HORIZONTAL = new Set(Line.DOUBLE_HORIZONTAL, Block.FULL, "◄", "►");

  public static final Set VERTICAL = new Set(Line.VERTICAL, Block.FULL, "↑", "↓");

  public static final Set HORIZONTAL = new Set(Line.HORIZONTAL, Block.FULL, "←", "→");
}
