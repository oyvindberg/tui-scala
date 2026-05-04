package jatatui.core.style.palette;

import jatatui.core.style.Color;

/// Represents the Tailwind CSS [default color palette][palette].
///
/// [palette]: https://tailwindcss.com/docs/customizing-colors#default-color-palette
///
/// There are 22 palettes. Each palette has 11 colors, with variants from 50 to 950. Black and
/// White are included for completeness.
///
/// ## Example
///
/// ```
/// import jatatui.core.style.Color;
/// import jatatui.core.style.palette.Tailwind;
///
/// assert Tailwind.RED.c500.equals(new Color.Rgb(239, 68, 68));
/// assert Tailwind.BLUE.c500.equals(new Color.Rgb(59, 130, 246));
/// ```
public final class Tailwind {
  private Tailwind() {}

  /// A Tailwind color palette of 11 shades, indexed 50, 100, 200, ..., 900, 950.
  public record Palette(
      Color c50,
      Color c100,
      Color c200,
      Color c300,
      Color c400,
      Color c500,
      Color c600,
      Color c700,
      Color c800,
      Color c900,
      Color c950) {}

  // ---- Black and white (top-level) ----

  public static final Color BLACK = Color.fromU32(0x000000);
  public static final Color WHITE = Color.fromU32(0xffffff);

  // ---- Palettes ----

  public static final Palette SLATE =
      new Palette(
          Color.fromU32(0xf8fafc),
          Color.fromU32(0xf1f5f9),
          Color.fromU32(0xe2e8f0),
          Color.fromU32(0xcbd5e1),
          Color.fromU32(0x94a3b8),
          Color.fromU32(0x64748b),
          Color.fromU32(0x475569),
          Color.fromU32(0x334155),
          Color.fromU32(0x1e293b),
          Color.fromU32(0x0f172a),
          Color.fromU32(0x020617));

  public static final Palette GRAY =
      new Palette(
          Color.fromU32(0xf9fafb),
          Color.fromU32(0xf3f4f6),
          Color.fromU32(0xe5e7eb),
          Color.fromU32(0xd1d5db),
          Color.fromU32(0x9ca3af),
          Color.fromU32(0x6b7280),
          Color.fromU32(0x4b5563),
          Color.fromU32(0x374151),
          Color.fromU32(0x1f2937),
          Color.fromU32(0x111827),
          Color.fromU32(0x030712));

  public static final Palette ZINC =
      new Palette(
          Color.fromU32(0xfafafa),
          Color.fromU32(0xf4f4f5),
          Color.fromU32(0xe4e4e7),
          Color.fromU32(0xd4d4d8),
          Color.fromU32(0xa1a1aa),
          Color.fromU32(0x71717a),
          Color.fromU32(0x52525b),
          Color.fromU32(0x3f3f46),
          Color.fromU32(0x27272a),
          Color.fromU32(0x18181b),
          Color.fromU32(0x09090b));

  public static final Palette NEUTRAL =
      new Palette(
          Color.fromU32(0xfafafa),
          Color.fromU32(0xf5f5f5),
          Color.fromU32(0xe5e5e5),
          Color.fromU32(0xd4d4d4),
          Color.fromU32(0xa3a3a3),
          Color.fromU32(0x737373),
          Color.fromU32(0x525252),
          Color.fromU32(0x404040),
          Color.fromU32(0x262626),
          Color.fromU32(0x171717),
          Color.fromU32(0x0a0a0a));

  public static final Palette STONE =
      new Palette(
          Color.fromU32(0xfafaf9),
          Color.fromU32(0xf5f5f4),
          Color.fromU32(0xe7e5e4),
          Color.fromU32(0xd6d3d1),
          Color.fromU32(0xa8a29e),
          Color.fromU32(0x78716c),
          Color.fromU32(0x57534e),
          Color.fromU32(0x44403c),
          Color.fromU32(0x292524),
          Color.fromU32(0x1c1917),
          Color.fromU32(0x0c0a09));

  public static final Palette RED =
      new Palette(
          Color.fromU32(0xfef2f2),
          Color.fromU32(0xfee2e2),
          Color.fromU32(0xfecaca),
          Color.fromU32(0xfca5a5),
          Color.fromU32(0xf87171),
          Color.fromU32(0xef4444),
          Color.fromU32(0xdc2626),
          Color.fromU32(0xb91c1c),
          Color.fromU32(0x991b1b),
          Color.fromU32(0x7f1d1d),
          Color.fromU32(0x450a0a));

