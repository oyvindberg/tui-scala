package tui.scripts

import bleep._
import bleep.plugin.jni.{Cargo, JniNative, JniPackage}

import java.nio.file.Path

object GenJniLibrary extends bleep.BleepCodegenScript("GenJniLibrary") {

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

  override def run(started: Started, commands: Commands, targets: List[GenJniLibrary.Target], args: List[String]): Unit = {
    val jniNative = crosstermJniNativeLib(started)
    val jniPackage = new JniPackage(started.buildPaths.buildDir, jniNative) {
      // override naming standard to match `NativeLoader.java`
      override lazy val managedNativeLibraries: Seq[(Path, RelPath)] = {
        val library: Path = jniNative.nativeCompile()
        val name = System.mapLibraryName(s"native-${jniNative.nativePlatform}-${jniNative.libName}")
        Seq(library -> new RelPath(List(name)))
      }
    }

    targets.foreach { target =>
      // copy into place in resources directories
      val writtenPaths = jniPackage.copyTo(target.resources)
      writtenPaths.foreach(path => started.logger.withContext(path).info("wrote"))
    }
  }
}
