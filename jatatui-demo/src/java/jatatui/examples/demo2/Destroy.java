package jatatui.examples.demo2;

import jatatui.core.buffer.Buffer;
import jatatui.core.buffer.Cell;
import jatatui.core.layout.Constraint;
import jatatui.core.layout.Flex;
import jatatui.core.layout.Layout;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.style.Style;
import jatatui.core.terminal.Frame;
import jatatui.core.text.Text;
import java.util.Random;

/// Mirrors `apps/demo2/src/destroy.rs`.
///
/// Destroy mode renders a "drip" animation that scatters pixels downward and overlays a fading
/// "RATATUI" logo. Activated by pressing `d` in the demo.
///
/// ## Random number source
///
/// Upstream uses `rand_chacha::ChaCha8Rng::seed_from_u64(10)` so that the same pixels move every
/// frame. Java has no out-of-the-box ChaCha8, but we only need a deterministic stream — using
/// [java.util.Random] seeded with the same constant gives the same property (the sequence is not
/// the same as upstream's, but the visual effect — a deterministic drip — matches).
public final class Destroy {

  private Destroy() {}

  /// Delay the start of the animation so it doesn't start immediately.
  private static final int DELAY = 120;

  /// Higher means more pixels per frame are modified in the animation.
  private static final int DRIP_SPEED = 500;

  /// Delay the start of the text animation so it doesn't start immediately after the initial delay.
  private static final int TEXT_DELAY = 180;

  /// Destroy mode activated by pressing `d`.
  public static void destroy(Frame frame, int frameCountInput) {
    int frameCount = Math.max(0, frameCountInput - DELAY);
    if (frameCount == 0) {
      return;
    }
    Rect area = frame.area();
    Buffer buf = frame.bufferMut();
    drip(frameCount, area, buf);
    text(frameCount, area, buf);
  }

  /// Move a bunch of random pixels down one row.
  ///
  /// Each frame picks some random pixels and moves each down one row. This is a very inefficient
  /// way to do this, but it works well enough for this demo.
  private static void drip(int frameCount, Rect area, Buffer buf) {
    Random rng = new Random(10);
    int rampFrames = 450;
    double fractionalSpeed = (double) frameCount / (double) rampFrames;
    double variableSpeed = (double) DRIP_SPEED * fractionalSpeed * fractionalSpeed * fractionalSpeed;
    int pixelCount = (int) Math.floor((double) frameCount * variableSpeed);
    int width = area.width();
    int innerHeightHigh = Math.max(2, area.height() - 2);
    if (width <= 0 || innerHeightHigh <= 1) {
      return;
    }
    for (int i = 0; i < pixelCount; i++) {
      int srcX = rng.nextInt(width);
      int srcY = 1 + rng.nextInt(innerHeightHigh - 1); // 1..(height-2)
      Cell src = buf.cellAt(srcX, srcY).copy();
      // 1% of the time, move a blank or pixel (10:1) to the top line of the screen.
      if (randomRatio(rng, 1, 100)) {
        int lo = Math.max(0, srcX - 5);
        int hi = Math.min(area.right() - 1, srcX + 5);
        int destX;
        if (hi <= lo) {
          destX = clamp(srcX, area.left(), area.right() - 1);
        } else {
          int range = hi - lo;
          destX = clamp(lo + rng.nextInt(range), area.left(), area.right() - 1);
        }
        int destY = area.top() + 1;
        Cell dest = buf.cellAt(destX, destY);
        if (randomRatio(rng, 1, 10)) {
          copyInto(dest, src);
        } else {
          dest.reset();
        }
      } else {
        // move the pixel down one row
        int destX = srcX;
        int destY = Math.min(srcY + 1, area.bottom() - 2);
        copyInto(buf.cellAt(destX, destY), src);
      }
    }
  }

