package jatatui.core.buffer;

/// A single coordinate-and-cell update produced by [Buffer#diff(Buffer)].
///
/// Mirrors the `(u16, u16, &Cell)` tuple returned by upstream `Buffer::diff` — Java has no
/// tuples, so we model it as a dedicated record (per the project's hard rule).
///
/// The `cell` is the cell instance from the **next** buffer (the second argument to `diff`).
/// Callers must not mutate it: the next buffer still owns the cell.
public record BufferUpdate(int x, int y, Cell cell) {}
