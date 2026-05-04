package jatatui.core.layout.solver;

public final class EditInfo {
  public final Tag tag;
  public final Constraint constraint;
  public double constant;

  public EditInfo(Tag tag, Constraint constraint, double constant) {
    this.tag = tag;
    this.constraint = constraint;
    this.constant = constant;
  }
}
