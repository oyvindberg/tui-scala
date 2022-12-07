# tui-scala

[![Build Status](https://github.com/oyvindberg/tui-scala/workflows/CI/badge.svg)](https://github.com/oyvindberg/tui-scala/actions?query=workflow%3ACI+)

<img src="./assets/demo.gif" alt="Demo cast under Linux Termite with Inconsolata font 12pt">

`tui-scala` is a [Scala](https://www.scala-lang.org) library to build rich terminal
user interfaces and dashboards. It is a port of [tui-rs](https://github.com/fdehau/tui-rs), 
which is heavily inspired by the `Javascript`
library [blessed-contrib](https://github.com/yaronn/blessed-contrib) and the
`Go` library [termui](https://github.com/gizak/termui).

The library supports the [crossterm](https://github.com/crossterm-rs/crossterm) backend, which
is published separately as a Java artifact which calls native rust code through JNI.
This backend works both when running on the JVM and when running as GraalVM native image.

The library is based on the principle of immediate rendering with intermediate
buffers. This means that at each new frame you should build all widgets that are
supposed to be part of the UI. While providing a great flexibility for rich and
interactive UI, this may introduce overhead for highly dynamic content. So, the
implementation try to minimize the number of ansi escapes sequences generated to
draw the updated UI. In practice, given the speed of the JVM the overhead rather
comes from the terminal emulator than the library itself.

Moreover, the library does not provide any input handling nor any event system, and
you may rely on `crossterm` achieve such features.

### Widgets

The library comes with the following list of widgets:

* [Block](https://github.com/oyvindberg/tui-rs/blob/master/examples/block.scala)
* [Gauge](https://github.com/oyvindberg/tui-rs/blob/master/examples/gauge.scala)
* [Sparkline](https://github.com/oyvindberg/tui-rs/blob/master/examples/sparkline.scala)
* [Chart](https://github.com/oyvindberg/tui-rs/blob/master/examples/chart.scala)
* [BarChart](https://github.com/oyvindberg/tui-rs/blob/master/examples/barchart.scala)
* [List](https://github.com/oyvindberg/tui-rs/blob/master/examples/list.scala)
* [Table](https://github.com/oyvindberg/tui-rs/blob/master/examples/table.scala)
* [Paragraph](https://github.com/oyvindberg/tui-rs/blob/master/examples/paragraph.scala)
* [Canvas (with line, point cloud, map)](https://github.com/oyvindberg/tui-rs/blob/master/examples/canvas.scala)
* [Tabs](https://github.com/oyvindberg/tui-rs/blob/master/examples/tabs.scala)

Click on each item to see the source of the example. Run the examples with
bleep (e.g. to run the barchart example `bleep run demo@jvm213 barchart`), and quit by pressing `q`.

## Building

- `git clone https://github.com/oyvindberg/tui-scala`
- [install bleep](https://bleep.build/docs/installing/)
- (if you use bash, run `bleep install-tab-completions-bash` and start a new shell to get tab completions)
- `git submodule init && git submodule update`
- `bleep setup-ide jvm213` to enable IDE import (metals or intellij)
-  open in your IDE
- `bleep gen-jni-library` to generate JNI bindings for `crossterm`
- `bleep gen-native-binary` if you want to see how fast things get with native image compilation
- `bleep run demo@jvm213 barchart` to run demo