package tui.scripts

import bleep.model

object projectsToPublish {
  // will publish these with dependencies
  def include(crossName: model.CrossProjectName): Boolean =
    crossName.name.value match {
      case "tui" => true
      case _     => false
    }
}
