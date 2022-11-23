package tui.scripts

import bleep.*
import bleep.model.{CrossProjectName, Project}
import bleep.plugin.jni.JniJavah

import scala.collection.immutable

object GenHeaders extends BleepScript("GenHeaders") {
  override def run(started: Started, commands: Commands, args: List[String]): Unit = {
    commands.compile(List(crosstermProject))

    val javah = new JniJavah(started.logger, started.projectPaths(crosstermProject), started.bloopProjects(crosstermProject))
    val path = javah.javah()
    started.logger.withContext(path).warn("Generated")
  }
}
