package tui.scripts

import bleep._
import bleep.plugin.jni.JniJavah

object GenHeaders extends BleepScript("GenHeaders") {
  override def run(started: Started, commands: Commands, args: List[String]): Unit = {
    commands.compile(List(crosstermProject))

    val javah = new JniJavah(started.logger, started.projectPaths(crosstermProject), started.bloopProject(crosstermProject))
    val path = javah.javah()
    started.logger.withContext(path).warn("Generated")
  }
}
