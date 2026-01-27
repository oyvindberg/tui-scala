package tui

/** CompletedFrame represents the state of the terminal after all changes performed in the last `Terminal.draw` call have been applied. Therefore, it is only
  * valid until the next call to `Terminal.draw`.
  */
case class CompletedFrame(
    buffer: Buffer,
    area: Rect
)
