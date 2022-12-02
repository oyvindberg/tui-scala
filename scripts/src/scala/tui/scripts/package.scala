package tui

import bleep.model

package object scripts {
  val crosstermProject: model.CrossProjectName =
    model.CrossProjectName(model.ProjectName("crossterm-jvm"), None)
  val demoProject =
    model.CrossProjectName(model.ProjectName("demo"), crossId = Some(model.CrossId("jvm3")))
}
