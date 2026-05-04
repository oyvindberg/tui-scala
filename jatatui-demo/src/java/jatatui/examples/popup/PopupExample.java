package jatatui.examples.popup;

import jatatui.core.layout.Constraint;
import jatatui.core.layout.Flex;
import jatatui.core.layout.Layout;
import jatatui.core.layout.Rect;
import jatatui.core.terminal.Frame;
import jatatui.core.terminal.Terminal;
import jatatui.core.text.Line;
import jatatui.crossterm.CrosstermBackend;
import jatatui.crossterm.Jatatui;
import jatatui.widgets.Clear;
import jatatui.widgets.block.Block;
import jatatui.widgets.paragraph.Paragraph;
import java.io.IOException;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Event;
import tui.crossterm.KeyCode;
import tui.crossterm.KeyEventKind;

/// A jatatui example that demonstrates how to handle popups.
///
/// Mirrors `examples/apps/popup/src/main.rs`. Press 'p' to toggle the popup, 'q' to quit.
public final class PopupExample {

  private static final CrosstermJni JNI = new CrosstermJni();

  private PopupExample() {}

  public static void main(String[] args) throws IOException {
    Jatatui.runIo(terminal -> run(terminal));
  }

  private static void run(Terminal<CrosstermBackend> terminal) throws IOException {
    boolean[] showPopup = {false};
    while (true) {
      terminal.draw(frame -> render(frame, showPopup[0]));
      Event event = JNI.read();
      if (event instanceof Event.Key keyEvt
          && keyEvt.keyEvent().kind() == KeyEventKind.Press) {
        KeyCode code = keyEvt.keyEvent().code();
        if (code instanceof KeyCode.Char ch) {
          if (ch.c() == 'q') {
            return;
          } else if (ch.c() == 'p') {
            showPopup[0] = !showPopup[0];
          }
        }
      }
    }
  }

  private static void render(Frame frame, boolean showPopup) {
    Rect area = frame.area();

    Layout layout = Layout.vertical(new Constraint.Length(1), new Constraint.Fill(1));
    Rect[] split = area.layout(layout, 2);
    Rect instructions = split[0];
    Rect content = split[1];

    frame.renderWidget(
        Paragraph.of(Line.from("Press 'p' to toggle popup, 'q' to quit").centered()),
        instructions);

    frame.renderWidget(Block.bordered().withTitle("Content").onBlue(), content);

    if (showPopup) {
      Block popup = Block.bordered().withTitle("Popup");
      Rect popupArea = centeredArea(area, 60, 20);
      // clears out any background in the area before rendering the popup
      frame.renderWidget(Clear.INSTANCE, popupArea);
      frame.renderWidget(popup, popupArea);
    }
  }

  /// Create a centered rect using up certain percentage of the available rect.
  private static Rect centeredArea(Rect area, int percentX, int percentY) {
    Layout vertical = Layout.vertical(new Constraint.Percentage(percentY)).withFlex(Flex.Center);
    Layout horizontal =
        Layout.horizontal(new Constraint.Percentage(percentX)).withFlex(Flex.Center);
    Rect vArea = area.layout(vertical, 1)[0];
    return vArea.layout(horizontal, 1)[0];
  }
}
