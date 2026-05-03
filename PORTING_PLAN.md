# Plan: bring tui-scala up to ratatui's latest

This file is the source of truth for the porting effort. Update it after every release is shipped (move the row from "Pending" to "Done").

## Repo state

The Scala port was originally based on **fdehau/tui-rs commit `fafad6c96`** (one commit past `v0.19.0`, 2022-08-14). After that, fdehau abandoned the repo and the community continued work as **ratatui**, which forked at the same point.

## Submodules

Three Rust submodules track the upstream sources we port from. Bump in lockstep with the equivalent dependency change in ratatui:

| Submodule | Upstream | Bump policy |
|---|---|---|
| `submodules/ratatui` | https://github.com/ratatui/ratatui | Bumped on every release we port. |
| `submodules/cassowary` | https://github.com/dylanede/cassowary-rs | Stays at `9523a8f` (cassowary 0.3.0). Ratatui has stayed on `0.3` from day one. Bump only if a future ratatui release moves off it. |
| `submodules/crossterm` | https://github.com/crossterm-rs/crossterm | Bumped 4 times across the porting timeline (see table below). |

## Per-release porting ritual

For each release `<R>`:

1. **Bump submodules**:
   - `cd submodules/ratatui && git fetch --tags && git checkout <R>`
   - If the row says crossterm bumps: `cd submodules/crossterm && git checkout <new-version>`
