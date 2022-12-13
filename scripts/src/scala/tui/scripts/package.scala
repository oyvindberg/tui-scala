package tui

import bleep.model
import bleep.model.CrossProjectName

package object scripts {
  val crosstermProject: model.CrossProjectName =
    model.CrossProjectName(model.ProjectName("crossterm"), None)
  val demoProject: CrossProjectName =
    model.CrossProjectName(model.ProjectName("demo"), crossId = Some(model.CrossId("jvm213")))

  // will publish these with dependencies
  def projectsToPublish(crossName: model.CrossProjectName): Boolean =
    crossName.name.value match {
      case "tui" => true
      case _     => false
    }

  val groupId = "com.olvind.tui"
}
