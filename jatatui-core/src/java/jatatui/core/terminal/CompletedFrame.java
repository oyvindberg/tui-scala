package jatatui.core.terminal;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Rect;

/// `CompletedFrame` represents the state of the terminal after all changes performed in the last
/// [Terminal#draw(java.util.function.Consumer)] call have been applied. Therefore, it is only valid
/// until the next call to [Terminal#draw(java.util.function.Consumer)].
///
/// Mirrors `ratatui_core::terminal::CompletedFrame` (v0.30). Upstream uses a borrowed reference to
/// the buffer; in Java the buffer is a plain reference — callers must not mutate it.
public record CompletedFrame(Buffer buffer, Rect area, int count) {}
