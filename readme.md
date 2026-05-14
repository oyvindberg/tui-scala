> [!NOTE]
> **jatatui** is a Java port of [ratatui](https://github.com/ratatui/ratatui) v0.30.0.
> It is the successor to [`tui-scala`](https://github.com/oyvindberg/tui-scala),
> which ported the much older 2022-era `tui-rs`. `tui-scala` will be archived once
> jatatui is feature-complete.

# jatatui

[![Build Status](https://github.com/oyvindberg/jatatui/actions/workflows/build.yml/badge.svg?branch=main&event=push)](https://github.com/oyvindberg/jatatui/actions/workflows/build.yml?query=branch%3Amain)

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
| `jatatui-react` | `jatatui-core`, `jatatui-widgets`, `jatatui-crossterm` | Optional React-style layer: components-as-functions, hooks (`useState` / `useRef` / `useEffect` / `useFocus` / `useContext`), event bubbling with `stopPropagation`, Portal, ReactApp runner. |
| `jatatui-components` | `jatatui-react` | Higher-level components built on the React layer: List, Table, Gauge, BarChart, Sparkline, Scrollbar, TextInput, Modal, Dropdown, Toast, Form, Router, Theme. |

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

public class Hello {
  public static void main(String[] args) throws java.io.IOException {
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
        // (your event loop here — read keys via the crossterm JNI binding)
      }
    });
  }
}
```

For something more substantive, see the demos in `jatatui-demo/` (porting of
the upstream `examples/apps/*`) and the React-style demos in
`jatatui-demo-react/`.

## React-style layer (optional)

`jatatui-react` and `jatatui-components` add a React-shaped API: components as
pure functions, hooks for state, event bubbling, focus management. Same buffer
underneath — just different ergonomics.

```java
import static jatatui.react.Components.*;
import static jatatui.components.Components.*;
import jatatui.react.ReactApp;
import tui.crossterm.KeyCode;

public class Counter {
  public static void main(String[] args) throws java.io.IOException {
    ReactApp.run(component(ctx -> {
      var n = ctx.useState(() -> 0);
      ctx.onGlobalKey(new KeyCode.Up(),   () -> n.update(v -> v + 1));
      ctx.onGlobalKey(new KeyCode.Down(), () -> n.update(v -> v - 1));
      return text("count = " + n.get());
    }));
  }
}
```

What's available: `useState`, `useRef`, `useEffect`, `useFocus`, `useContext`,
`memo`, `pureComponent`; `column` / `row` / `box` / `stack` layouts; `portal`
for overlays; `text` / `paragraph` / `button` / `tabs` / `forEach`; bubbling
mouse/key events with `stopPropagation`; the `widget(...)` escape hatch wraps
any `jatatui-widgets` widget into the tree.

Higher-level components (`jatatui-components`): `textInput`, `titledTextInput`,
`modal`, `dropdown`, `toastsProvider` + `useToasts`, `formProvider` + `useForm`
+ `useField`, `router` + `useRouter`, `themeProvider` + `Theme.useTheme`,
`list`, `table`, `gauge`, `lineGauge`, `barchart`, `sparkline`, `scrollbar`.

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
bleep publish local-ivy --version "0.30.0_$(date +%Y-%m-%d)-SNAPSHOT"
```

This publishes the six publishable modules (`crossterm`, `jatatui-core`,
`jatatui-widgets`, `jatatui-crossterm`, `jatatui-react`, `jatatui-components`)
to the local Ivy repo (`~/.ivy2/local/`). The version is stable for the day
(consumers can pin it), and re-publishing the next day produces a fresh
identifier — handy for picking up changes without a coordinate dance.

```kotlin
// in another project's build
implementation("com.olvind.jatatui:jatatui-widgets:0.30.0_2026-05-14-SNAPSHOT")
```

## Release

Releases are tagged `jatatui-vX.Y.Z` and published to Maven Central by the
[build workflow](.github/workflows/build.yml). Required
secrets in the repository: `PGP_SECRET`, `PGP_PASSPHRASE`, `SONATYPE_USERNAME`,
`SONATYPE_PASSWORD` (see
[bleep's publish setup](https://oyvindberg.github.io/bleep/) or
[sbt-ci-release docs](https://github.com/sbt/sbt-ci-release#sonatype) for the
GPG / Sonatype workflow — bleep's publish reuses the same env vars).

To cut a release: tag the head commit, push the tag.
```bash
git tag jatatui-v0.30.0
git push origin jatatui-v0.30.0
```
The workflow runs `bleep publish sonatype --version 0.30.0 --assert-release`,
uploads native libraries from all 5 platforms, and creates a GitHub Release.

## Status

Port progresses per upstream source file. See [PORTING_PLAN.md](PORTING_PLAN.md)
for the live status board and [CLAUDE.md](CLAUDE.md) for the porting workflow
and translation conventions.

## License

MIT (same as ratatui). See [LICENSE](LICENSE).