  /// Draw some text fading in and out from black to red and back.
  private static void text(int frameCount, Rect area, Buffer buf) {
    int subFrame = Math.max(0, frameCount - TEXT_DELAY);
    if (subFrame == 0) {
      return;
    }
    String logo =
        "██████      ████    ██████    ████    ██████  ██    ██  ██\n"
            + "██    ██  ██    ██    ██    ██    ██    ██    ██    ██  ██\n"
            + "██████    ████████    ██    ████████    ██    ██    ██  ██\n"
            + "██  ██    ██    ██    ██    ██    ██    ██    ██    ██  ██\n"
            + "██    ██  ██    ██    ██    ██    ██    ██      ████    ██\n";
    Text logoText = Text.styled(logo, Style.empty().withFg(new Color.Rgb(255, 255, 255)));
    Rect centered = centeredRect(area, logoText.width(), logoText.height());

    Buffer maskBuf = Buffer.empty(centered);
    renderTextWithoutBlock(logoText, centered, maskBuf);

    double percentage = clamp((double) subFrame / 480.0, 0.0, 1.0);

    for (int y = centered.top(); y < centered.bottom(); y++) {
      for (int x = centered.left(); x < centered.right(); x++) {
        Cell cell = buf.cellAt(x, y);
        Cell maskCell = maskBuf.cellAt(x, y);
        cell.setSymbol(maskCell.symbol());

        Color cellColor = unwrapBg(cell, new Color.Rgb(0, 0, 0));
        Color maskColor = unwrapFg(maskCell, new Color.Rgb(255, 0, 0));

        Color color = blend(maskColor, cellColor, percentage);
        cell.setStyle(Style.empty().withFg(color));
      }
    }
  }

  /// Mimic upstream's `cell.style().bg.unwrap_or(Color::Rgb(0, 0, 0))`. When the cell's bg is
  /// [Color.Reset] (the buffer's default sentinel for "unset"), fall back to the supplied color.
  private static Color unwrapBg(Cell cell, Color fallback) {
    Color bg = cell.style().bg().orElse(fallback);
    if (bg instanceof Color.Reset) {
      return fallback;
    }
    return bg;
  }

  private static Color unwrapFg(Cell cell, Color fallback) {
    Color fg = cell.style().fg().orElse(fallback);
    if (fg instanceof Color.Reset) {
      return fallback;
    }
    return fg;
  }

  private static Color blend(Color maskColor, Color cellColor, double percentage) {
    if (!(maskColor instanceof Color.Rgb maskRgb)) {
      return maskColor;
    }
    if (!(cellColor instanceof Color.Rgb cellRgb)) {
      return maskColor;
    }
    double remain = 1.0 - percentage;
    double red = (double) maskRgb.r() * percentage + (double) cellRgb.r() * remain;
    double green = (double) maskRgb.g() * percentage + (double) cellRgb.g() * remain;
    double blue = (double) maskRgb.b() * percentage + (double) cellRgb.b() * remain;
    return new Color.Rgb((int) red, (int) green, (int) blue);
  }

  /// A centered rect of the given size. Mirrors `centered_rect` in the Rust source.
  private static Rect centeredRect(Rect area, int width, int height) {
    Layout horizontal = Layout.horizontal(new Constraint.Length(width)).withFlex(Flex.Center);
    Layout vertical = Layout.vertical(new Constraint.Length(height)).withFlex(Flex.Center);
    Rect[] vSplit = area.layout(vertical, 1);
    Rect[] hSplit = vSplit[0].layout(horizontal, 1);
    return hSplit[0];
  }

  // ---- Cell helpers ----

  private static void copyInto(Cell dest, Cell src) {
    dest.setSymbol(src.symbol()).setFg(src.fg).setBg(src.bg).setUnderlineColor(src.underlineColor);
    dest.modifier = src.modifier;
    dest.skip = src.skip;
  }

  /// Render text into a buffer using the buffer's `setStringn` API, no Block. Mirrors a minimal
  /// `Text::render` for the destroy mode.
  private static void renderTextWithoutBlock(Text text, Rect area, Buffer buf) {
    int y = area.top();
    for (jatatui.core.text.Line line : text) {
      if (y >= area.bottom()) break;
      int x = area.left();
      Style lineStyle = text.style.patch(line.style);
      for (jatatui.core.text.Span span : line) {
        if (x >= area.right()) break;
        Style spanStyle = lineStyle.patch(span.style);
        var pos = buf.setStringn(x, y, span.content, area.right() - x, spanStyle);
        x = pos.x();
      }
      y++;
    }
  }

  private static int clamp(int value, int lo, int hi) {
    if (value < lo) return lo;
    if (value > hi) return hi;
    return value;
  }

  private static double clamp(double value, double lo, double hi) {
    if (value < lo) return lo;
    if (value > hi) return hi;
    return value;
  }

  /// Returns true with probability `numerator / denominator`. Mirrors `rand::Rng::random_ratio`.
  private static boolean randomRatio(Random rng, int numerator, int denominator) {
    if (denominator <= 0) return false;
    return rng.nextInt(denominator) < numerator;
  }
}
