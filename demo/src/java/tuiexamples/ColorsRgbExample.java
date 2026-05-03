package tuiexamples;

import tui.Alignment;
import tui.Buffer;
import tui.Color;
import tui.Constraint;
import tui.Direction;
import tui.Layout;
import tui.Margin;
import tui.Rect;
import tui.Style;
import tui.Terminal;
import tui.Text;
import tui.Widget;
import tui.WithTerminal;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Duration;
import tui.crossterm.Event;
import tui.crossterm.KeyCode;
import tui.widgets.ParagraphWidget;

/// Renders the full RGB color range as a grid of half-block characters. Requires a terminal
/// that supports 24-bit color (true color).
public final class ColorsRgbExample {
  private ColorsRgbExample() {}

  public static void main(String[] args) {
    WithTerminal.apply((jni, terminal) -> {
      runApp(terminal, jni);
      return null;
    });
  }

  public static void runApp(Terminal terminal, CrosstermJni jni) {
    while (true) {
      terminal.draw(
          frame -> {
            Layout layout =
                new Layout(
                    Direction.Vertical,
                    Margin.of(0),
                    new Constraint[] {new Constraint.Length(1), new Constraint.Min(0)},
                    true);
            Rect[] chunks = layout.split(frame.size);

            ParagraphWidget title =
                ParagraphWidget.empty(Text.nostyle("colors_rgb example. Press q to quit"))
                    .withStyle(Style.empty().withFg(Color.DarkGray))
                    .withAlignment(Alignment.Center);
            frame.renderWidget(title, chunks[0]);

            frame.renderWidget(new RgbColors(), chunks[1]);
          });

      Duration timeout = new Duration(0, 100_000_000);
      if (jni.poll(timeout)) {
        Event ev = jni.read();
        if (ev instanceof Event.Key key
            && key.keyEvent().code() instanceof KeyCode.Char c
            && c.c() == 'q') {
          return;
        }
      }
    }
  }

  private static final class RgbColors implements Widget {
    @Override
    public void render(Rect area, Buffer buf) {
      // Each cell renders a half-block character. The fg encodes the upper half's color
      // and the bg encodes the lower half's color, doubling vertical resolution.
      for (int xi = 0; xi < area.width(); xi++) {
        int x = area.left() + xi;
        for (int yi = 0; yi < area.height(); yi++) {
          int y = area.top() + yi;
          double hue = (double) xi * 360.0 / Math.max(1, area.width());

          double valueFg = (double) yi / Math.max(0.5, area.height() - 0.5);
          double valueBg = ((double) yi + 0.5) / Math.max(0.5, area.height() - 0.5);

          Color fg = hsvToRgb(hue, 1.0, clamp01(valueFg));
          Color bg = hsvToRgb(hue, 1.0, clamp01(valueBg));

          buf.get(x, y).setChar('▀').setFg(fg).setBg(bg);
        }
      }
    }
  }

  private static double clamp01(double v) {
    if (v < 0) return 0;
    if (v > 1) return 1;
    return v;
  }

  /// Plain HSV → RGB conversion. Not perceptually uniform like Okhsv, but produces a
  /// recognizable rainbow gradient sufficient for a smoke test of 24-bit color.
  private static Color hsvToRgb(double hueDeg, double saturation, double value) {
    double h = ((hueDeg % 360.0) + 360.0) % 360.0 / 60.0;
    double c = value * saturation;
    double x = c * (1 - Math.abs((h % 2.0) - 1.0));
    double r1, g1, b1;
    if (h < 1) {
      r1 = c;
      g1 = x;
      b1 = 0;
    } else if (h < 2) {
      r1 = x;
      g1 = c;
      b1 = 0;
    } else if (h < 3) {
      r1 = 0;
      g1 = c;
      b1 = x;
    } else if (h < 4) {
      r1 = 0;
      g1 = x;
      b1 = c;
    } else if (h < 5) {
      r1 = x;
      g1 = 0;
      b1 = c;
    } else {
      r1 = c;
      g1 = 0;
      b1 = x;
    }
    double m = value - c;
    int r = (int) Math.round((r1 + m) * 255.0);
    int g = (int) Math.round((g1 + m) * 255.0);
    int b = (int) Math.round((b1 + m) * 255.0);
    return new Color.Rgb(clampByte(r), clampByte(g), clampByte(b));
  }

  private static int clampByte(int v) {
    if (v < 0) return 0;
    if (v > 255) return 255;
    return v;
  }
}
