package jatatui.tests.core.terminal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jatatui.core.backend.TestBackend;
import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Rect;
import jatatui.core.style.Style;
import jatatui.core.terminal.CompletedFrame;
import jatatui.core.terminal.Terminal;
import jatatui.core.terminal.TerminalOptions;
import jatatui.core.terminal.Viewport;
import jatatui.core.widgets.Widget;
import jatatui.tests._support.BufferAssertions;
import java.io.IOException;
import org.junit.jupiter.api.Test;

/// Tests for [Terminal]. Ports the integration tests in
/// `submodules/ratatui/ratatui/tests/terminal.rs`.
///
/// Notes on skipped variants:
/// - `terminal_insert_before_moves_viewport_does_not_clobber`,
///   `terminal_insert_before_scrolls_on_large_input_does_not_clobber`,
///   `terminal_insert_before_scrolls_on_many_inserts_does_not_clobber` and
///   `terminal_insert_before_large_viewport_does_not_clobber` are gated upstream on the
///   `scrolling-regions` Cargo feature. The Java port uses the no-scrolling-regions
///   `insert_before` implementation only, mirroring upstream's default-feature build, so these
///   variants are N/A here.
public class TerminalTest {

  @Test
  public void swap_buffer_clears_prev_buffer() throws IOException {
    Terminal<TestBackend> terminal = Terminal.create(new TestBackend(100, 50));
    terminal.currentBufferMut().setString(0, 0, "Hello", Style.reset());
    assertEquals("H", terminal.currentBufferMut().content()[0].symbol());
    terminal.swapBuffers();
    assertEquals(" ", terminal.currentBufferMut().content()[0].symbol());
  }

  @Test
  public void terminal_draw_returns_the_completed_frame() throws IOException {
    Terminal<TestBackend> terminal = Terminal.create(new TestBackend(10, 10));
    Widget paragraphTest = (area, buf) -> Widget.renderString("Test", area, buf);
    CompletedFrame frame = terminal.draw(f -> f.renderWidget(paragraphTest, f.area()));
    assertEquals("T", frame.buffer().cellAt(0, 0).symbol());
    assertEquals(new Rect(0, 0, 10, 10), frame.area());

    terminal.backend().resize(8, 8);
    Widget paragraphLowerTest = (area, buf) -> Widget.renderString("test", area, buf);
    CompletedFrame frame2 = terminal.draw(f -> f.renderWidget(paragraphLowerTest, f.area()));
    assertEquals("t", frame2.buffer().cellAt(0, 0).symbol());
    assertEquals(new Rect(0, 0, 8, 8), frame2.area());
  }

  @Test
  public void terminal_draw_increments_frame_count() throws IOException {
    Terminal<TestBackend> terminal = Terminal.create(new TestBackend(10, 10));
    Widget renderTest = (area, buf) -> Widget.renderString("Test", area, buf);

    CompletedFrame frame0 =
        terminal.draw(
            f -> {
              assertEquals(0, f.count());
              f.renderWidget(renderTest, f.area());
            });
    assertEquals(0, frame0.count());

    CompletedFrame frame1 =
        terminal.draw(
            f -> {
              assertEquals(1, f.count());
              f.renderWidget(renderTest, f.area());
            });
    assertEquals(1, frame1.count());

    CompletedFrame frame2 =
        terminal.draw(
            f -> {
              assertEquals(2, f.count());
              f.renderWidget(renderTest, f.area());
            });
    assertEquals(2, frame2.count());
  }

  @Test
  public void terminal_insert_before_moves_viewport() throws IOException {
    // 5-line terminal, single-line inline viewport. Insert 2 lines (less than `5 - 1 = 4`):
    // viewport should move down to accommodate the new lines.
    Terminal<TestBackend> terminal =
        Terminal.withOptions(new TestBackend(20, 5), new TerminalOptions(Viewport.inline(1)));

    terminal.insertBefore(
        2, buf -> renderLines(buf, "------ Line 1 ------", "------ Line 2 ------"));

    terminal.draw(
        f ->
            f.renderWidget(
                (area, buffer) -> Widget.renderString("[---- Viewport ----]", area, buffer),
                f.area()));

    BufferAssertions.assertBufferEq(
        terminal.backend().buffer(),
        Buffer.withLines(
            "------ Line 1 ------",
            "------ Line 2 ------",
            "[---- Viewport ----]",
            "                    ",
            "                    "));
    assertEquals(0, terminal.backend().scrollback().area().height());
  }

