package jatatui.examples.colorsrgb;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Constraint;
import jatatui.core.layout.Layout;
import jatatui.core.layout.Position;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.terminal.Terminal;
import jatatui.core.text.Text;
import jatatui.core.widgets.Widget;
import jatatui.crossterm.CrosstermBackend;
import jatatui.crossterm.Jatatui;
import jatatui.widgets.paragraph.Paragraph;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Duration;
import tui.crossterm.Event;
import tui.crossterm.KeyEventKind;

/// A jatatui example that shows the full range of RGB colors that can be displayed in the
/// terminal.
///
/// Requires a terminal that supports 24-bit color (true color) and unicode.
///
/// This example also demonstrates how implementing rendering on a mutable widget allows the widget
/// to update its state while it is being rendered. The fps widget updates the fps calculation and
/// the colors widget updates a cached version of the colors to render instead of recalculating
/// them every frame.
///
/// Java port of `examples/apps/colors-rgb/src/main.rs` from ratatui v0.30.
///
/// **Deviation from upstream**: ratatui uses the `palette` crate's `Okhsv` (perceptually uniform
/// Oklab/Oklch color space) to compute the rainbow. Java does not have an Oklab implementation
/// in the standard library, so this port uses standard HSV → RGB conversion inline. The visual
/// effect is a rainbow gradient that animates horizontally; the perceptual uniformity differs but
/// the demo intent (FPS-driven double-buffered render) is preserved.
public final class ColorsRgbExample {

  private static final CrosstermJni JNI = new CrosstermJni();

  private ColorsRgbExample() {}

  public static void main(String[] args) throws IOException {
    Jatatui.runIo(terminal -> new App().run(terminal));
  }

  // ---- AppState ----

  private enum AppState {
    Running,
    Quit
  }

  // ---- App ----

  private static final class App implements Widget {
    private AppState state;
    private final FpsWidget fpsWidget;
    private final ColorsWidget colorsWidget;

    App() {
      this.state = AppState.Running;
      this.fpsWidget = new FpsWidget();
      this.colorsWidget = new ColorsWidget();
    }

    void run(Terminal<CrosstermBackend> terminal) throws IOException {
      while (isRunning()) {
        terminal.draw(frame -> frame.renderWidget(this, frame.area()));
        handleEvents();
      }
    }

    boolean isRunning() {
      return state == AppState.Running;
    }

    void handleEvents() {
      // ~60 fps cap: poll for at most 1/60s, drain a single event if available.
      Duration timeout = new Duration(0, 16_666_666);
      if (!JNI.poll(timeout)) {
        return;
      }
      Event event = JNI.read();
      if (event instanceof Event.Key key && key.keyEvent().kind() == KeyEventKind.Press) {
        state = AppState.Quit;
      }
    }

    @Override
    public void render(Rect area, Buffer buf) {
      Rect[] vertical =
          Layout.vertical(new Constraint.Length(1), new Constraint.Min(0)).split(area);
      Rect top = vertical[0];
      Rect colors = vertical[1];
      Rect[] horizontal =
          Layout.horizontal(new Constraint.Min(0), new Constraint.Length(8)).split(top);
      Rect title = horizontal[0];
      Rect fps = horizontal[1];
      Paragraph.of(Text.from("colors_rgb example. Press q to quit").centered()).render(title, buf);
      fpsWidget.render(fps, buf);
      colorsWidget.render(colors, buf);
    }
  }

  // ---- FpsWidget ----

  private static final class FpsWidget implements Widget {
    private int frameCount;
    private long lastInstantNanos;
    private Optional<Float> fps;

    FpsWidget() {
      this.frameCount = 0;
      this.lastInstantNanos = System.nanoTime();
      this.fps = Optional.empty();
    }

    @Override
    public void render(Rect area, Buffer buf) {
      calculateFps();
      if (fps.isPresent()) {
        String text = String.format("%.1f fps", fps.get());
        Paragraph.of(text).render(area, buf);
      }
    }

