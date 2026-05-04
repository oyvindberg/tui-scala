package jatatui.examples.todolist;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Constraint;
import jatatui.core.layout.Layout;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import jatatui.core.style.palette.Tailwind;
import jatatui.core.symbols.Border;
import jatatui.core.terminal.Terminal;
import jatatui.core.text.Line;
import jatatui.crossterm.CrosstermBackend;
import jatatui.crossterm.Jatatui;
import jatatui.widgets.Borders;
import jatatui.widgets.block.Block;
import jatatui.widgets.block.Padding;
import jatatui.widgets.list.HighlightSpacing;
import jatatui.widgets.list.List;
import jatatui.widgets.list.ListItem;
import jatatui.widgets.list.ListState;
import jatatui.widgets.paragraph.Paragraph;
import jatatui.widgets.paragraph.Wrap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Event;
import tui.crossterm.KeyCode;
import tui.crossterm.KeyEvent;
import tui.crossterm.KeyEventKind;

/// A jatatui example that demonstrates how to create a todo list with selectable items.
///
/// Java port of `examples/apps/todo-list/src/main.rs` from ratatui v0.30.
public final class TodoListExample {

  private static final CrosstermJni JNI = new CrosstermJni();

  private static final Style TODO_HEADER_STYLE =
      Style.empty().withFg(Tailwind.SLATE.c100()).withBg(Tailwind.BLUE.c800());
  private static final Color NORMAL_ROW_BG = Tailwind.SLATE.c950();
  private static final Color ALT_ROW_BG_COLOR = Tailwind.SLATE.c900();
  private static final Style SELECTED_STYLE =
      Style.empty().withBg(Tailwind.SLATE.c800()).withAddModifier(Modifier.BOLD);
  private static final Color TEXT_FG_COLOR = Tailwind.SLATE.c200();
  private static final Color COMPLETED_TEXT_FG_COLOR = Tailwind.GREEN.c500();

  private TodoListExample() {}

  public static void main(String[] args) throws IOException {
    Jatatui.runIo(terminal -> new App().run(terminal));
  }

  enum Status {
    Todo,
    Completed
  }

  static final class TodoItem {
    Status status;
    final String todo;
    final String info;

    TodoItem(Status status, String todo, String info) {
      this.status = status;
      this.todo = todo;
      this.info = info;
    }

    static TodoItem of(Status status, String todo, String info) {
      return new TodoItem(status, todo, info);
    }

    ListItem toListItem() {
      Line line =
          switch (status) {
            case Todo -> Line.styled(" ☐ " + todo, Style.empty().withFg(TEXT_FG_COLOR));
            case Completed ->
                Line.styled(" ✓ " + todo, Style.empty().withFg(COMPLETED_TEXT_FG_COLOR));
          };
      return ListItem.of(line);
    }
  }

  static final class TodoList {
    final java.util.List<TodoItem> items;
    final ListState state = ListState.empty();

    TodoList(java.util.List<TodoItem> items) {
      this.items = items;
    }
  }

  static final class App {
    boolean shouldExit = false;
    final TodoList todoList =
        new TodoList(
            new ArrayList<>(
                java.util.List.of(
                    TodoItem.of(
                        Status.Todo,
                        "Rewrite everything with Rust!",
                        "I can't hold my inner voice. He tells me to rewrite the complete universe"
                            + " with Rust"),
                    TodoItem.of(
                        Status.Completed,
                        "Rewrite all of your tui apps with Ratatui",
                        "Yes, you heard that right. Go and replace your tui with Ratatui."),
                    TodoItem.of(
                        Status.Todo,
                        "Pet your cat",
                        "Minnak loves to be pet by you! Don't forget to pet and give some treats!"),
                    TodoItem.of(
                        Status.Todo, "Walk with your dog", "Max is bored, go walk with him!"),
                    TodoItem.of(Status.Completed, "Pay the bills", "Pay the train subscription!!!"),
                    TodoItem.of(
                        Status.Completed,
                        "Refactor list example",
                        "If you see this info that means I completed this task!"))));

    void run(Terminal<CrosstermBackend> terminal) throws IOException {
      while (!shouldExit) {
        terminal.draw(frame -> render(frame.area(), frame.bufferMut()));
        Event event = JNI.read();
        if (event instanceof Event.Key keyEvt && keyEvt.keyEvent().kind() == KeyEventKind.Press) {
          handleKey(keyEvt.keyEvent());
        }
      }
    }