  public static final Palette ORANGE =
      new Palette(
          Color.fromU32(0xfff7ed),
          Color.fromU32(0xffedd5),
          Color.fromU32(0xfed7aa),
          Color.fromU32(0xfdba74),
          Color.fromU32(0xfb923c),
          Color.fromU32(0xf97316),
          Color.fromU32(0xea580c),
          Color.fromU32(0xc2410c),
          Color.fromU32(0x9a3412),
          Color.fromU32(0x7c2d12),
          Color.fromU32(0x431407));

  public static final Palette AMBER =
      new Palette(
          Color.fromU32(0xfffbeb),
          Color.fromU32(0xfef3c7),
          Color.fromU32(0xfde68a),
          Color.fromU32(0xfcd34d),
          Color.fromU32(0xfbbf24),
          Color.fromU32(0xf59e0b),
          Color.fromU32(0xd97706),
          Color.fromU32(0xb45309),
          Color.fromU32(0x92400e),
          Color.fromU32(0x78350f),
          Color.fromU32(0x451a03));

  public static final Palette YELLOW =
      new Palette(
          Color.fromU32(0xfefce8),
          Color.fromU32(0xfef9c3),
          Color.fromU32(0xfef08a),
          Color.fromU32(0xfde047),
          Color.fromU32(0xfacc15),
          Color.fromU32(0xeab308),
          Color.fromU32(0xca8a04),
          Color.fromU32(0xa16207),
          Color.fromU32(0x854d0e),
          Color.fromU32(0x713f12),
          Color.fromU32(0x422006));

  public static final Palette LIME =
      new Palette(
          Color.fromU32(0xf7fee7),
          Color.fromU32(0xecfccb),
          Color.fromU32(0xd9f99d),
          Color.fromU32(0xbef264),
          Color.fromU32(0xa3e635),
          Color.fromU32(0x84cc16),
          Color.fromU32(0x65a30d),
          Color.fromU32(0x4d7c0f),
          Color.fromU32(0x3f6212),
          Color.fromU32(0x365314),
          Color.fromU32(0x1a2e05));

  public static final Palette GREEN =
      new Palette(
          Color.fromU32(0xf0fdf4),
          Color.fromU32(0xdcfce7),
          Color.fromU32(0xbbf7d0),
          Color.fromU32(0x86efac),
          Color.fromU32(0x4ade80),
          Color.fromU32(0x22c55e),
          Color.fromU32(0x16a34a),
          Color.fromU32(0x15803d),
          Color.fromU32(0x166534),
          Color.fromU32(0x14532d),
          Color.fromU32(0x052e16));

  public static final Palette EMERALD =
      new Palette(
          Color.fromU32(0xecfdf5),
          Color.fromU32(0xd1fae5),
          Color.fromU32(0xa7f3d0),
          Color.fromU32(0x6ee7b7),
          Color.fromU32(0x34d399),
          Color.fromU32(0x10b981),
          Color.fromU32(0x059669),
          Color.fromU32(0x047857),
          Color.fromU32(0x065f46),
          Color.fromU32(0x064e3b),
          Color.fromU32(0x022c22));

  public static final Palette TEAL =
      new Palette(
          Color.fromU32(0xf0fdfa),
          Color.fromU32(0xccfbf1),
          Color.fromU32(0x99f6e4),
          Color.fromU32(0x5eead4),
          Color.fromU32(0x2dd4bf),
          Color.fromU32(0x14b8a6),
          Color.fromU32(0x0d9488),
          Color.fromU32(0x0f766e),
          Color.fromU32(0x115e59),
          Color.fromU32(0x134e4a),
          Color.fromU32(0x042f2e));

  public static final Palette CYAN =
      new Palette(
          Color.fromU32(0xecfeff),
          Color.fromU32(0xcffafe),
          Color.fromU32(0xa5f3fc),
          Color.fromU32(0x67e8f9),
          Color.fromU32(0x22d3ee),
          Color.fromU32(0x06b6d4),
          Color.fromU32(0x0891b2),
          Color.fromU32(0x0e7490),
          Color.fromU32(0x155e75),
          Color.fromU32(0x164e63),
          Color.fromU32(0x083344));

