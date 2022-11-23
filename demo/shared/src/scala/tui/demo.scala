package tui

import tui.crossterm._

import java.util.Optional

object demo {
  def main(args: Array[String]): Unit = {
    val jni = new CrosstermJni();
    println("Hola")
    println(jni.terminalSize())
    println(jni.poll(new Duration(secs = 1, nanos = 0)))
    jni.enqueueTerminalClear(ClearType.All)
    val opt = Optional.of[Color](new Color.Rgb(1, 2, 3))
    jni.enqueueStyleSetColors(opt, Optional.empty());
    jni.enqueueStyleSetAttributes(new Attributes(java.util.List.of(Attribute.Bold, Attribute.Dim)));
    jni.enqueueEventEnableFocusChange()
    jni.enqueueEventEnableMouseCapture()
    jni.enqueueEventEnableBracketedPaste()
    jni.enqueueEventPushKeyboardEnhancementFlags(new KeyboardEnhancementFlags(KeyboardEnhancementFlags.REPORT_EVENT_TYPES))
    println(new Color.Rgb(1, 2, 3))
    jni.enableRawMode()

    var continue = true
    while (continue) {
      val e = jni.read()
      jni.enqueueCursorMoveToColumn(0)
      jni.enqueueStylePrint("event: " + e + "\n")
      jni.flush()
      e match {
        case x: Event.Key =>
          x.keyEvent.code match {
            case c: KeyCode.Char if c.c() == 'q' => continue = false
            case _                               => ()
          }
        case _ => ()
      }
    }
    var i = 0
    while (i < 20) {
      val color = if (i % 2 == 0) new crossterm.Color.Red() else new crossterm.Color.Yellow()
      jni.enqueueCursorMoveToColumn(0)
      jni.enqueueStyleSetColors(Optional.of(color), Optional.empty())
      jni.enqueueStylePrint("flaff: " + i + "\n")
      jni.enqueueCursorMoveLeft(1)
      jni.flush()
      //      Thread.sleep(50L)
      i += 1
    }
    jni.enqueueStyleResetColor()
//    jni.enqueueTerminalClear(ClearType.Purge)
//    jni.enqueueTerminalClear(ClearType.All)
    jni.enqueueStylePrint("done: " + i + "\n")
    jni.disableRawMode()
    jni.enqueueEventDisableFocusChange()
    jni.enqueueEventDisableMouseCapture()
    jni.enqueueEventDisableBracketedPaste()
    jni.enqueueEventPopKeyboardEnhancementFlags()
    jni.flush()

//    jni.enqueueStyleSetStyle(
//      Optional.of(new Color.AnsiValue(1)),
//      Optional.of(new Color.AnsiValue(81)),
//      Optional.empty,
//      new Attributes(java.util.List.of(Attribute.Bold, Attribute.CrossedOut))
//    )
  }
}
