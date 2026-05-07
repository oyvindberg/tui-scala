package jatatui.components.modal;

import jatatui.core.style.Style;
import jatatui.react.Element;
import java.util.Optional;

/// Props for [Modal]. `width` / `height` are absolute cell counts; the modal centers itself on
/// the screen. If the screen is smaller than the requested size, the modal shrinks to fit.
public record ModalProps(
    boolean open,
    String title,
    Element body,
    Runnable onDismiss,
    int width,
    int height,
    Optional<Style> backdropStyle,
    Style boxStyle) {

  /// Default-styled factory: 40 wide × 12 tall. No backdrop dimming. Esc / click-outside dismiss.
  public static ModalProps of(boolean open, String title, Element body, Runnable onDismiss) {
    return new ModalProps(
        open, title, body, onDismiss, 40, 12, Optional.empty(), Style.empty());
  }

  public ModalProps withSize(int width, int height) {
    return new ModalProps(open, title, body, onDismiss, width, height, backdropStyle, boxStyle);
  }

  public ModalProps withBackdropStyle(Style backdropStyle) {
    return new ModalProps(
        open, title, body, onDismiss, width, height, Optional.of(backdropStyle), boxStyle);
  }

  public ModalProps withBoxStyle(Style boxStyle) {
    return new ModalProps(open, title, body, onDismiss, width, height, backdropStyle, boxStyle);
  }
}