  public static final Palette SKY =
      new Palette(
          Color.fromU32(0xf0f9ff),
          Color.fromU32(0xe0f2fe),
          Color.fromU32(0xbae6fd),
          Color.fromU32(0x7dd3fc),
          Color.fromU32(0x38bdf8),
          Color.fromU32(0x0ea5e9),
          Color.fromU32(0x0284c7),
          Color.fromU32(0x0369a1),
          Color.fromU32(0x075985),
          Color.fromU32(0x0c4a6e),
          Color.fromU32(0x082f49));

  public static final Palette BLUE =
      new Palette(
          Color.fromU32(0xeff6ff),
          Color.fromU32(0xdbeafe),
          Color.fromU32(0xbfdbfe),
          Color.fromU32(0x93c5fd),
          Color.fromU32(0x60a5fa),
          Color.fromU32(0x3b82f6),
          Color.fromU32(0x2563eb),
          Color.fromU32(0x1d4ed8),
          Color.fromU32(0x1e40af),
          Color.fromU32(0x1e3a8a),
          Color.fromU32(0x172554));

  public static final Palette INDIGO =
      new Palette(
          Color.fromU32(0xeef2ff),
          Color.fromU32(0xe0e7ff),
          Color.fromU32(0xc7d2fe),
          Color.fromU32(0xa5b4fc),
          Color.fromU32(0x818cf8),
          Color.fromU32(0x6366f1),
          Color.fromU32(0x4f46e5),
          Color.fromU32(0x4338ca),
          Color.fromU32(0x3730a3),
          Color.fromU32(0x312e81),
          Color.fromU32(0x1e1b4b));

  public static final Palette VIOLET =
      new Palette(
          Color.fromU32(0xf5f3ff),
          Color.fromU32(0xede9fe),
          Color.fromU32(0xddd6fe),
          Color.fromU32(0xc4b5fd),
          Color.fromU32(0xa78bfa),
          Color.fromU32(0x8b5cf6),
          Color.fromU32(0x7c3aed),
          Color.fromU32(0x6d28d9),
          Color.fromU32(0x5b21b6),
          Color.fromU32(0x4c1d95),
          Color.fromU32(0x2e1065));

  public static final Palette PURPLE =
      new Palette(
          Color.fromU32(0xfaf5ff),
          Color.fromU32(0xf3e8ff),
          Color.fromU32(0xe9d5ff),
          Color.fromU32(0xd8b4fe),
          Color.fromU32(0xc084fc),
          Color.fromU32(0xa855f7),
          Color.fromU32(0x9333ea),
          Color.fromU32(0x7e22ce),
          Color.fromU32(0x6b21a8),
          Color.fromU32(0x581c87),
          Color.fromU32(0x3b0764));

  public static final Palette FUCHSIA =
      new Palette(
          Color.fromU32(0xfdf4ff),
          Color.fromU32(0xfae8ff),
          Color.fromU32(0xf5d0fe),
          Color.fromU32(0xf0abfc),
          Color.fromU32(0xe879f9),
          Color.fromU32(0xd946ef),
          Color.fromU32(0xc026d3),
          Color.fromU32(0xa21caf),
          Color.fromU32(0x86198f),
          Color.fromU32(0x701a75),
          Color.fromU32(0x4a044e));

  public static final Palette PINK =
      new Palette(
          Color.fromU32(0xfdf2f8),
          Color.fromU32(0xfce7f3),
          Color.fromU32(0xfbcfe8),
          Color.fromU32(0xf9a8d4),
          Color.fromU32(0xf472b6),
          Color.fromU32(0xec4899),
          Color.fromU32(0xdb2777),
          Color.fromU32(0xbe185d),
          Color.fromU32(0x9d174d),
          Color.fromU32(0x831843),
          Color.fromU32(0x500724));

  public static final Palette ROSE =
      new Palette(
          Color.fromU32(0xfff1f2),
          Color.fromU32(0xffe4e6),
          Color.fromU32(0xfecdd3),
          Color.fromU32(0xfda4af),
          Color.fromU32(0xfb7185),
          Color.fromU32(0xf43f5e),
          Color.fromU32(0xe11d48),
          Color.fromU32(0xbe123c),
          Color.fromU32(0x9f1239),
          Color.fromU32(0x881337),
          Color.fromU32(0x4c0519));
}
