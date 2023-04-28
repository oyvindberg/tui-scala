/// A simple example demonstrating how to handle user input. This is
/// a bit out of the scope of the library as it does not provide any
/// input handling out of the box. However, it may helps some to get
/// started.
///
/// This is a very simple example:
///   * A input box always focused. Every character you type is registered
///   here
///   * Pressing Backspace erases a character
///   * Pressing Enter pushes the current input in the history of previous
///   messages

package tuiexamples

import tui._
import tui.crossterm.{CrosstermJni, KeyCode}
import tui.widgets.{BlockWidget, ParagraphWidget}
import tui.widgets.ListWidget

object UserInputExample {
  sealed trait InputMode
  object InputMode {
    case object Normal extends InputMode
    case object Editing extends InputMode
  }

  /// App holds the state of the application
  case class App(
      /// Current value of the input box
      var input: String = "",
      /// Current input mode
      var input_mode: InputMode = InputMode.Normal,
      /// History of recorded messages
      var messages: Array[String] = Array.empty
  )

  def main(args: Array[String]): Unit = withTerminal { (jni, terminal) =>
    // create app and run it
    val app = App()
    run_app(terminal, app, jni)
  }

  def run_app(terminal: Terminal, app: App, jni: CrosstermJni): Unit =
    while (true) {
      terminal.draw(f => ui(f, app))

      jni.read() match {
        case key: tui.crossterm.Event.Key =>
          app.input_mode match {
            case InputMode.Normal =>
              key.keyEvent().code() match {
                case c: KeyCode.Char if c.c == 'e' => app.input_mode = InputMode.Editing;
                case c: KeyCode.Char if c.c == 'q' => return
                case _                             => ()
              }
            case InputMode.Editing =>
              key.keyEvent().code() match {
                case _: KeyCode.Enter =>
                  app.messages = app.messages :+ app.input
                  app.input = ""
                case c: KeyCode.Char      => app.input = app.input + c.c();
                case _: KeyCode.Backspace => app.input = app.input.substring(0, app.input.length - 1)
                case _: KeyCode.Esc       => app.input_mode = InputMode.Normal;
                case _                    => ()
              }
          }
        case _ => ()
      }
    }

  def ui(f: Frame, app: App): Unit = {

    val (msg, style) = app.input_mode match {
      case InputMode.Normal =>
        (
          Text.from(
            Span.nostyle("Press "),
            Span.styled("q", Style.DEFAULT.addModifier(Modifier.BOLD)),
            Span.nostyle(" to exit, "),
            Span.styled("e", Style.DEFAULT.addModifier(Modifier.BOLD)),
            Span.nostyle(" to start editing.")
          ),
          Style.DEFAULT.addModifier(Modifier.RAPID_BLINK)
        )
      case InputMode.Editing =>
        (
          Text.from(
            Span.nostyle("Press "),
            Span.styled("Esc", Style.DEFAULT.addModifier(Modifier.BOLD)),
            Span.nostyle(" to stop editing, "),
            Span.styled("Enter", Style.DEFAULT.addModifier(Modifier.BOLD)),
            Span.nostyle(" to record the message")
          ),
          Style.DEFAULT
        )
    }
    val items: Array[ListWidget.Item] =
      app.messages.zipWithIndex.map { case (m, i) => ListWidget.Item(content = Text.nostyle(s"$i: $m")) }

    Layout(direction = Direction.Vertical, margin = Margin(2))(
      Constraint.Length(1) -> ParagraphWidget(text = msg.overwrittenStyle(style)),
      Constraint.Length(3) -> { (area, buf) =>
        ParagraphWidget(
          text = Text.nostyle(app.input),
          block = Some(BlockWidget(borders = Borders.ALL, title = Some(Spans.nostyle("Input")))),
          style = app.input_mode match {
            case InputMode.Normal  => Style.DEFAULT
            case InputMode.Editing => Style.DEFAULT.fg(Color.Yellow)
          }
        ).render(area, buf)
        app.input_mode match {
          case InputMode.Normal =>
            // Hide the cursor. `Frame` does this by default, so we don't need to do anything here
            ()

          case InputMode.Editing =>
            // Make the cursor visible and ask tui-rs to put it at the specified coordinates after rendering
            f.setCursor(
              // Put cursor past the end of the input text
              x = area.x + Grapheme(app.input).width + 1,
              // Move one line down, from the border to the input line
              y = area.y + 1
            )
        }
      },
      Constraint.Min(5) -> ListWidget(
        state = ListWidget.State(),
        items = items,
        block = Some(BlockWidget(borders = Borders.ALL, title = Some(Spans.nostyle("Messages"))))
      )
    ).render(f.size, f.buffer)
  }
}
