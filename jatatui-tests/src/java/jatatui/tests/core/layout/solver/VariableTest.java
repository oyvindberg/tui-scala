package jatatui.tests.core.layout.solver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jatatui.core.layout.solver.Expression;
import jatatui.core.layout.solver.KasuariOps;
import jatatui.core.layout.solver.Term;
import jatatui.core.layout.solver.Variable;
import org.junit.jupiter.api.Test;

/// Java port of the inline `#[cfg(test)] mod tests` block at the bottom of
/// `submodules/kasuari/src/variable.rs`. Skipped: f32 variants (no separate Java overload),
/// the `_assign` variants, and `variable_default` (the Rust `Default` trait does not apply —
/// Java exposes `Variable.create()` instead, which is exercised by every Solver test).
public final class VariableTest {

  private static final Variable LEFT = Variable.force(0);
  private static final Variable RIGHT = Variable.force(1);

  private static Term leftTerm() {
    return new Term(LEFT, 1.0);
  }

  private static Term rightTerm() {
    return new Term(RIGHT, 1.0);
  }

  private static List<Term> termList(Term... ts) {
    return new ArrayList<>(Arrays.asList(ts));
  }

  private static void assertExprEquals(Expression expected, Expression actual) {
    assertEquals(expected.constant, actual.constant, 0.0);
    assertEquals(expected.terms, actual.terms);
  }

  @Test
  void variable_unique() {
    assertNotEquals(LEFT, RIGHT);
  }

  @Test
  void variable_add_f64() {
    assertExprEquals(new Expression(termList(leftTerm()), 5.0), KasuariOps.add(LEFT, 5.0));
    assertExprEquals(new Expression(termList(leftTerm()), 5.0), KasuariOps.add(5.0, LEFT));
  }

  @Test
  void variable_add_variable() {
    assertExprEquals(
        new Expression(termList(leftTerm(), rightTerm()), 0.0), KasuariOps.add(LEFT, RIGHT));
  }

  @Test
  void variable_add_term() {
    assertExprEquals(
        new Expression(termList(leftTerm(), rightTerm()), 0.0), KasuariOps.add(LEFT, rightTerm()));
    assertExprEquals(
        new Expression(termList(leftTerm(), rightTerm()), 0.0), KasuariOps.add(leftTerm(), RIGHT));
  }

  @Test
  void variable_sub_f64() {
    assertExprEquals(new Expression(termList(leftTerm()), -5.0), KasuariOps.sub(LEFT, 5.0));
    assertExprEquals(
        new Expression(termList(new Term(LEFT, -1.0)), 5.0), KasuariOps.sub(5.0, LEFT));
  }

  @Test
  void variable_sub_variable() {
    assertExprEquals(
        new Expression(termList(leftTerm(), new Term(RIGHT, -1.0)), 0.0),
        KasuariOps.sub(LEFT, RIGHT));
  }

  @Test
  void variable_mul_f64() {
    assertEquals(new Term(LEFT, 5.0), KasuariOps.mul(LEFT, 5.0));
    assertEquals(new Term(LEFT, 5.0), KasuariOps.mul(5.0, LEFT));
  }

  @Test
  void variable_div_f64() {
    assertEquals(new Term(LEFT, 1.0 / 5.0), KasuariOps.div(LEFT, 5.0));
  }

  @Test
  void variable_neg() {
    assertEquals(new Term(LEFT, -1.0), KasuariOps.negate(LEFT));
  }
}
