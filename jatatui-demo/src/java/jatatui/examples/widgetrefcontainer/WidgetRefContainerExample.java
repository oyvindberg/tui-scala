package jatatui.examples.widgetrefcontainer;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Constraint;
import jatatui.core.layout.Direction;
import jatatui.core.layout.Layout;
import jatatui.core.layout.Rect;
import jatatui.core.terminal.Frame;
import jatatui.core.terminal.Terminal;
import jatatui.core.widgets.Widget;
import jatatui.crossterm.CrosstermBackend;
import jatatui.crossterm.Jatatui;
import jatatui.widgets.block.Block;
import jatatui.widgets.paragraph.Paragraph;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Event;
import tui.crossterm.KeyEventKind;

/// An example of how to store heterogeneous widgets in a container and render them by reference.
///
/// This example creates a `StackContainer` widget that can hold any number of widgets of
/// different types. It creates two widgets, [Greeting] and [Farewell], and stores them in a
/// `StackContainer` with a vertical layout. The `StackContainer` widget renders each of its
/// child widgets in the order they were added.
///
/// Java port of `examples/apps/widget-ref-container/src/main.rs` from ratatui v0.30.
///
/// Upstream relies on the unstable `WidgetRef` trait to box different widget types under one
/// abstraction. In jatatui every [Widget] is a regular interface with a `render` method, so the
/// container simply holds `List<Widget>` directly and calls `render` on each entry.
public final class WidgetRefContainerExample {

  private static final CrosstermJni JNI = new CrosstermJni();

  private WidgetRefContainerExample() {}

  public static void main(String[] args) throws IOException {
    Jatatui.runIo(WidgetRefContainerExample::run);
  }

  private static void run(Terminal<CrosstermBackend> terminal) throws IOException {
    while (true) {
      terminal.draw(WidgetRefContainerExample::render);
      Event event = JNI.read();
      if (isKeyPress(event)) {
        return;
      }
    }
  }

  private static boolean isKeyPress(Event event) {
    if (event instanceof Event.Key key) {
      return key.keyEvent().kind() == KeyEventKind.Press;
    }
    return false;
  }

  private static void render(Frame frame) {
    List<StackContainer.Entry> entries = new ArrayList<>();
    entries.add(new StackContainer.Entry(new Greeting(), new Constraint.Percentage(50)));
    entries.add(new StackContainer.Entry(new Farewell(), new Constraint.Percentage(50)));
    StackContainer container = new StackContainer(Direction.Vertical, entries);
    frame.renderWidget(container, frame.area());
  }

  /// A simple widget that prints the word "Hello" inside a bordered block.
  static final class Greeting implements Widget {
    @Override
    public void render(Rect area, Buffer buf) {
      Paragraph.of("Hello").withBlock(Block.bordered()).render(area, buf);
    }
  }

  /// A simple widget that prints the word "Goodbye" inside a bordered block.
  static final class Farewell implements Widget {
    @Override
    public void render(Rect area, Buffer buf) {
      Paragraph.of("Goodbye").withBlock(Block.bordered()).render(area, buf);
    }
  }

  /// A container widget that lays out its child widgets in the given [Direction], allocating each
  /// child according to its [Constraint]. Mirrors upstream's `StackContainer` struct.
  static final class StackContainer implements Widget {

    /// A child widget paired with the layout constraint allocated for it.
    record Entry(Widget widget, Constraint constraint) {}

    private final Direction direction;
    private final List<Entry> entries;

    StackContainer(Direction direction, List<Entry> entries) {
      this.direction = direction;
      this.entries = List.copyOf(entries);
    }

    @Override
    public void render(Rect area, Buffer buf) {
      List<Constraint> constraints = new ArrayList<>(entries.size());
      for (Entry e : entries) {
        constraints.add(e.constraint());
      }
      Rect[] areas = Layout.of(direction, constraints).split(area);
      for (int i = 0; i < entries.size() && i < areas.length; i++) {
        entries.get(i).widget().render(areas[i], buf);
      }
    }
  }
}
