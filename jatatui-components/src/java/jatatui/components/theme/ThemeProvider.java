package jatatui.components.theme;

import static jatatui.react.Components.provide;

import jatatui.react.Element;

/// Convenience wrapper around `provide(Theme.CONTEXT, theme, child)`.
public final class ThemeProvider {
  private ThemeProvider() {}

  public static Element of(Theme theme, Element child) {
    return provide(Theme.CONTEXT, theme, child);
  }
}
