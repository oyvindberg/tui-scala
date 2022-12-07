# tui-scala

[![Build Status](https://github.com/oyvindberg/tui-scala/workflows/CI/badge.svg)](https://github.com/oyvindberg/tui-scala/actions?query=workflow%3ACI+)

https://user-images.githubusercontent.com/247937/206300852-9afab78b-5705-4241-bef1-f841bf5d42cc.mp4

`tui-scala` is a [Scala](https://www.scala-lang.org) library to build rich terminal
user interfaces and dashboards. It is a port of [tui-rs](https://github.com/fdehau/tui-rs), 
which is heavily inspired by the `Javascript`
library [blessed-contrib](https://github.com/yaronn/blessed-contrib) and the
`Go` library [termui](https://github.com/gizak/termui).

**The port is not yet complete, see [roadmap](https://github.com/oyvindberg/tui-scala/issues/15) for immediate plans.
There are bite-sized tasks to complete if you want to contribute!**

The library supports the [crossterm](https://github.com/crossterm-rs/crossterm) backend, the integration with which
is published separately as a Java artifact which calls native rust code through JNI.
This integration works both when running on the JVM and when running as GraalVM native image.

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

* ~~[Block](https://github.com/oyvindberg/tui-rs/blob/master/examples/block.scala)~~ (not ported yet)
* ~~[Gauge](https://github.com/oyvindberg/tui-rs/blob/master/examples/gauge.scala)~~ (not ported yet)
* [Sparkline](https://github.com/oyvindberg/tui-rs/blob/master/examples/sparkline.scala)
* ~~[Chart](https://github.com/oyvindberg/tui-rs/blob/master/examples/chart.scala)~~ (not ported yet)
* [BarChart](https://github.com/oyvindberg/tui-rs/blob/master/examples/barchart.scala)
* [List](https://github.com/oyvindberg/tui-rs/blob/master/examples/list.scala)
* [Table](https://github.com/oyvindberg/tui-rs/blob/master/examples/table.scala)
* [Paragraph](https://github.com/oyvindberg/tui-rs/blob/master/examples/paragraph.scala)
* ~~[Canvas (with line, point cloud, map)](https://github.com/oyvindberg/tui-rs/blob/master/examples/canvas.scala)~~ (not ported yet)
* [Tabs](https://github.com/oyvindberg/tui-rs/blob/master/examples/tabs.scala)

Click on each item to see the source of the example. Run the examples with
bleep (e.g. to run the barchart example `bleep run demo@jvm213 barchart`), and quit by pressing `q`.

## Building

(tui-scala helps dog-food the experimental [bleep](https://bleep.build/docs/) Scala build tool as it's gearing up for first public release. keep an open mind!)

- `git clone https://github.com/oyvindberg/tui-scala`
- [install bleep](https://bleep.build/docs/installing/)
- (if you use bash, run `bleep install-tab-completions-bash` and start a new shell to get tab completions)
- `git submodule init && git submodule update`
- `bleep gen-jni-library` to generate JNI bindings for `crossterm` (needed to run)
- `bleep setup-ide jvm213` to enable IDE import (metals or intellij)
-  open in your IDE
- `bleep run demo@jvm213 <demoname>` to run demos
- `bleep gen-native-image` if you want to see how fast things get with native image compilation
