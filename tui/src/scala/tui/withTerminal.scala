package tui

import tui.crossterm.{Command, CrosstermJni}

object withTerminal {
  def apply[T](f: (CrosstermJni, Terminal) => T): T = {
    val jni = new CrosstermJni
    // setup terminal
    jni.enableRawMode()
    jni.execute(new Command.EnterAlternateScreen(), new Command.EnableMouseCapture())

    val backend = new CrosstermBackend(jni)

    val terminal = Terminal.init(backend)

    try f(jni, terminal)
    finally {
      // restore terminal
      jni.disableRawMode()
      jni.execute(new Command.LeaveAlternateScreen(), new Command.DisableMouseCapture())
      backend.showCursor()
    }
  }
}
