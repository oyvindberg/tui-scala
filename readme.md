# tui-scala

[![Build Status](https://github.com/oyvindberg/tui-scala/actions/workflows/build.yml/badge.svg)](https://github.com/oyvindberg/tui-scala/actions/workflows/build.yml)



https://user-images.githubusercontent.com/247937/207265695-58d2eeac-2f62-4264-95f9-9e25b1f99964.mp4


`tui-scala` is a [Scala](https://www.scala-lang.org) library to build rich terminal
user interfaces and dashboards. It is a port of [tui-rs](https://github.com/fdehau/tui-rs), 
which is heavily inspired by the `Javascript`
library [blessed-contrib](https://github.com/yaronn/blessed-contrib) and the
`Go` library [termui](https://github.com/gizak/termui).

**The port is now complete, and from here on it will diverge from the original design. See [roadmap](https://github.com/oyvindberg/tui-scala/issues/15) for immediate plans.
There are some design/ideas tasks where you can help with ideas, POCs and implementation if you want to contribute!**

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

The library comes with a bunch of widgets: here is example code for all of them:

* [BarChart](demo/src/scala/tuiexamples/BarChartExample.scala)
* [Block](demo/src/scala/tuiexamples/BlockExample.scala)
* [Canvas](demo/src/scala/tuiexamples/CanvasExample.scala)
* [Chart](demo/src/scala/tuiexamples/ChartExample.scala)
* [CustomWidget](demo/src/scala/tuiexamples/CustomWidgetExample.scala)
* [Gauge](demo/src/scala/tuiexamples/GaugeExample.scala)
* [Layout](demo/src/scala/tuiexamples/LayoutExample.scala)
* [List](demo/src/scala/tuiexamples/ListExample.scala)
* [Paragraph](demo/src/scala/tuiexamples/ParagraphExample.scala)
* [Popup](demo/src/scala/tuiexamples/PopupExample.scala)
* [Sparkline](demo/src/scala/tuiexamples/SparklineExample.scala)
* [Table](demo/src/scala/tuiexamples/TableExample.scala)
* [Tabs](demo/src/scala/tuiexamples/TabsExample.scala)
* [UserInput](demo/src/scala/tuiexamples/UserInputExample.scala)

Click on each item to see the source of the example. Run the examples with
bleep (`bleep run demo@jvm213`), and quit by pressing `q`.

The demo shown in the first video can be found here:
* [Demo](demo/src/scala/tuiexamples/demo)

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
