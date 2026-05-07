package jatatui.components.router;

import jatatui.react.Element;

/// One entry on the router's screen stack: a `label` (for breadcrumbs / titles) and the `body`
/// Element that renders this screen.
public record Screen(String label, Element body) {

  public static Screen of(String label, Element body) {
    return new Screen(label, body);
  }
}
