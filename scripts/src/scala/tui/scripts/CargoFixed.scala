package tui.scripts

import bleep.plugin.jni.BuildTool

import bleep.internal.FileUtils
import bleep.logging.Logger
import bleep.{cli, PathOps, RelPath}

import java.nio.file.{Files, Path}
import scala.jdk.CollectionConverters._

class CargoFixed(protected val release: Boolean = true) extends BuildTool {

  def name: String = "Cargo"

  def ensureHasBuildFile(sourceDirectory: Path, logger: Logger, libName: String): Unit = {
    val buildScript = sourceDirectory / "Cargo.toml"
    if (FileUtils.exists(buildScript)) ()
    else {
      logger.withContext(buildScript).info(s"Initialized empty build script for $name")
      Files.createDirectories(buildScript.getParent)
      Files.writeString(buildScript, template(libName))
    }
  }

  def template(libName: String) =
    s"""[package]
       |name = "${libName}"
       |version = "0.1.0"
       |authors = ["John Doe <john.doe@gmail.com>"]
       |edition = "2018"
       |
       |[dependencies]
       |jni = "0.19"
       |
       |[lib]
       |crate_type = ["cdylib"]
       |""".stripMargin

  override def getInstance(baseDirectory: Path, buildDirectory: Path, logger: Logger, env: List[(String, String)]): Instance =
    new Instance(baseDirectory, logger, env)

  class Instance(protected val baseDirectory: Path, protected val logger: Logger, env: List[(String, String)]) extends BuildTool.Instance {
    val cliOut = cli.Out.ViaLogger(logger)

    def clean(): Unit =
      cli("cargo clean", baseDirectory, List("cargo", "clean"), logger = logger, out = cliOut, env = env)

    def library(targetDirectory: Path): Path = {
      cli(
        "cargo build",
        baseDirectory,
        List[Option[String]](
          Some("cargo"),
          Some("build"),
          if (release) Some("--release") else None,
          Some("--target-dir"),
          Some(targetDirectory.toString)
        ).flatten,
        logger = logger,
        out = cliOut,
        env = env
      )

      val subdir = if (release) "release" else "debug"
      val products: List[Path] =
        Files
          .list(targetDirectory.resolve(subdir))
          .filter(Files.isRegularFile(_))
          .filter { p =>
            val fileName = p.getFileName.toString
            fileName.endsWith(".so") || fileName.endsWith(".dylib") || fileName.endsWith(".dll")
          }
          .iterator()
          .asScala
          .toList

      // only one produced library is expected
      products match {
        case Nil =>
          sys.error(
            s"No files were created during compilation, " +
              s"something went wrong with the $name configuration."
          )
        case head :: Nil =>
          head
        case more @ (picked :: _) =>
          val foundBinaries = more.map(path => RelPath.relativeTo(targetDirectory, path).asString)
          logger
            .withContext(foundBinaries)
            .withContext(picked)
            .warn(s"More than one built library was found under $targetDirectory. Only the first one will be used.")
          picked
      }
    }
  }
}
