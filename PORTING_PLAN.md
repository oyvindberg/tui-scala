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
3. **Apply** the equivalent changes in Java to `cassowary/`, `tui/`, `demo/`.
4. **Update tests** if APIs changed.
5. **Run tests**: `BLEEP_VERSION=0.0.13 bleep test tests` — must be green.
6. **Commit** with subject `Port ratatui <R>` and body summarising notable changes.

**Hard rules:**
- No commit if tests aren't green. Either fix the port or revert the submodule bump.
- One commit per release tag.
- No "drive-by" cleanup commits between release commits — keep the history release-aligned.

## Releases (16 total, ~1,028 human code-touching commits)

### Phase 1 — pre-API-revamp (~267 commits)

| # | Release | Date | Commits | Crossterm | Status |
|---|---|---|---:|---|---|
| 1 | `v0.20.0` | 2023-03-19 | 16 | **0.25 → 0.26** | pending |
| 2 | `v0.20.1` | 2023-03-22 | 3 | — | pending |
| 3 | `v0.21.0` | 2023-05-29 | 33 | — | pending |
| 4 | `v0.22.0` | 2023-07-17 | 40 | — | pending |
| 5 | `v0.23.0` | 2023-08-28 | 62 | **0.26 → 0.27** | pending |
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
