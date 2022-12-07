package tui.scripts

import bleep.*
import bleep.model.{CrossProjectName, Project}
import bleep.plugin.jni.{Cargo, JniJavah, JniNative, JniPackage}

import java.nio.file.Files
import scala.collection.immutable

object GenJniLibrary extends BleepScript("GenJniLibrary") {

  def crosstermJniNativeLib(started: Started): JniNative =
    new JniNative(
      logger = started.logger,
      nativeCompileSourceDirectory = started.projectPaths(crosstermProject).dir / "cargo",
      nativeTargetDirectory = started.buildPaths.dotBleepDir,
      nativeBuildTool = new CargoFixed(release = true),
      libName = "crossterm",
      env = sys.env.toList
    )

  override def run(started: Started, commands: Commands, args: List[String]): Unit = {

    val jniNative = crosstermJniNativeLib(started)
    val jniPackage = new JniPackage(started.buildPaths.buildDir, jniNative)

    // copy into place in resources directories
    val writtenPaths = jniPackage.copyTo(started.projectPaths(crosstermProject).resourcesDirs.generated)

    writtenPaths.foreach(path => started.logger.withContext(path).info("wrote"))

  }
}
