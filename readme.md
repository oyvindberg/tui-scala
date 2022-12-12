# tui-scala

[![Build Status](https://github.com/oyvindberg/tui-scala/workflows/CI/badge.svg)](https://github.com/oyvindberg/tui-scala/actions?query=workflow%3ACI+)

https://user-images.githubusercontent.com/247937/206300852-9afab78b-5705-4241-bef1-f841bf5d42cc.mp4

`tui-scala` is a [Scala](https://www.scala-lang.org) library to build rich terminal
user interfaces and dashboards. It is a port of [tui-rs](https://github.com/fdehau/tui-rs), 
which is heavily inspired by the `Javascript`
library [blessed-contrib](https://github.com/yaronn/blessed-contrib) and the
`Go` library [termui](https://github.com/gizak/termui).

**The port is not yet complete, see [roadmap](https://github.com/oyvindberg/tui-scala/issues/15) for immediate plans.
There are [bite-sized](https://github.com/oyvindberg/tui-scala/issues?q=is%3Aissue+is%3Aopen+label%3A%22good+first+issue%22) 
tasks to complete if you want to contribute!**

The library uses [crossterm](https://github.com/crossterm-rs/crossterm) as a backend. 
`crossterm` handles differences between platforms, so everything should work on major operating systems, including windows.

The integration with `crossterm` is published separately as a Java artifact, which calls native rust code through JNI.
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

The library comes with a bunch of widgets: here is some example code:

* [BarChart](https://github.com/oyvindberg/tui-rs/blob/master/examples/barchart.scala)
* [Block](https://github.com/oyvindberg/tui-rs/blob/master/examples/block.scala) (not ported yet)
* [Canvas (with line, point cloud, world map)](https://github.com/oyvindberg/tui-rs/blob/master/examples/canvas.scala)
* [Chart](https://github.com/oyvindberg/tui-rs/blob/master/examples/chart.scala)
* [Gauge](https://github.com/oyvindberg/tui-rs/blob/master/examples/gauge.scala)
* [Layout](https://github.com/oyvindberg/tui-rs/blob/master/examples/layout.scala)
* [List](https://github.com/oyvindberg/tui-rs/blob/master/examples/list.scala)
* [Paragraph](https://github.com/oyvindberg/tui-rs/blob/master/examples/paragraph.scala)
* [Popup](https://github.com/oyvindberg/tui-rs/blob/master/examples/popup.scala)
* [Sparkline](https://github.com/oyvindberg/tui-rs/blob/master/examples/sparkline.scala)
* [Table](https://github.com/oyvindberg/tui-rs/blob/master/examples/table.scala)
* [Tabs](https://github.com/oyvindberg/tui-rs/blob/master/examples/tabs.scala)
* [User input](https://github.com/oyvindberg/tui-rs/blob/master/examples/user_input.scala)

Click on each item to see the source of the example. Run the examples with
bleep (e.g. to run the barchart example `bleep run demo@jvm213 barchart`), and quit by pressing `q`.

### Installation

For sbt:

```scala
  libraryDependencies += "com.olvind.tui" %% "tui" % "<version>"
```

If you only want `crossterm` to do low-level things, or if you want to experiment with making a TUI, these are the coordinates:
```scala
  libraryDependencies += "com.olvind.tui" % "crossterm" % "<version>"
```


And then copy/paste one of the demos above to get started.

It's cross published for scala 2.13 and 3. Note that scala 3 won't work with graalvm native image until 3.3. 

You'll need a recent JVM with support for sealed interfaces and records. likely 18. 

### Contributing/building

See [contributing](./contributing.md)