  @Test
  public void terminal_insert_before_scrolls_on_large_input() throws IOException {
    // Insert more lines than `terminal height - viewport height`: viewport moves to the bottom and
    // lines above scroll.
    Terminal<TestBackend> terminal =
        Terminal.withOptions(new TestBackend(20, 5), new TerminalOptions(Viewport.inline(1)));

    terminal.insertBefore(
        5,
        buf ->
            renderLines(
                buf,
                "------ Line 1 ------",
                "------ Line 2 ------",
                "------ Line 3 ------",
                "------ Line 4 ------",
                "------ Line 5 ------"));

    terminal.draw(
        f ->
            f.renderWidget(
                (area, buffer) -> Widget.renderString("[---- Viewport ----]", area, buffer),
                f.area()));

    BufferAssertions.assertBufferEq(
        terminal.backend().buffer(),
        Buffer.withLines(
            "------ Line 2 ------",
            "------ Line 3 ------",
            "------ Line 4 ------",
            "------ Line 5 ------",
            "[---- Viewport ----]"));
    BufferAssertions.assertBufferEq(
        terminal.backend().scrollback(), Buffer.withLines("------ Line 1 ------"));
  }

  @Test
  public void terminal_insert_before_scrolls_on_many_inserts() throws IOException {
    // Multiple small insertions (each less than `terminal height - viewport height`) should give
    // the same end state as one large insertion.
    Terminal<TestBackend> terminal =
        Terminal.withOptions(new TestBackend(20, 5), new TerminalOptions(Viewport.inline(1)));

    terminal.insertBefore(1, buf -> renderLines(buf, "------ Line 1 ------"));
    terminal.insertBefore(1, buf -> renderLines(buf, "------ Line 2 ------"));
    terminal.insertBefore(1, buf -> renderLines(buf, "------ Line 3 ------"));
    terminal.insertBefore(1, buf -> renderLines(buf, "------ Line 4 ------"));
    terminal.insertBefore(1, buf -> renderLines(buf, "------ Line 5 ------"));

    terminal.draw(
        f ->
            f.renderWidget(
                (area, buffer) -> Widget.renderString("[---- Viewport ----]", area, buffer),
                f.area()));

    BufferAssertions.assertBufferEq(
        terminal.backend().buffer(),
        Buffer.withLines(
            "------ Line 2 ------",
            "------ Line 3 ------",
            "------ Line 4 ------",
            "------ Line 5 ------",
            "[---- Viewport ----]"));
    BufferAssertions.assertBufferEq(
        terminal.backend().scrollback(), Buffer.withLines("------ Line 1 ------"));
  }

  @Test
  public void terminal_insert_before_large_viewport() throws IOException {
    // Regression test: doing an insert_before when the viewport covered the entire screen used to
    // panic.
    Terminal<TestBackend> terminal =
        Terminal.withOptions(new TestBackend(20, 3), new TerminalOptions(Viewport.inline(3)));

    terminal.insertBefore(1, buf -> renderLines(buf, "------ Line 1 ------"));
    terminal.insertBefore(
        3,
        buf ->
            renderLines(
                buf, "------ Line 2 ------", "------ Line 3 ------", "------ Line 4 ------"));
    terminal.insertBefore(
        7,
        buf ->
            renderLines(
                buf,
                "------ Line 5 ------",
                "------ Line 6 ------",
                "------ Line 7 ------",
                "------ Line 8 ------",
                "------ Line 9 ------",
                "----- Line 10 ------",
                "----- Line 11 ------"));

    // No bordered "Viewport" widget available, so render plain lines instead — the test focuses on
    // the scrollback contents and the fact that the viewport area is preserved at the bottom.
    terminal.draw(
        f ->
            f.renderWidget(
                (area, buffer) -> {
                  buffer.setString(area.x(), area.y(), "Top viewport line   ", Style.empty());
                  buffer.setString(area.x(), area.y() + 1, "     Viewport       ", Style.empty());
                  buffer.setString(area.x(), area.y() + 2, "Bot viewport line   ", Style.empty());
                },
                f.area()));

    BufferAssertions.assertBufferEq(
        terminal.backend().buffer(),
        Buffer.withLines("Top viewport line   ", "     Viewport       ", "Bot viewport line   "));

    BufferAssertions.assertBufferEq(
        terminal.backend().scrollback(),
        Buffer.withLines(
            "------ Line 1 ------",
            "------ Line 2 ------",
            "------ Line 3 ------",
            "------ Line 4 ------",
            "------ Line 5 ------",
            "------ Line 6 ------",
            "------ Line 7 ------",
            "------ Line 8 ------",
            "------ Line 9 ------",
            "----- Line 10 ------",
            "----- Line 11 ------"));
  }

  // --- Helpers ---

  /// Renders `lines` to the given buffer, one per row, starting at row 0.
  private static void renderLines(Buffer buf, String... lines) {
    for (int i = 0; i < lines.length; i++) {
      buf.setString(0, i, lines[i], Style.empty());
    }
  }
}
