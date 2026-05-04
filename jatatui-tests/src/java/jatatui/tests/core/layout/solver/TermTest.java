package jatatui.tests.core.layout.solver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jatatui.core.layout.solver.Expression;
import jatatui.core.layout.solver.KasuariOps;
import jatatui.core.layout.solver.Term;
import jatatui.core.layout.solver.Variable;
import org.junit.jupiter.api.Test;

/// Java port of the inline `#[cfg(test)] mod tests` block at the bottom of
/// `submodules/kasuari/src/term.rs`. Skipped: f32 variants (Java has no separate `f32` overload),
/// and the `_assign` variants (KasuariOps does not expose mutating helpers — the non-mutating
/// equivalents already cover the semantics).
public final class TermTest {

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
  void new_() {
    Term t = new Term(LEFT, 2.0);
    assertEquals(LEFT, t.variable);
    assertEquals(2.0, t.coefficient, 0.0);
  }

  @Test
  void mul_f64() {
    assertEquals(new Term(LEFT, 2.0), KasuariOps.mul(leftTerm(), 2.0));
    assertEquals(new Term(LEFT, 2.0), KasuariOps.mul(2.0, leftTerm()));
  }

  @Test
  void div_f64() {
    assertEquals(new Term(LEFT, 0.5), KasuariOps.div(leftTerm(), 2.0));
  }

  @Test
  void add_f64() {
    assertExprEquals(new Expression(termList(leftTerm()), 2.0), KasuariOps.add(leftTerm(), 2.0));
    assertExprEquals(new Expression(termList(leftTerm()), 2.0), KasuariOps.add(2.0, leftTerm()));
  }

  @Test
  void add_term() {
    assertExprEquals(
        new Expression(termList(leftTerm(), rightTerm()), 0.0),
        KasuariOps.add(leftTerm(), rightTerm()));
  }

  @Test
  void add_expression() {
    assertExprEquals(
        new Expression(termList(rightTerm(), leftTerm()), 1.0),
        KasuariOps.add(leftTerm(), new Expression(termList(rightTerm()), 1.0)));
  }

  @Test
  void sub_f64() {
    assertExprEquals(new Expression(termList(leftTerm()), -2.0), KasuariOps.sub(leftTerm(), 2.0));
    assertExprEquals(
        new Expression(termList(new Term(LEFT, -1.0)), 2.0), KasuariOps.sub(2.0, leftTerm()));
  }

  @Test
  void sub_term() {
    assertExprEquals(
        new Expression(termList(leftTerm(), new Term(RIGHT, -1.0)), 0.0),
        KasuariOps.sub(leftTerm(), rightTerm()));
  }

  @Test
  void neg() {
    assertEquals(new Term(LEFT, -1.0), KasuariOps.negate(leftTerm()));
  }
}