2. **Read** `submodules/ratatui/CHANGELOG.md` between previous and current tag.
3. **Apply** the equivalent code changes in Java to `cassowary/`, `tui/`, `demo/`.
4. **Port tests** added in this release:
   - **Inline `#[cfg(test)] mod tests` blocks** at the bottom of source files → port to our scalatest suites. These typically test internal/public behavior.
   - **Integration tests under `tests/widgets_*.rs`** → port to `tests/src/scala/tui/widgets/*Tests.scala`.
   - **Skip doctests** (`/// ``` ` examples in comments) — Rust-specific executable doc snippets with no Java equivalent.
   - For tests that exercise features we deferred (e.g. calendar, `Masked`, inline viewport): defer the tests too. Track in this file.
5. **Run tests**: `BLEEP_VERSION=0.0.13 bleep test tests` — must be green.
6. **Commit** with subject `Port ratatui <R>` and body summarising notable changes (and any deferrals).

**Hard rules:**
- No commit if tests aren't green. Either fix the port or revert the submodule bump.
- One commit per release tag.
- No "drive-by" cleanup commits between release commits — keep the history release-aligned.
- Any feature or test that's deferred from a release must be listed under "Deferred work" below so we can come back to it.

## Releases (16 total, ~1,028 human code-touching commits)

### Phase 1 — pre-API-revamp (~267 commits)

| # | Release | Date | Commits | Crossterm | Status |
|---|---|---|---:|---|---|
| 1 | `v0.20.0` | 2023-03-19 | 16 | **0.25 → 0.26** | **done** |
| 2 | `v0.20.1` | 2023-03-22 | 3 | — | **done** |
| 3 | `v0.21.0` | 2023-05-29 | 33 | — | **done** |
| 4 | `v0.22.0` | 2023-07-17 | 40 | — | **done** |
| 5 | `v0.23.0` | 2023-08-28 | 62 | **0.26 → 0.27** | **done** |
| 6 | `v0.24.0` | 2023-10-23 | 63 | — | pending |
| 7 | `v0.25.0` | 2023-12-18 | 50 | — | pending |

### Phase 2 — major API revamp (126 commits)

| # | Release | Date | Commits | Crossterm | Status |
|---|---|---|---:|---|---|
| 8 | `v0.26.0` | 2024-02-02 | 126 | — | pending |

### Phase 3 — refinement (~330 commits)

| # | Release | Date | Commits | Crossterm | Status |
|---|---|---|---:|---|---|
| 9 | `v0.26.1` | 2024-02-12 | 16 | — | pending |
| 10 | `v0.26.2` | 2024-04-15 | 65 | — | pending |
| 11 | `v0.26.3` | 2024-05-20 | 38 | — | pending |
| 12 | `v0.27.0` | 2024-06-24 | 45 | — | pending |
| 13 | `v0.28.0` | 2024-08-07 | 57 | **0.27 → 0.28.1** | pending |
| 14 | `v0.28.1` | 2024-08-25 | 15 | — | pending |
| 15 | `v0.29.0` | 2024-10-21 | 41 | — | pending |

### Phase 4 — workspace split (310 commits)

| # | Release | Date | Commits | Crossterm | Status |
|---|---|---|---:|---|---|
| 16 | `ratatui-v0.30.0` | 2025-12-26 | 310 | **0.28 → 0.29** | pending |

After this release, ratatui is split into `ratatui-core`, `ratatui-widgets`, `ratatui-crossterm`, `ratatui-macros`, `ratatui-termwiz`, `ratatui-termion`. The Java module layout may need to mirror this.

## Test invariants

- `BLEEP_VERSION=0.0.13 bleep test tests` must pass on every commit.
- 2 pre-existing ignored tests (`cassowary.RemovalTest.remove_constraint`, `widgets.TableTests.widgets_table_should_clamp_offset_if_rows_are_removed`) are tracked and may be un-ignored as fixes from upstream are ported.

## Known carry-overs from the original Scala port

These were preserved verbatim during the Java rewrite. Watch for upstream fixes:

- `Layout.split` swaps Horizontal↔Vertical width/height in the expand-to-fill last-rect adjustment. Cosmetic — the cassowary solver already pins the last edge correctly when `expandToFill = true`.

## Deferred work

Features or tests that were skipped in a release commit and need to be revisited. When porting later, search this file by ratatui release tag.

| First seen in | What | Why deferred |
|---|---|---|
| v0.21.0 | `BlockWidget` padding (#20) | Additive feature; no demo currently uses it. Will batch with other Block work. |
| v0.21.0 | `BlockWidget` title on bottom (#36) | Additive feature; will batch with other Block work. |
| v0.21.0 | Sparkline directions (#134) | Additive; defer until needed. |
| v0.21.0 | Calendar widget (#138) | New widget; not used by demo. |
| v0.21.0 | Circle canvas shape (#159) | New canvas Shape; not used by demo. |
| v0.21.0 | `Masked` text (#168) | Additive text type. |
| v0.21.0 | Inline viewport (#114) | BREAKING terminal change; needs careful porting. |
| v0.21.0 | `Spans` → `Line` rename (#178) | Touches every widget and test; do as a dedicated commit. |
| v0.21.0 | Termwiz backend (#5) | Rust-only backend. |
| v0.21.0 | `border!` macro (#11) | Rust macro — not portable. Equivalent: `Borders.LEFT.or(Borders.TOP)`. |
| v0.22.0 | Block multi-title (#232) | Restructures BlockWidget; defer until needed. |
| v0.22.0 | Stylize trait + shorthands (#283, #289) | Rust trait pattern; in Java just chain `.withFg(...).withBg(...)`. Skip unless useful. |
| v0.22.0 | Scrollbar widget (#228) | New widget. |
| v0.22.0 | Underline color (#308) | Needs Modifier extension; defer. |
| v0.22.0 | Barchart groups (#288), custom bar text (#309) | Restructures BarChartWidget. |
| v0.22.0 | Prelude (#304) | Rust-only. |
| v0.22.0 | Reflow simplification (#290) | Internal Rust refactor; behavior preserved. |
| v0.22.0 | bitflags 2.3 upgrade (#205) | Rust dep — N/A. |
| v0.23.0 | BarChart direction (#325) — horizontal bars | Additive feature. |
| v0.23.0 | List highlight_spacing (#394) | Additive feature. |
| v0.23.0 | Table highlight_spacing (#375) | Additive feature. |
| v0.23.0 | Table line alignment (#392) | Additive rendering option. |
| v0.23.0 | Layout big rewrite (#411 #405 #51950994 #de25de0a) | Ratatui replaced its layout impl. Our cassowary-based port already handles the bug cases (gaps, left<=right) via REQUIRED constraints. Port the new impl only if we want to abandon cassowary, which we shouldn't. |
| v0.23.0 | Weak constraints for similar-size rects (#395) | Defer until layout itself is revisited. |
| v0.23.0 | Block title_style fix (#349) | Part of multi-title; we deferred multi-title. |
| v0.23.0 | Common traits Clone/Copy/Debug/Default | Java equivalents already in place via records. |
