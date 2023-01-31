package tui.scripts

import bleep._
import bleep.plugin.jni.{Cargo, JniNative, JniPackage}

object GenJniLibrary extends BleepScript("GenJniLibrary") {

  def crosstermJniNativeLib(started: Started): JniNative =
    new JniNative(
      logger = started.logger,
      nativeCompileSourceDirectory = started.projectPaths(crosstermProject).dir / "cargo",
      nativeTargetDirectory = started.buildPaths.dotBleepDir,
      nativeBuildTool = new Cargo(release = true),
      libName = "crossterm",
      env = sys.env.toList
    ) {
      override lazy val nativePlatform: String =
        OsArch.current match {
          case OsArch.LinuxAmd64    => "x86_64-linux"
          case OsArch.WindowsAmd64  => "x86_64-windows"
          case OsArch.MacosAmd64    => "x86_64-darwin"
          case OsArch.MacosArm64(_) => "arm64-darwin"
          case other: OsArch.Other  => sys.error(s"not implemented: $other")
        }
    }

  override def run(started: Started, commands: Commands, args: List[String]): Unit = {
    val jniNative = crosstermJniNativeLib(started)
    val jniPackage = new JniPackage(started.buildPaths.buildDir, jniNative)

    // copy into place in resources directories
    val writtenPaths = jniPackage.copyTo(started.projectPaths(crosstermProject).resourcesDirs.generated)

    writtenPaths.foreach(path => started.logger.withContext(path).info("wrote"))

  }
}
