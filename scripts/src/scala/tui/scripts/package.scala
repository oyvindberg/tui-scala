package tui

import bleep.model

package object scripts {
  val crosstermProject: model.CrossProjectName =
    model.CrossProjectName(model.ProjectName("crossterm"), None)
  val demoProject =
    model.CrossProjectName(model.ProjectName("demo"), crossId = Some(model.CrossId("jvm3")))

  // will publish these with dependencies
  def projectsToPublish(crossName: model.CrossProjectName): Boolean =
    crossName.name.value match {
      case "tui" => true
      case _     => false
    }

  val groupId = "com.olvind.tui"
}
