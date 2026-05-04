# jatatui — porting plan

This is the active status board. See `CLAUDE.md` for the durable playbook.

## Strategy

Java port of [ratatui](https://github.com/ratatui/ratatui) at the v0.30.0 release. Read upstream as the target and translate it source-file-by-source-file. Each commit ports one (or a small group of) `.rs` source files together with their inline `#[cfg(test)] mod tests`.

## Phases

| # | Phase | Status |
|---|---|---|
| 0 | Initialize repo: copy infra from tui-scala, add submodules, write bleep config | done |
| 1 | Bump crossterm JNI to 0.29; bump bleep to 1.0.0-M9 (JUnit 5 works from this version) | done |
| 2 | Port kasuari (cassowary fork) to `jatatui.core.layout.solver` | done |
| 3a | Port `ratatui-core/layout/` primitives (Direction, Alignment, Corner, Flex, Margin, Offset, Constraint, Position, Size, Rect) | done |
| 3b | Port `ratatui-core/style/` (Color, Modifier, Style, Stylize, palettes) | done |
| 3c | Port `ratatui-core/symbols/` (Bar, Block, Border, Braille, HalfBlock, Line, Marker, Merge, Pixel, Scrollbar, Shade) | done |
| 3d | Port `ratatui-core/layout/Layout`, Spacing; complete Rect (iter, ops, layout-dependent methods) | done |
| 3e | Port `ratatui-core/text/` (StyledGrapheme, Span, Line, Text, Masked) | in progress |
| 3f | Port `ratatui-core/buffer/` (Cell, Buffer, assert helpers) | pending |
| 3g | Port `ratatui-core/backend.rs` + `backend/test.rs` (Backend trait + TestBackend) | pending |
| 3h | Port `ratatui-core/terminal/` (Frame, Terminal, Viewport) | pending |
| 3i | Port `ratatui-core/widgets/` (Widget, StatefulWidget traits) | pending |
| 4 | Port `ratatui-widgets/src/*` (15+ widgets, ~27.5k LOC). Spawn ~6 parallel agents grouped by widget family. | done |
| 5 | Port `ratatui-crossterm/src/lib.rs` (the CrosstermBackend impl, ~880 LOC) | done |
| 6 | Replace `null` with `Optional`; add `Either.unit()` for Void-success; remove the prior `Either.right(null)` exception from CLAUDE.md | done |
| 7 | Port all `examples/apps/*` to `jatatui-demo` | done |
| 8 | Manually launch demo, fix any runtime issues | pending |

## Bleep projects

| Project | Upstream crate | Source root | Test root |
|---|---|---|---|
| `crossterm` | n/a (existing JNI binding) | `crossterm/src/java/tui/crossterm/` | covered via `jatatui-crossterm` tests |
| `jatatui-core` | `ratatui-core` | `jatatui-core/src/java/jatatui/core/` | `jatatui-tests/src/java/jatatui/tests/core/` |
| `jatatui-widgets` | `ratatui-widgets` | `jatatui-widgets/src/java/jatatui/widgets/` | `jatatui-tests/src/java/jatatui/tests/widgets/` |
| `jatatui-crossterm` | `ratatui-crossterm` | `jatatui-crossterm/src/java/jatatui/crossterm/` | `jatatui-tests/src/java/jatatui/tests/crossterm/` |
| `jatatui-tests` | n/a | `jatatui-tests/src/java/jatatui/tests/` | self, isTestProject |
| `jatatui-demo` | `examples/apps/*` | `jatatui-demo/src/java/jatatui/examples/` | smoke-tested |
| `scripts` | n/a (Scala 2.13 build infra) | `scripts/src/scala/tui/scripts/` | n/a |

## Submodules and version pins

| Submodule | Repo | Pin |
|---|---|---|
| `submodules/ratatui` | github.com/ratatui/ratatui | `ratatui-v0.30.0` (commit `0a2a7c0`) |
| `submodules/crossterm` | github.com/crossterm-rs/crossterm | `0.29` tag |
| `submodules/kasuari` | github.com/ratatui/kasuari | `v0.4.12` |

## Dependency mapping (Rust → Java)

Non-ratatui Rust crates that ratatui-core / ratatui-widgets pull in, and how each gets handled:

| Rust crate | Used for | Java equivalent |
|---|---|---|
| `kasuari 0.4` | Cassowary linear-constraint layout solver | **Translate to Java** as part of `jatatui-core` (its layout/solver module). Lives in `jatatui-core/src/java/jatatui/core/layout/solver/`. |
| `crossterm 0.29` | Terminal control (raw mode, mouse, resize) | Already covered by the local `crossterm/` JNI binding. `jatatui-crossterm` is the thin Java backend on top. |
| `unicode-segmentation 1.x` | Grapheme cluster iteration | `java.text.BreakIterator`. |
| `unicode-width 0.1.x` | East-Asian width / wcwidth | Port of `wcwidth.c` (the legacy `Wcwidth.java` is the same algorithm; copy and modernize). |
| `compact_str 0.9` | Small-string optimization | Plain `java.lang.String`. |
| `bitflags 2.10` | Bitflag types (Modifier, Borders, etc.) | Plain `int` bits + record/class wrapper. |
| `itertools 0.14` | Iteration combinators | `java.util.stream.*` and inline loops. |
| `thiserror` | Error derive | Java exception classes or sealed interfaces. |
| `strum` | Enum iteration / derive | Java enums have `values()` / `valueOf()` built in. |
| `hashbrown` | Faster HashMap | `java.util.HashMap` / `LinkedHashMap`. |
| `lru 0.16` | LRU cache | `LinkedHashMap` with `removeEldestEntry`, or a small custom class. Used only in the optional `layout-cache` feature. |
| `line-clipping 0.3` | Cohen-Sutherland line clipping | Translate the small algorithm directly. |
| `instability` | `#[unstable]` attribute marker | No-op in Java; we just port the methods. |
| `document-features` | Doc generation from features | No-op in Java. |
| `palette` (optional) | Color space conversions for fancy demos | Skip unless an example requires it. |
| `anstyle` (optional) | ANSI style interop | Skip. |

## Tests

**Counts at v0.30.0:** ratatui-core ~361 `#[test]`s across 34 files, ratatui-widgets ~404 across 32 files, ratatui-crossterm 1. **~766 tests total** to translate.

**Mapping rule:** ratatui's `ratatui-core/src/foo/bar.rs` with `#[cfg(test)] mod tests` becomes `jatatui-tests/src/java/jatatui/tests/core/foo/BarTest.java`. One Test class per upstream source file. Each `#[test] fn xxx()` becomes `@Test void xxx() {}`.

**External integration tests** (`<crate>/tests/*.rs`): only `ratatui-core/tests/rect.rs` exists at v0.30.0; merge those into `RectTest.java` alongside the inline tests from `ratatui-core/src/layout/rect.rs`.

**Doctests** (`/// ``` ` examples in `///` comments): not ported. Copy as `@Test` only when the doctest demonstrates behavior not already covered.

**Test framework:** JUnit 5 (`org.junit.jupiter:junit-jupiter:5.11.3`). `jatatui-tests` is the only project that depends on it.

**Test helpers** in `jatatui-tests/src/java/jatatui/tests/_support/`:
- `BufferAssertions.assertBufferEq(actual, expected)` — diff and pretty-print.
- `BufferAssertions.bufferLines(String...)` — analogue of upstream's `Buffer::with_lines(vec!["..."])`.

## Demo / examples (Phase 7)

Layout: `jatatui-demo/src/java/jatatui/examples/`, with `Launcher.java` dispatching by name to each example.

Per upstream, these are the example apps in `examples/apps/`:

```
advanced-widget-impl  async-github         calendar-explorer    canvas
chart                 color-explorer       colors-rgb           constraint-explorer
constraints           custom-widget        demo (multi-file)    demo2 (multi-file)
flex                  gauge                hello-world          hyperlink
inline                input-form           minimal              modifiers
mouse-drawing         panic                popup                release-header
scrollbar             table                todo-list            tracing
user-input            weather              widget-ref-container barchart
block                 list                 sparkline            tabs
```

`demo/` and `demo2/` are multi-file → sub-packages `jatatui.examples.demo.*` and `jatatui.examples.demo2.*`.

**Examples to skip** (with reason in commit body):
- `async-github`, `weather` — needs network/HTTP client
- `tracing` — needs Rust async + tracing crate
- `release-header` — internal CI helper for ratatui releases
- `mouse-drawing` — only if our `crossterm/` JNI binding doesn't expose mouse events

Everything else gets ported.

## Tracking

- **TaskList**: one task per upstream source file (~85 for source code, ~30+ for examples).
- **Pre-commit invariants**:
  - `bleep compile jatatui-core jatatui-widgets jatatui-crossterm` green
  - `bleep test jatatui-tests` green
  - Source under `jatatui-{core,widgets,crossterm}/` ships with corresponding tests under `jatatui-tests/` in the same commit.
- **Hard rule**: no "defer" / "later" / "follow-up" / "TODO". Each upstream item is either ported or marked **N/A** with a reason in the commit body.

## Publishing

- groupId: `com.olvind.jatatui`
- artifacts published: `crossterm`, `jatatui-core`, `jatatui-widgets`, `jatatui-crossterm`
