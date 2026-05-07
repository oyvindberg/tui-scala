package jatatui.components.theme;

import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import jatatui.react.Context;
import jatatui.react.RenderContext;

/// Bundle of styles for the visual identity of an app. Pass via [#PROVIDER]; consume via
/// [#useTheme].
///
/// `LIGHT` and `DARK` are sensible defaults; supply your own [Theme] record to override.
public record Theme(
    Style page,
    Style accent,
    Style border,
    Style title,
    Style success,
    Style warning,
    Style error,
    Style muted) {

  /// Default light theme — black on default bg.
  public static final Theme LIGHT =
      new Theme(
          Style.empty().withFg(new Color.Black()),
          Style.empty().withFg(new Color.Blue()).withAddModifier(Modifier.BOLD),
          Style.empty().withFg(new Color.DarkGray()),
          Style.empty().withFg(new Color.Black()).withAddModifier(Modifier.BOLD),
          Style.empty().withFg(new Color.Green()),
          Style.empty().withFg(new Color.Yellow()),
          Style.empty().withFg(new Color.Red()),
          Style.empty().withFg(new Color.Gray()));

  /// Default dark theme — white on default (terminal-bg) bg.
  public static final Theme DARK =
      new Theme(
          Style.empty().withFg(new Color.White()),
          Style.empty().withFg(new Color.LightCyan()).withAddModifier(Modifier.BOLD),
          Style.empty().withFg(new Color.Gray()),
          Style.empty().withFg(new Color.White()).withAddModifier(Modifier.BOLD),
          Style.empty().withFg(new Color.LightGreen()),
          Style.empty().withFg(new Color.LightYellow()),
          Style.empty().withFg(new Color.LightRed()),
          Style.empty().withFg(new Color.DarkGray()));

  /// Context holding the active theme. Defaults to [#LIGHT] when no provider is in scope.
  public static final Context<Theme> CONTEXT = Context.create(LIGHT);

  /// Read the active theme. Equivalent to `ctx.useContext(Theme.CONTEXT)`.
  public static Theme useTheme(RenderContext ctx) {
    return ctx.useContext(CONTEXT);
  }
}
