package jatatui.tests.core.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.layout.Constraint;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ConstraintTest {

  @Test
  public void from_lengths() {
    List<Constraint> cs = Constraint.fromLengths(1, 2, 3);
    assertEquals(
        List.of(new Constraint.Length(1), new Constraint.Length(2), new Constraint.Length(3)), cs);
  }

  @Test
  public void from_percentages() {
    List<Constraint> cs = Constraint.fromPercentages(10, 20, 30);
    assertEquals(
        List.of(
            new Constraint.Percentage(10),
            new Constraint.Percentage(20),
            new Constraint.Percentage(30)),
        cs);
  }

  @Test
  public void from_ratios() {
    List<Constraint> cs = Constraint.fromRatios(new int[][] {{1, 2}, {3, 4}});
    assertEquals(List.of(new Constraint.Ratio(1, 2), new Constraint.Ratio(3, 4)), cs);
  }

  @Test
  public void from_maxes() {
    List<Constraint> cs = Constraint.fromMaxes(5, 10);
    assertEquals(List.of(new Constraint.Max(5), new Constraint.Max(10)), cs);
  }

  @Test
  public void from_mins() {
    List<Constraint> cs = Constraint.fromMins(5, 10);
    assertEquals(List.of(new Constraint.Min(5), new Constraint.Min(10)), cs);
  }

  @Test
  public void from_fills() {
    List<Constraint> cs = Constraint.fromFills(1, 2);
    assertEquals(List.of(new Constraint.Fill(1), new Constraint.Fill(2)), cs);
  }

  @Test
  public void apply() {
    // Percentage caps at length
    assertEquals(0, new Constraint.Percentage(0).apply(100));
    assertEquals(50, new Constraint.Percentage(50).apply(100));
    assertEquals(100, new Constraint.Percentage(100).apply(100));
    assertEquals(100, new Constraint.Percentage(200).apply(100));

    // Ratio: 0/0 → 0, n/0 → length (no panic)
    assertEquals(0, new Constraint.Ratio(0, 0).apply(100));
    assertEquals(100, new Constraint.Ratio(1, 0).apply(100));
    assertEquals(0, new Constraint.Ratio(0, 1).apply(100));
    assertEquals(50, new Constraint.Ratio(1, 2).apply(100));
    assertEquals(100, new Constraint.Ratio(2, 2).apply(100));
    assertEquals(100, new Constraint.Ratio(3, 2).apply(100));

    assertEquals(0, new Constraint.Length(0).apply(100));
    assertEquals(50, new Constraint.Length(50).apply(100));
    assertEquals(100, new Constraint.Length(200).apply(100));

    assertEquals(0, new Constraint.Max(0).apply(100));
    assertEquals(50, new Constraint.Max(50).apply(100));
    assertEquals(100, new Constraint.Max(200).apply(100));

    assertEquals(100, new Constraint.Min(0).apply(100));
    assertEquals(100, new Constraint.Min(50).apply(100));
    assertEquals(200, new Constraint.Min(200).apply(100));

    // Fill behaves like Length per upstream apply()
    assertEquals(0, new Constraint.Fill(0).apply(100));
    assertEquals(50, new Constraint.Fill(50).apply(100));
    assertEquals(100, new Constraint.Fill(200).apply(100));
  }
}
