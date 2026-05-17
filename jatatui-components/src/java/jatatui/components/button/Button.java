package jatatui.components.button;

import static jatatui.react.Components.*;

import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import jatatui.core.text.Line;
import jatatui.core.text.Span;
import jatatui.core.widgets.Widget;
import jatatui.react.Element;
import jatatui.widgets.Borders;
import jatatui.widgets.block.Block;
import jatatui.widgets.block.BorderType;
import jatatui.widgets.paragraph.Paragraph;
import java.util.Optional;
import tui.crossterm.KeyCode;

/// Bordered, Tab-focusable button. Enter (when focused) or mouse click activates.
///
/// Two visual variants via [#primary]:
///   - `true` — green border, intended for the dominant action on a form ("Save", "Confirm")
///   - `false` — gray border, secondary actions ("Cancel", "Back")
///
/// When focused the border switches to the thick variant + cyan, so the user can tell at a
/// glance which button Enter would activate.
public final class Button {
  private Button() {}

  /// Build a button. `focusId` participates in the FocusManager — pick something stable so
  /// imperative `ctx.focus(id)` works and Tab order is predictable.
  public static Element of(String label, String focusId, boolean primary, Runnable onClick) {
    return component(
        ctx -> {
          boolean focused = ctx.useFocus(Optional.of(focusId), false);

          if (focused) ctx.onKey(new KeyCode.Enter(), onClick::run);
          ctx.onClick(e -> onClick.run());

          Color accent = primary ? new Color.Green() : new Color.Gray();
          BorderType borderType = focused ? BorderType.Thick : BorderType.Rounded;
          Style borderStyle = Style.empty().withFg(focused ? new Color.Cyan() : accent);
          Style textStyle =
              Style.empty()
                  .withFg(focused ? new Color.White() : accent)
                  .withAddModifier(Modifier.BOLD);

          Widget w =
              (area, buffer) -> {
                Block block =
                    Block.empty()
                        .withBorders(Borders.ALL)
                        .withBorderType(borderType)
                        .withBorderStyle(borderStyle);
                Paragraph.of(Line.from(Span.styled("  " + label, textStyle)))
                    .withBlock(block)
                    .render(area, buffer);
              };
          return widget(w);
        });
  }
}
