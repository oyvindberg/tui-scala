package tui

import bleep.model
import bleep.model.CrossProjectName

package object scripts {
  val crosstermProject: model.CrossProjectName =
    model.CrossProjectName(model.ProjectName("crossterm"), None)
  val demoProject: CrossProjectName =
    model.CrossProjectName(model.ProjectName("jatatui-demo"), None)

  // will publish these with dependencies
  def projectsToPublish(crossName: model.CrossProjectName): Boolean =
    crossName.name.value match {
      case "crossterm"         => true
      case "jatatui-core"      => true
      case "jatatui-widgets"   => true
      case "jatatui-crossterm" => true
      case _                   => false
    }

  val groupId = "com.olvind.jatatui"
}
