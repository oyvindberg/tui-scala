package jatatui.examples.demo2;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.widgets.Widget;

/// Mirrors `apps/demo2/src/colors.rs`.
///
/// A widget that renders a color swatch of RGB colors plus a static helper that converts a
/// (hue, saturation, value) triple from the Okhsv color space to an sRGB [Color.Rgb].
///
/// ## Color-space math
///
/// Upstream uses the Rust `palette` crate to do `Okhsv -> Srgb`. Java has no equivalent crate
/// pulled into this project, so the conversion is implemented here from Björn Ottosson's published
/// formulas (<https://bottosson.github.io/posts/oklab/> and the companion Okhsv post). Only the two
/// values we actually need (`max_saturation = 1.0` and `max_value = 1.0`) are exercised by demo2,
/// so [#colorFromOklab(float, float, float)] is the only function exposed.
///
/// The implementation follows:
/// 1. Okhsv -> OkLrCh (Oklab cylindrical with toe-mapped lightness)
/// 2. Toe inverse to recover Oklab L
/// 3. Oklab -> linear sRGB via Ottosson's matrix
/// 4. Linear sRGB -> sRGB gamma encoding
public final class Colors {

  private Colors() {}

  /// A widget that renders a color swatch of RGB colors.
  ///
  /// The widget is rendered as a rectangle with the hue changing along the x-axis from 0.0 to 360.0
  /// and the value changing along the y-axis (from 1.0 to 0.0). Each pixel is rendered as a block
  /// character with the top half slightly lighter than the bottom half.
  public static final class RgbSwatch implements Widget {

    public RgbSwatch() {}

    @Override
    public void render(Rect area, Buffer buf) {
      int height = area.height();
      int width = area.width();
      for (int yi = 0; yi < height; yi++) {
        int y = area.top() + yi;
        float value = (float) height - (float) yi;
        float valueFg = value / (float) height;
        float valueBg = (value - 0.5f) / (float) height;
        for (int xi = 0; xi < width; xi++) {
          int x = area.left() + xi;
          float hue = (float) xi * 360.0f / (float) width;
          Color fg = colorFromOklab(hue, MAX_SATURATION, valueFg);
          Color bg = colorFromOklab(hue, MAX_SATURATION, valueBg);
          buf.cellAt(x, y).setSymbol("▀").setFg(fg).setBg(bg);
        }
      }
    }
  }

  /// Maximum saturation in Okhsv; fixed at 1.0 (mirrors `Okhsv::max_saturation()` in `palette`).
  public static final float MAX_SATURATION = 1.0f;

  /// Maximum value in Okhsv; fixed at 1.0 (mirrors `Okhsv::max_value()` in `palette`).
  public static final float MAX_VALUE = 1.0f;

  /// Convert a hue and value into an RGB color via the Okhsv → Oklab → linear sRGB → sRGB pipeline.
  ///
  /// `hue` is in degrees (0.0..360.0). `saturation` and `value` are 0..1.
  ///
  /// See <https://bottosson.github.io/posts/oklab/> for the math.
  public static Color colorFromOklab(float hue, float saturation, float value) {
    double h = ((double) hue) / 360.0; // hue in turns
    double s = saturation;
    double v = value;

    // -- Okhsv -> Oklab
    // Reference: https://bottosson.github.io/posts/colorpicker/#hsv-2
    double a_ = Math.cos(2.0 * Math.PI * h);
    double b_ = Math.sin(2.0 * Math.PI * h);

    Cusp cusp = findCusp(a_, b_);
    StSt st = stMax(a_, b_, cusp);
    double sMax = st.s();
    double tMax = st.t();
    double s0 = 0.5;
    double k = 1.0 - s0 / sMax;

    // first compute L and C on a toe straight line with constant lightness V
    double lV = 1.0 - s * s0 / (s0 + tMax - tMax * k * s);
    double cV = s * tMax * s0 / (s0 + tMax - tMax * k * s);

    double l = v * lV;
    double c = v * cV;

    // then compensate for both toe and the curved top part of the triangle
    double lVt = toeInv(lV);
    double cVt = cV * lVt / lV;

    double lNew = toeInv(l);
    c = c * lNew / l;
    l = lNew;

    double[] rgbScale = oklabToLinearSrgb(lVt, cVt * a_, cVt * b_);
    double scale = Math.cbrt(1.0 / Math.max(Math.max(rgbScale[0], rgbScale[1]), Math.max(rgbScale[2], 0.0)));

    l *= scale;
    c *= scale;

    double[] linear = oklabToLinearSrgb(l, c * a_, c * b_);

    int r = toSrgb255(linear[0]);
    int g = toSrgb255(linear[1]);
    int bComp = toSrgb255(linear[2]);
    return new Color.Rgb(r, g, bComp);
  }

  // ---- Helpers (Ottosson's reference implementation, ported from C/Rust) ----

  private record Cusp(double l, double c) {}

  private record StSt(double s, double t) {}

