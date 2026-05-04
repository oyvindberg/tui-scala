package jatatui.core.backend;

import jatatui.core.layout.Size;

/// The window size in characters (columns / rows) as well as pixels.
///
/// Mirrors upstream `ratatui_core::backend::WindowSize` (v0.30). The `pixels` field may not be
/// implemented by all terminals and may return `(0, 0)` — see
/// https://man7.org/linux/man-pages/man4/tty_ioctl.4.html under "Get and set window size" /
/// `TIOCGWINSZ`, where the pixel fields are commented as "unused".
///
/// Upstream names the columns/rows field `columns_rows`; per the project's tuple-naming rule we
/// keep a domain-meaningful name. Java doesn't allow snake_case as a record component, so the
/// component is `columns` (columns and rows together — same `Size` value).
public record WindowSize(Size columns, Size pixels) {}
