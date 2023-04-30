package tui.scripts

import bleep._
import bleep.plugin.nativeimage.NativeImagePlugin

import java.nio.file.Path

object GenNativeImage extends BleepScript("GenNativeImage") {
  def run(started: Started, commands: Commands, args: List[String]): Unit = {
    commands.compile(List(demoProject))

    val plugin = new NativeImagePlugin(
      project = started.bloopProject(demoProject),
      logger = started.logger,
      nativeImageOptions = List(
        "--verbose",
        "--no-fallback",
        "-H:+ReportExceptionStackTraces",
        "--initialize-at-build-time=scala.runtime.Statics$VM",
        "--initialize-at-build-time=scala.Symbol",
        "--initialize-at-build-time=scala.Symbol$",
        "--native-image-info",
        """-H:IncludeResources=libnative-arm64-darwin-crossterm.dylib""",
        """-H:IncludeResources=libnative-x86_64-darwin-crossterm.dylib""",
        """-H:IncludeResources=libnative-x86_64-linux-crossterm.so""",
        """-H:IncludeResources=native-x86_64-windows-crossterm.dll""",
        "-H:-UseServiceLoaderFeature"
      ),
      jvmCommand = started.jvmCommand,
      env = sys.env.toList ++ List(("USE_NATIVE_IMAGE_JAVA_PLATFORM_MODULE_SYSTEM", "false"))
    ) {
      // allow user to pass in name of generated binary as parameter
      override val nativeImageOutput: Path = args.headOption match {
        case Some(relPath) =>
          // smoothen over some irritation from github action scripts
          val relPathNoExe = if (relPath.endsWith(".exe")) relPath.dropRight(".exe".length) else relPath
          started.pre.buildPaths.cwd / relPathNoExe
        case None => super.nativeImageOutput
      }
    }
    val path = plugin.nativeImage()
    started.logger.info(s"Created native-image at $path")
  }
}