    private void calculateFps() {
      frameCount += 1;
      long now = System.nanoTime();
      long elapsedNanos = now - lastInstantNanos;
      if (elapsedNanos > 1_000_000_000L && frameCount > 2) {
        float elapsedSecs = elapsedNanos / 1_000_000_000.0f;
        fps = Optional.of(frameCount / elapsedSecs);
        frameCount = 0;
        lastInstantNanos = now;
      }
    }
  }

  // ---- ColorsWidget ----

  private static final class ColorsWidget implements Widget {
    /// The colors to render — should be double the height of the area as we render two rows of
    /// pixels for each row of the widget using the half block character. This is computed any
    /// time the size of the widget changes.
    private List<List<Color>> colors;

    /// The number of elapsed frames that have passed — used to animate the colors by shifting
    /// the x index by the frame number.
    private int frameCount;

    ColorsWidget() {
      this.colors = new ArrayList<>();
      this.frameCount = 0;
    }

    @Override
    public void render(Rect area, Buffer buf) {
      setupColors(area);
      int w = area.width();
      for (int xi = 0, x = area.left(); x < area.right(); xi++, x++) {
        // animate the colors by shifting the x index by the frame number
        int shifted = w == 0 ? 0 : Math.floorMod(xi + frameCount, w);
        for (int yi = 0, y = area.top(); y < area.bottom(); yi++, y++) {
          // render a half block character for each row of pixels with the foreground color set to
          // the color of the pixel and the background color set to the color of the pixel below it
          Color fg = colors.get(yi * 2).get(shifted);
          Color bg = colors.get(yi * 2 + 1).get(shifted);
          buf.cellAt(new Position(x, y)).setSymbol("▀").setFg(fg).setBg(bg);
        }
      }
      frameCount += 1;
    }

    private void setupColors(Rect size) {
      int width = size.width();
      // double the height because each screen row has two rows of half block pixels
      int height = size.height() * 2;
      // only update the colors if the size has changed since the last time we rendered
      if (colors.size() == height && (height == 0 || colors.get(0).size() == width)) {
        return;
      }
      colors = new ArrayList<>(height);
      for (int y = 0; y < height; y++) {
        List<Color> row = new ArrayList<>(width);
        for (int x = 0; x < width; x++) {
          float hue = width == 0 ? 0.0f : x * 360.0f / width;
          float value = height == 0 ? 0.0f : (height - y) / (float) height;
          float saturation = 1.0f;
          row.add(hsvToRgb(hue, saturation, value));
        }
        colors.add(row);
      }
    }
  }

  /// Standard HSV -> RGB conversion (replaces upstream's perceptual `Okhsv` from the `palette`
  /// crate — see class-level Javadoc for the deviation rationale). `hue` in [0, 360),
  /// `saturation` and `value` in [0, 1].
  private static Color hsvToRgb(float hue, float saturation, float value) {
    float h = ((hue % 360.0f) + 360.0f) % 360.0f;
    float c = value * saturation;
    float x = c * (1.0f - Math.abs((h / 60.0f) % 2.0f - 1.0f));
    float m = value - c;
    float r1;
    float g1;
    float b1;
    if (h < 60.0f) {
      r1 = c;
      g1 = x;
      b1 = 0.0f;
    } else if (h < 120.0f) {
      r1 = x;
      g1 = c;
      b1 = 0.0f;
    } else if (h < 180.0f) {
      r1 = 0.0f;
      g1 = c;
      b1 = x;
    } else if (h < 240.0f) {
      r1 = 0.0f;
      g1 = x;
      b1 = c;
    } else if (h < 300.0f) {
      r1 = x;
      g1 = 0.0f;
      b1 = c;
    } else {
      r1 = c;
      g1 = 0.0f;
      b1 = x;
    }
    int r = clamp8((int) Math.round((r1 + m) * 255.0f));
    int g = clamp8((int) Math.round((g1 + m) * 255.0f));
    int b = clamp8((int) Math.round((b1 + m) * 255.0f));
    return new Color.Rgb(r, g, b);
  }

  private static int clamp8(int v) {
    if (v < 0) return 0;
    if (v > 255) return 255;
    return v;
  }
}