    void handleKey(KeyEvent key) {
      KeyCode code = key.code();
      if (code instanceof KeyCode.Esc) {
        shouldExit = true;
      } else if (code instanceof KeyCode.Char ch) {
        switch (ch.c()) {
          case 'q' -> shouldExit = true;
          case 'h' -> selectNone();
          case 'j' -> selectNext();
          case 'k' -> selectPrevious();
          case 'g' -> selectFirst();
          case 'G' -> selectLast();
          case 'l' -> toggleStatus();
          default -> {
            // ignore other keys
          }
        }
      } else if (code instanceof KeyCode.Left) {
        selectNone();
      } else if (code instanceof KeyCode.Down) {
        selectNext();
      } else if (code instanceof KeyCode.Up) {
        selectPrevious();
      } else if (code instanceof KeyCode.Home) {
        selectFirst();
      } else if (code instanceof KeyCode.End) {
        selectLast();
      } else if (code instanceof KeyCode.Right) {
        toggleStatus();
      } else if (code instanceof KeyCode.Enter) {
        toggleStatus();
      }
    }

    void selectNone() {
      todoList.state.select(Optional.empty());
    }

    void selectNext() {
      todoList.state.selectNext();
    }

    void selectPrevious() {
      todoList.state.selectPrevious();
    }

    void selectFirst() {
      todoList.state.selectFirst();
    }

    void selectLast() {
      todoList.state.selectLast();
    }

    /// Changes the status of the selected list item.
    void toggleStatus() {
      Optional<Integer> selected = todoList.state.selected();
      if (selected.isEmpty()) return;
      int i = selected.get();
      TodoItem item = todoList.items.get(i);
      item.status =
          switch (item.status) {
            case Completed -> Status.Todo;
            case Todo -> Status.Completed;
          };
    }

    // ---- Render ----

    void render(Rect area, Buffer buf) {
      Layout mainLayout =
          Layout.vertical(
              new Constraint.Length(2), new Constraint.Fill(1), new Constraint.Length(1));
      Rect[] mainSplit = area.layout(mainLayout, 3);
      Rect headerArea = mainSplit[0];
      Rect contentArea = mainSplit[1];
      Rect footerArea = mainSplit[2];

      Layout contentLayout = Layout.vertical(new Constraint.Fill(1), new Constraint.Fill(1));
      Rect[] contentSplit = contentArea.layout(contentLayout, 2);
      Rect listArea = contentSplit[0];
      Rect itemArea = contentSplit[1];

      renderHeader(headerArea, buf);
      renderFooter(footerArea, buf);
      renderList(listArea, buf);
      renderSelectedItem(itemArea, buf);
    }

    static void renderHeader(Rect area, Buffer buf) {
      Paragraph.of("Ratatui Todo List Example").bold().centered().render(area, buf);
    }

    static void renderFooter(Rect area, Buffer buf) {
      Paragraph.of("Use ↓↑ to move, ← to unselect, → to change status, g/G to go top/bottom.")
          .centered()
          .render(area, buf);
    }

    void renderList(Rect area, Buffer buf) {
      Block block =
          Block.empty()
              .withTitle(Line.raw("TODO List").centered())
              .withBorders(Borders.TOP)
              .withBorderSet(Border.EMPTY)
              .withBorderStyle(TODO_HEADER_STYLE)
              .withStyle(Style.empty().withBg(NORMAL_ROW_BG));

      java.util.List<ListItem> items = new ArrayList<>(todoList.items.size());
      for (int i = 0; i < todoList.items.size(); i++) {
        Color color = alternateColors(i);
        ListItem li = todoList.items.get(i).toListItem().withStyle(Style.empty().withBg(color));
        items.add(li);
      }

      List list =
          List.of(items)
              .withBlock(block)
              .withHighlightStyle(SELECTED_STYLE)
              .withHighlightSymbol(">")
              .withHighlightSpacing(HighlightSpacing.Always);

      // StatefulWidget render path — pass the state explicitly.
      list.render(area, buf, todoList.state);
    }

    void renderSelectedItem(Rect area, Buffer buf) {
      String info;
      Optional<Integer> selected = todoList.state.selected();
      if (selected.isPresent()) {
        TodoItem item = todoList.items.get(selected.get());
        info =
            switch (item.status) {
              case Completed -> "✓ DONE: " + item.info;
              case Todo -> "☐ TODO: " + item.info;
            };
      } else {
        info = "Nothing selected...";
      }

      Block block =
          Block.empty()
              .withTitle(Line.raw("TODO Info").centered())
              .withBorders(Borders.TOP)
              .withBorderSet(Border.EMPTY)
              .withBorderStyle(TODO_HEADER_STYLE)
              .withStyle(Style.empty().withBg(NORMAL_ROW_BG))
              .withPadding(Padding.horizontal(1));

      Paragraph.of(info)
          .withBlock(block)
          .withStyle(Style.empty().withFg(TEXT_FG_COLOR))
          .withWrap(new Wrap(false))
          .render(area, buf);
    }

    static Color alternateColors(int i) {
      return i % 2 == 0 ? NORMAL_ROW_BG : ALT_ROW_BG_COLOR;
    }
  }
}