  /// Convert Oklab (L, a, b) to linear sRGB.
  ///
  /// Source: <https://bottosson.github.io/posts/oklab/>
  private static double[] oklabToLinearSrgb(double l, double a, double b) {
    double l_ = l + 0.3963377774 * a + 0.2158037573 * b;
    double m_ = l - 0.1055613458 * a - 0.0638541728 * b;
    double s_ = l - 0.0894841775 * a - 1.2914855480 * b;

    double l3 = l_ * l_ * l_;
    double m3 = m_ * m_ * m_;
    double s3 = s_ * s_ * s_;

    double r = 4.0767416621 * l3 - 3.3077115913 * m3 + 0.2309699292 * s3;
    double g = -1.2684380046 * l3 + 2.6097574011 * m3 - 0.3413193965 * s3;
    double bC = -0.0041960863 * l3 - 0.7034186147 * m3 + 1.7076147010 * s3;
    return new double[] {r, g, bC};
  }

  /// Find the L_cusp and C_cusp for a unit-vector (a, b) hue. Returns the maximum-chroma point
  /// where the gamut boundary is reached at this hue.
  ///
  /// Source: <https://bottosson.github.io/posts/gamutclipping/#intersection-with-srgb-gamut>
  private static Cusp findCusp(double a, double b) {
    double sCusp = computeMaxSaturation(a, b);
    double[] rgbAtMax = oklabToLinearSrgb(1.0, sCusp * a, sCusp * b);
    double lCusp =
        Math.cbrt(1.0 / Math.max(Math.max(rgbAtMax[0], rgbAtMax[1]), Math.max(rgbAtMax[2], 0.0)));
    double cCusp = lCusp * sCusp;
    return new Cusp(lCusp, cCusp);
  }

  /// Maximum saturation in Oklab for a given hue (a, b). From Ottosson's reference C++ code.
  private static double computeMaxSaturation(double a, double b) {
    double k0;
    double k1;
    double k2;
    double k3;
    double k4;
    double wl;
    double wm;
    double ws;

    if (-1.88170328 * a - 0.80936493 * b > 1.0) {
      // red
      k0 = 1.19086277;
      k1 = 1.76576728;
      k2 = 0.59662641;
      k3 = 0.75515197;
      k4 = 0.56771245;
      wl = 4.0767416621;
      wm = -3.3077115913;
      ws = 0.2309699292;
    } else if (1.81444104 * a - 1.19445276 * b > 1.0) {
      // green
      k0 = 0.73956515;
      k1 = -0.45954404;
      k2 = 0.08285427;
      k3 = 0.12541070;
      k4 = 0.14503204;
      wl = -1.2684380046;
      wm = 2.6097574011;
      ws = -0.3413193965;
    } else {
      // blue
      k0 = 1.35733652;
      k1 = -0.00915799;
      k2 = -1.15130210;
      k3 = -0.50559606;
      k4 = 0.00692167;
      wl = -0.0041960863;
      wm = -0.7034186147;
      ws = 1.7076147010;
    }

    double s = k0 + k1 * a + k2 * b + k3 * a * a + k4 * a * b;

    double k_l = 0.3963377774 * a + 0.2158037573 * b;
    double k_m = -0.1055613458 * a - 0.0638541728 * b;
    double k_s = -0.0894841775 * a - 1.2914855480 * b;

    double l_ = 1.0 + s * k_l;
    double m_ = 1.0 + s * k_m;
    double s_ = 1.0 + s * k_s;

    double l = l_ * l_ * l_;
    double m = m_ * m_ * m_;
    double sC = s_ * s_ * s_;

    double l_dS = 3.0 * k_l * l_ * l_;
    double m_dS = 3.0 * k_m * m_ * m_;
    double s_dS = 3.0 * k_s * s_ * s_;

    double l_dS2 = 6.0 * k_l * k_l * l_;
    double m_dS2 = 6.0 * k_m * k_m * m_;
    double s_dS2 = 6.0 * k_s * k_s * s_;

    double f = wl * l + wm * m + ws * sC;
    double f1 = wl * l_dS + wm * m_dS + ws * s_dS;
    double f2 = wl * l_dS2 + wm * m_dS2 + ws * s_dS2;

    s = s - f * f1 / (f1 * f1 - 0.5 * f * f2);
    return s;
  }

  /// Compute (S, T) from Cusp. From Ottosson's color picker post.
  private static StSt stMax(double a, double b, Cusp cusp) {
    double l = cusp.l();
    double c = cusp.c();
    return new StSt(c / l, c / (1.0 - l));
  }

  /// Toe and toe-inverse, from Ottosson's Okhsv post. Compresses L to make pure black behave well.
  private static double toe(double x) {
    double k1 = 0.206;
    double k2 = 0.03;
    double k3 = (1.0 + k1) / (1.0 + k2);
    return 0.5 * (k3 * x - k1 + Math.sqrt((k3 * x - k1) * (k3 * x - k1) + 4.0 * k2 * k3 * x));
  }

  private static double toeInv(double x) {
    double k1 = 0.206;
    double k2 = 0.03;
    double k3 = (1.0 + k1) / (1.0 + k2);
    return (x * x + k1 * x) / (k3 * (x + k2));
  }

  /// Linear sRGB component (0..1, may be slightly negative or above 1) → sRGB-encoded byte.
  private static int toSrgb255(double linear) {
    double clamped = Math.max(0.0, Math.min(1.0, linear));
    double encoded;
    if (clamped <= 0.0031308) {
      encoded = clamped * 12.92;
    } else {
      encoded = 1.055 * Math.pow(clamped, 1.0 / 2.4) - 0.055;
    }
    int v = (int) Math.round(encoded * 255.0);
    if (v < 0) return 0;
    if (v > 255) return 255;
    return v;
  }
}
