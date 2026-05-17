> [!NOTE]
> **jatatui** is a Java port of [ratatui](https://github.com/ratatui/ratatui) v0.30.0.
> It is the successor to `tui-scala`, which ported the much older 2022-era `tui-rs`.
> The original `tui-scala` code lives on the
> [`main-tui-scala`](https://github.com/oyvindberg/jatatui/tree/main-tui-scala)
> branch of this repo (with the further Java-rewrite + v0.20–v0.25 ports preserved
> on [`legacy-port`](https://github.com/oyvindberg/jatatui/tree/legacy-port)).

# jatatui

[![Build Status](https://github.com/oyvindberg/jatatui/actions/workflows/build.yml/badge.svg?branch=main&event=push)](https://github.com/oyvindberg/jatatui/actions/workflows/build.yml?query=branch%3Amain)

https://github.com/user-attachments/assets/9da930e4-e01f-4307-b519-d0581a10fefa

Build rich terminal UIs from Java. jatatui is a faithful port of ratatui — same
widgets, same buffer model, same Crossterm backend — translated to idiomatic
modern Java (records, sealed interfaces, switch expressions). Plus an optional
React-style component layer on top.

## Modules

| artifact | depends on | what it is |
|---|---|---|
| `crossterm` | — | JNI binding to the Rust [crossterm](https://github.com/crossterm-rs/crossterm) crate. Native libs ship for Linux x86_64/aarch64, macOS x86_64/arm64, Windows x86_64. |
| `jatatui-core` | — | Buffer, Cell, Style, Color, Modifier, Layout, Constraint, Rect, Frame, Terminal, Text/Line/Span. Port of `ratatui-core`. |
| `jatatui-widgets` | `jatatui-core` | Block, Paragraph, List, Table, Tabs, BarChart, Chart, Calendar, Canvas, Gauge, Sparkline, Scrollbar, Clear, Borders, JatatuiLogo, JatatuiMascot, TextInput. Port of `ratatui-widgets`. |
| `jatatui-crossterm` | `jatatui-core`, `crossterm` | `CrosstermBackend` and the conversion glue (Color/Style/Modifier ↔ JNI). Port of `ratatui-crossterm`. |
| `jatatui-react` | `jatatui-core`, `jatatui-widgets`, `jatatui-crossterm` | Optional React-style layer: components-as-functions, hooks, event bubbling, focus management, portals, `ReactApp` runner + embeddable `Renderer`. |
| `jatatui-components` | `jatatui-react` | Higher-level components built on the React layer: text input, list/table/gauges, modal, dropdown, picker, selectable list, button, screen frame, confirm dialog, link, scrollable, toasts, forms, router, theme. |

The non-React stack (`crossterm` + `jatatui-core` + `jatatui-widgets` +
`jatatui-crossterm`) is a complete library on its own. The React layer is
opt-in.

## Install

Coordinates: `com.olvind.jatatui`.

Maven:
```xml
<dependency>
  <groupId>com.olvind.jatatui</groupId>
  <artifactId>jatatui-core</artifactId>
  <version>0.30.0</version>
</dependency>
<dependency>
  <groupId>com.olvind.jatatui</groupId>
  <artifactId>jatatui-widgets</artifactId>
  <version>0.30.0</version>
</dependency>
<dependency>
  <groupId>com.olvind.jatatui</groupId>
  <artifactId>jatatui-crossterm</artifactId>
  <version>0.30.0</version>
</dependency>
```

Gradle:
```kotlin
implementation("com.olvind.jatatui:jatatui-core:0.30.0")
implementation("com.olvind.jatatui:jatatui-widgets:0.30.0")
implementation("com.olvind.jatatui:jatatui-crossterm:0.30.0")
```

The version tracks the upstream ratatui release jatatui ports from. JVM target
is **Java 21**.

## Quick start

```java
import jatatui.core.style.*;
import jatatui.core.text.Line;
import jatatui.core.widgets.Widget;
import jatatui.crossterm.Jatatui;
import jatatui.widgets.Borders;
import jatatui.widgets.block.Block;
import jatatui.widgets.paragraph.Paragraph;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Event;
import tui.crossterm.KeyCode;

public class Hello {
  public static void main(String[] args) throws java.io.IOException {
    var ct = new CrosstermJni();
    Jatatui.runIo(terminal -> {
      while (true) {
        terminal.draw(frame -> {
          Block block = Block.empty()
              .withTitle(Line.from(" Hello "))
              .withBorders(Borders.ALL)
              .withBorderStyle(Style.empty().withFg(new Color.Yellow()));
          Widget content = Paragraph.of("Press q to quit.")
              .withStyle(Style.empty().withFg(new Color.White()));
          block.render(frame.area(), frame.bufferMut());
          content.render(block.inner(frame.area()), frame.bufferMut());
        });
        Event event = ct.read();
        if (event instanceof Event.Key key
            && key.keyEvent().code() instanceof KeyCode.Char c
            && c.c() == 'q') {
          return;
        }
      }
    });
  }
}
```

For something more substantive, see the demos in `jatatui-demo/` (porting of
the upstream `examples/apps/*`) and the React-style demos in
`jatatui-demo-react/`.

## React-style layer (optional)

> [!WARNING]
> **`jatatui-react` and `jatatui-components` are proof-of-concept.** They've
> been used to build a real app, and the core ideas are working, but sweeping
> API changes are likely as the layer matures. Pin a specific snapshot if
> you're depending on it for production code, and expect to refactor on each
> minor version bump.

`jatatui-react` and `jatatui-components` add a React-shaped API: components as
pure functions, hooks for state, focus management, event bubbling, portals for
overlays. Same buffer underneath — just different ergonomics.

```java
import static jatatui.react.Components.*;

import jatatui.core.style.Color;
import jatatui.core.style.Style;
import jatatui.react.ReactApp;
import jatatui.widgets.Borders;
import java.util.Optional;
import tui.crossterm.KeyCode;

public class Counter {
  public static void main(String[] args) throws java.io.IOException {
    ReactApp.run(component(ctx -> {
      var count = ctx.useState(() -> 0);
      boolean focused = ctx.useFocus(Optional.of("counter"), true);

      if (focused) {
        ctx.onKey(new KeyCode.Up(),   () -> count.update(n -> n + 1));
        ctx.onKey(new KeyCode.Down(), () -> count.update(n -> n - 1));
      }

      return box(
          focused ? " Counter * " : " Counter ",
          Borders.ALL,
          text(
              "Count: " + count.get(),
              Style.empty().withFg(focused ? Color.YELLOW : Color.CYAN)),
          row(
              button("[ + ]", Style.empty().withFg(Color.GREEN), () -> count.update(n -> n + 1)),
              button("[ - ]", Style.empty().withFg(Color.RED),   () -> count.update(n -> n - 1))),
          text(
              "(↑/↓ when focused, click ± buttons, Ctrl+C to quit)",
              Style.empty().withFg(Color.GRAY)));
    }));
  }
}
```

### What `jatatui-react` provides

- **Hooks**: `useState`, `useRef`, `useEffect`, `useFocus`, `useContext`,
  `useTimeout`. Fiber-keyed semantics — order matters, deps arrays gate
  re-runs.
- **Imperative focus**: `ctx.focus(id)` and `ctx.blur()` for cross-component
  focus moves (e.g. opening a modal). `autoFocus=true` on `useFocus`
  eager-claims on mount so newly-mounted screens get a focused element
  same-frame.
- **Reconciliation**: when the component type at a given fiber slot changes
  (e.g. router screen swap), hook state under that slot is dropped — no
  state-bleed across screens.
- **Events**: `onClick` / `onKey` / `onScroll` are area-scoped and bubble
  through the focused-element chain; `stopPropagation()` halts them.
  `onGlobalKey` is window-level. `ANY_KEY` / `ANY_CHAR` predicates match by
  shape rather than equality.
- **Portals**: `portal(area, child)` renders into an arbitrary rect outside
  the parent layout — the mechanism behind modals, dropdowns, tooltips. Pairs
  with the `Clear` widget for opaque overlays.
- **Layout primitives**: `column`, `row`, `box`, `stack`; per-child
  `length(n, ...)` / `fill(weight, ...)` / `min` / `max` / `percent` / `ratio`
  constraints.
- **Built-ins**: `text`, `paragraph`, `button`, `tabs`, `forEach`, `when` /
  `ifElse`, `memo`, `pureComponent`. The `widget(...)` escape hatch wraps any
  `jatatui-widgets` widget into the tree.
- **Two entry points**:
  - `ReactApp.run(element)` — turnkey: terminal init, event loop, repaint
    on dirty. Best for whole-app jatatui-react programs.
  - `Renderer` — embeddable engine. Call `renderer.render(frame, element)`
    inside your own draw loop and pump events with `dispatchKey` /
    `dispatchMouse`. Best when integrating into an existing terminal app or a
    test harness (`TestHarness` is built on it).

### Higher-level components (`jatatui-components`)

- **Input**: `textInput`, `titledTextInput`, `dropdown`, `picker` (search +
  ranked list), `selectableList` (heterogeneous rows with double-click
  activation).
- **Chrome**: `button`, `backButton`, `screenFrame`, `link`, `confirmDialog`,
  `modal`, `scrollable`.
- **Data**: `list`, `table`, `gauge`, `lineGauge`, `barchart`, `sparkline`,
  `scrollbar`.
- **Context providers**: `toastsProvider` + `useToasts`, `formProvider` +
  `useForm` + `useField`, `router` + `useRouter`, `themeProvider` +
  `Theme.useTheme`.
- **Search**: `FuzzyMatch` — IntelliJ-style scoring with word-boundary bonuses
  for ranking lists/pickers.

`Theme` is a `useContext`-style provider; components don't currently
auto-consume it — read the active theme with `Theme.useTheme()` and apply
styles explicitly where you want them.

Design notes: [jatatui-react/DESIGN.md](jatatui-react/DESIGN.md).

## Build

bleep is the build tool. The version is pinned in `bleep.yaml` (`$version:
1.0.0-M9`); the bleep launcher picks it up automatically.

```bash
cd jatatui
bleep compile
bleep test jatatui-tests
bleep run jatatui-demo -- demo2
bleep run jatatui-demo-react -- counter
```

## Local snapshot publish

For consuming jatatui from other local projects without waiting for a release:

```bash
cd jatatui
bleep publish local-ivy
```

The version is derived from `git describe` via dynver — something like
`0.30.0+14-shaabcdef`. This publishes the six publishable modules (`crossterm`,
`jatatui-core`, `jatatui-widgets`, `jatatui-crossterm`, `jatatui-react`,
`jatatui-components`) to the local Ivy repo (`~/.ivy2/local/`). Pin the same
identifier in the downstream build; re-publishing after new commits produces a
fresh identifier — handy for picking up changes without a coordinate dance.

```kotlin
// in another project's build
implementation("com.olvind.jatatui:jatatui-widgets:0.30.0+14-shaabcdef")
```

## Release

Releases are tagged `vX.Y.Z` and published to Maven Central by the
[build workflow](.github/workflows/build.yml). The same tag doubles as the
dynver base for in-between snapshot publishes. Required secrets in the
repository: `PGP_SECRET`, `PGP_PASSPHRASE`, `SONATYPE_USERNAME`,
`SONATYPE_PASSWORD` (see
[bleep's publish setup](https://oyvindberg.github.io/bleep/) or
[sbt-ci-release docs](https://github.com/sbt/sbt-ci-release#sonatype) for the
GPG / Sonatype workflow — bleep's publish reuses the same env vars).

To cut a release: tag the head commit, push the tag.
```bash
git tag v0.30.0
git push origin v0.30.0
```
The workflow runs `bleep publish sonatype --version 0.30.0 --assert-release`,
uploads native libraries from all 5 platforms, and creates a GitHub Release.

## License

MIT (same as ratatui). See [LICENSE](LICENSE).
