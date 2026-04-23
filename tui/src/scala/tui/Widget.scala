package tui

/** Base requirements for a Widget
  */
trait Widget {

  /** Draws the current state of the widget in the given buffer. That is the only method required to implement a custom widget.
    */
  def render(area: Rect, buf: Buffer): Unit
}

object Widget {
  def apply(f: (Rect, Buffer) => Unit): Widget = f(_, _)
  object Empty extends Widget {
    def render(area: Rect, buf: Buffer): Unit = ()
  }
}
