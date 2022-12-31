package tui.scripts

import bleep._
import bleep.plugin.nativeimage.NativeImagePlugin

import java.nio.file.{Files, Path, StandardCopyOption}

object GenNativeImage extends BleepScript("GenNativeImage") {
  def run(started: Started, commands: Commands, args: List[String]): Unit = {
    commands.compile(List(demoProject))

    val jvmCommand =
      FetchJvm(
        maybeCacheDir = Some(started.userPaths.resolveJvmCacheDir),
        cacheLogger = new BleepCacheLogger(started.logger),
        jvm = model.Jvm("graalvm-java17:22.3.0", None),
        ec = started.executionContext
      )

    val jniLibraryPath = GenJniLibrary.crosstermJniNativeLib(started).nativeCompile()

    val newJniLibraryPath = started.projectPaths(demoProject).resourcesDirs.generated / jniLibraryPath.getFileName.toString
    Files.createDirectories(newJniLibraryPath.getParent)
    Files.copy(jniLibraryPath, newJniLibraryPath, StandardCopyOption.REPLACE_EXISTING)
    started.logger.info(s"workaround for https://github.com/oracle/graal/issues/5219 : copy $jniLibraryPath to $newJniLibraryPath")

    val plugin = new NativeImagePlugin(
      project = started.bloopProjects(demoProject),
      logger = started.logger,
      nativeImageOptions = List(
        "--verbose",
        "--no-fallback",
        "-H:+ReportExceptionStackTraces",
        "--initialize-at-build-time=scala.runtime.Statics$VM",
        "--initialize-at-build-time=scala.Symbol",
        "--initialize-at-build-time=scala.Symbol$",
        "--native-image-info",
        """-H:IncludeResources=libcrossterm.dylib""",
        "-H:-UseServiceLoaderFeature"
      ),
      jvmCommand = jvmCommand,
      env = sys.env.toList ++ List(("USE_NATIVE_IMAGE_JAVA_PLATFORM_MODULE_SYSTEM", "false"))
    ) {
      // allow user to pass in name of generated binary as parameter
      override val nativeImageOutput: Path = args.headOption match {
        case Some(relPath) =>
          // smoothen over some irritation from github action scripts
          val relPathNoExe = if (relPath.endsWith(".exe")) relPath.dropRight(".exe".length) else relPath
          started.prebootstrapped.buildPaths.cwd / relPathNoExe
        case None => super.nativeImageOutput
      }
    }
    val path = plugin.nativeImage()
    started.logger.info(s"Created native-image at $path")
  }
}
