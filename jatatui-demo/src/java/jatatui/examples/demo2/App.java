package jatatui.examples.demo2;

import static jatatui.examples.demo2.Theme.THEME;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Constraint;
import jatatui.core.layout.Layout;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.style.Style;
import jatatui.core.terminal.Frame;
import jatatui.core.terminal.Terminal;
import jatatui.core.text.Line;
import jatatui.core.text.Span;
import jatatui.crossterm.CrosstermBackend;
import jatatui.examples.demo2.tabs.AboutTab;
import jatatui.examples.demo2.tabs.EmailTab;
import jatatui.examples.demo2.tabs.RecipeTab;
import jatatui.examples.demo2.tabs.TracerouteTab;
import jatatui.examples.demo2.tabs.WeatherTab;
import jatatui.widgets.block.Block;
import jatatui.widgets.paragraph.Paragraph;
import jatatui.widgets.tabs.Tabs;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Duration;
import tui.crossterm.Event;
import tui.crossterm.KeyCode;
import tui.crossterm.KeyEventKind;

/// Mirrors `apps/demo2/src/app.rs`.
///
/// Mutable state holding the current mode, currently selected tab, and the per-tab state objects.
public final class App {

  /// Application running mode.
  public enum Mode {
    Running,
    Destroy,
    Quit;

    public static Mode defaultMode() {
      return Running;
    }
  }

  /// Tabs in display order. The ordinal corresponds to the upstream `FromRepr` index.
  public enum Tab {
    About,
    Recipe,
    Email,
    Traceroute,
    Weather;

    public static Tab defaultTab() {
      return About;
    }

    public Tab next() {
      int currentIndex = ordinal();
      int nextIndex = currentIndex + 1;
      Tab[] all = values();
      if (nextIndex >= all.length) return this;
      return all[nextIndex];
    }

    public Tab prev() {
      int currentIndex = ordinal();
      int prevIndex = currentIndex - 1;
      if (prevIndex < 0) return this;
      return values()[prevIndex];
    }

    /// Title rendered in the top-right tabs strip. The `About` tab gets an empty title (mirrors
    /// upstream, where the logo doubles as the tab title).
    public String title() {
      return switch (this) {
        case About -> "";
        default -> " " + name() + " ";
      };
    }
  }

  private Mode mode;
  private Tab tab;
  private final AboutTab aboutTab;
  private final RecipeTab recipeTab;
  private final EmailTab emailTab;
  private final TracerouteTab tracerouteTab;
  private final WeatherTab weatherTab;

  /// Crossterm JNI handle for event polling.
  private final CrosstermJni jni;

  /// Frame counter used by destroy-mode animation. Incremented each draw.
  private int frameCount;

  private App(CrosstermJni jni) {
    this.mode = Mode.defaultMode();
    this.tab = Tab.defaultTab();
    this.aboutTab = AboutTab.defaultTab();
    this.recipeTab = RecipeTab.defaultTab();
    this.emailTab = EmailTab.defaultTab();
    this.tracerouteTab = TracerouteTab.defaultTab();
    this.weatherTab = WeatherTab.defaultTab();
    this.jni = jni;
    this.frameCount = 0;
  }

  /// Construct a new App with default state.
  public static App create(CrosstermJni jni) {
    return new App(jni);
  }

  /// Run the app until the user quits. Mirrors `App::run`.
  public void run(Terminal<CrosstermBackend> terminal) throws IOException {
    while (isRunning()) {
      terminal.draw(this::render);
      handleEvents();
    }
  }

  private boolean isRunning() {
    return mode != Mode.Quit;
  }

  /// Render a single frame.
  private void render(Frame frame) {
    Rect area = frame.area();
    Buffer buf = frame.bufferMut();
    renderApp(area, buf);
    if (mode == Mode.Destroy) {
      Destroy.destroy(frame, frameCount);
    }
    frameCount++;
  }

