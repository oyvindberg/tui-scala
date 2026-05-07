package jatatui.components.modal;

import static jatatui.react.Components.*;

import jatatui.core.layout.Rect;
import jatatui.react.Element;
import jatatui.widgets.Borders;
import tui.crossterm.KeyCode;

/// Modal dialog rendered via two portals layered on top of the screen:
///   - **Backdrop** (full screen): click-eater + dismiss-on-outside-click. Clicks anywhere
///     outside the modal box invoke `onDismiss` and are stopped from reaching the rest of the UI.
///   - **Box** (centered, sized via [ModalProps#width]/[ModalProps#height]): renders a bordered
///     [box] with the title, containing the body Element. Esc invokes `onDismiss` and is stopped.
///
/// When [ModalProps#open] is false, renders nothing.
///
/// Bubbling: the modal's portal children attach to the modal Component's fiber, so events bubble
/// through the modal first. Clicks on the box stop propagation so the backdrop's dismiss handler
/// doesn't also fire.
public final class Modal {
  private Modal() {}

  public static Element of(ModalProps props) {
    if (!props.open()) return empty();

    return component(
        ctx -> {
          Rect screen = ctx.frame().area();
          Rect boxArea = centerInside(screen, props.width(), props.height());

          Element backdrop =
              component(
                  c -> {
                    c.onClick(
                        e -> {
                          props.onDismiss().run();
                          e.stopPropagation();
                        });
                    return empty();
                  });

          Element boxLayer =
              component(
                  c -> {
                    // Eat clicks on the modal box so the backdrop's onDismiss doesn't fire.
                    c.onClick(e -> e.stopPropagation());
                    c.onKey(
                        new KeyCode.Esc(),
                        e -> {
                          props.onDismiss().run();
                          e.stopPropagation();
                        });
                    // Stack: Clear first so the box covers whatever was painted underneath, then
                    // the bordered box on top.
                    return stack(
                        widget(jatatui.widgets.Clear.instance()),
                        box(props.title(), Borders.ALL, props.body()));
                  });

          // Two siblings under one component → both portals share Modal's fiber as parent for
          // event bubbling.
          return column(portal(backdrop, screen), portal(boxLayer, boxArea));
        });
  }

  /// Centered Rect of the given size, clamped to fit inside `outer`.
  static Rect centerInside(Rect outer, int width, int height) {
    int w = Math.min(width, outer.width());
    int h = Math.min(height, outer.height());
    int x = outer.x() + (outer.width() - w) / 2;
    int y = outer.y() + (outer.height() - h) / 2;
    return new Rect(x, y, w, h);
  }
}