  /// Handle events from the terminal. Polls with a 1/50s timeout to mirror VHS's default frame
  /// rate.
  private void handleEvents() throws IOException {
    // 1/50 of a second to mirror VHS's default 50fps frame rate.
    Duration timeout = new Duration(0L, 20_000_000);
    if (!jni.poll(timeout)) {
      return;
    }
    Event ev = jni.read();
    if (!(ev instanceof Event.Key keyEv)) {
      return;
    }
    if (keyEv.keyEvent().kind() != KeyEventKind.Press) {
      return;
    }
    KeyCode code = keyEv.keyEvent().code();
    if (code instanceof KeyCode.Char ch) {
      switch (ch.c()) {
        case 'q' -> mode = Mode.Quit;
        case 'h' -> prevTab();
        case 'l' -> nextTab();
        case 'k' -> prev();
        case 'j' -> next();
        case 'd' -> destroy();
        default -> {
          // ignore
        }
      }
    } else if (code instanceof KeyCode.Esc) {
      mode = Mode.Quit;
    } else if (code instanceof KeyCode.Left) {
      prevTab();
    } else if (code instanceof KeyCode.Right) {
      nextTab();
    } else if (code instanceof KeyCode.Tab) {
      nextTab();
    } else if (code instanceof KeyCode.Up) {
      prev();
    } else if (code instanceof KeyCode.Down) {
      next();
    } else if (code instanceof KeyCode.Delete) {
      destroy();
    }
    // else: ignore
  }

  private void prev() {
    switch (tab) {
      case About -> aboutTab.prevRow();
      case Recipe -> recipeTab.prev();
      case Email -> emailTab.prev();
      case Traceroute -> tracerouteTab.prevRow();
      case Weather -> weatherTab.prev();
    }
  }

  private void next() {
    switch (tab) {
      case About -> aboutTab.nextRow();
      case Recipe -> recipeTab.next();
      case Email -> emailTab.next();
      case Traceroute -> tracerouteTab.nextRow();
      case Weather -> weatherTab.next();
    }
  }

  private void prevTab() {
    tab = tab.prev();
  }

  private void nextTab() {
    tab = tab.next();
  }

  private void destroy() {
    mode = Mode.Destroy;
  }

  // ---- Rendering ----

  /// Renders the application chrome (title bar + selected tab + bottom bar) into the given area.
  /// Mirrors `impl Widget for &App`.
  private void renderApp(Rect area, Buffer buf) {
    Layout layout =
        Layout.vertical(
            new Constraint.Length(1), new Constraint.Min(0), new Constraint.Length(1));
    Rect[] split = area.layout(layout, 3);
    Rect titleBar = split[0];
    Rect tabArea = split[1];
    Rect bottomBar = split[2];

    Block.empty().withStyle(THEME.root).render(area, buf);
    renderTitleBar(titleBar, buf);
    renderSelectedTab(tabArea, buf);
    renderBottomBar(bottomBar, buf);
  }

  private void renderTitleBar(Rect area, Buffer buf) {
    Layout layout = Layout.horizontal(new Constraint.Min(0), new Constraint.Length(43));
    Rect[] split = area.layout(layout, 2);
    Rect title = split[0];
    Rect tabsArea = split[1];

    buf.setSpan(title.x(), title.y(), Span.styled("Ratatui", THEME.appTitle), title.width());

    List<Line> titles = new ArrayList<>();
    for (Tab t : Tab.values()) {
      titles.add(Line.from(t.title()));
    }
    Tabs.of(titles)
        .withStyle(THEME.tabs)
        .withHighlightStyle(THEME.tabsSelected)
        .withSelected(tab.ordinal())
        .withDivider("")
        .withPadding("", "")
        .render(tabsArea, buf);
  }

  private void renderSelectedTab(Rect area, Buffer buf) {
    switch (tab) {
      case About -> aboutTab.render(area, buf);
      case Recipe -> recipeTab.render(area, buf);
      case Email -> emailTab.render(area, buf);
      case Traceroute -> tracerouteTab.render(area, buf);
      case Weather -> weatherTab.render(area, buf);
    }
  }

  private static void renderBottomBar(Rect area, Buffer buf) {
    String[][] keys = new String[][] {
      {"H/←", "Left"},
      {"L/→", "Right"},
      {"K/↑", "Up"},
      {"J/↓", "Down"},
      {"D/Del", "Destroy"},
      {"Q/Esc", "Quit"},
    };
    List<Span> spans = new ArrayList<>(keys.length * 2);
    for (String[] entry : keys) {
      spans.add(Span.styled(" " + entry[0] + " ", THEME.keyBinding.key()));
      spans.add(Span.styled(" " + entry[1] + " ", THEME.keyBinding.description()));
    }
    Line line =
        Line.fromSpans(spans)
            .centered()
            .withStyle(Style.empty().withFg(new Color.Indexed(236)).withBg(new Color.Indexed(232)));
    Paragraph.of(line)
        .withStyle(Style.empty().withFg(new Color.Indexed(236)).withBg(new Color.Indexed(232)))
        .centered()
        .render(area, buf);
  }

  /// Returns the current frame count (used by tests / debugging; mirrors `Frame::count` upstream).
  public int frameCount() {
    return frameCount;
  }
}
