package jatatui.examples.inline;

import jatatui.core.layout.Constraint;
import jatatui.core.layout.Layout;
import jatatui.core.layout.Rect;
import jatatui.core.style.Color;
import jatatui.core.style.Modifier;
import jatatui.core.style.Style;
import jatatui.core.terminal.Frame;
import jatatui.core.terminal.Terminal;
import jatatui.core.terminal.TerminalOptions;
import jatatui.core.terminal.Viewport;
import jatatui.core.text.Line;
import jatatui.core.text.Span;
import jatatui.crossterm.CrosstermBackend;
import jatatui.widgets.block.Block;
import jatatui.widgets.gauge.Gauge;
import jatatui.widgets.gauge.LineGauge;
import jatatui.widgets.list.List;
import jatatui.widgets.list.ListItem;
import jatatui.widgets.paragraph.Paragraph;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import tui.crossterm.CrosstermJni;
import tui.crossterm.Duration;
import tui.crossterm.Event;
import tui.crossterm.KeyCode;
import tui.crossterm.KeyEvent;

/// A jatatui example that demonstrates how to use the inlined viewport.
///
/// It shows a list of downloads in progress, with a progress bar for each download.
///
/// Mirrors `examples/apps/inline/src/main.rs` from ratatui v0.30.0.
public final class InlineExample {

  private static final CrosstermJni JNI = new CrosstermJni();
  private static final int NUM_DOWNLOADS = 10;
  /// Symbol for the bullet point in front of each in-progress download.
  private static final String DOT = "•";

  private InlineExample() {}

  /// All events handled by the main loop. Replaces upstream's `Event` enum (which clashes with
  /// `tui.crossterm.Event`, hence the longer name here).
  sealed interface AppEvent
      permits AppEvent.Input,
          AppEvent.Tick,
          AppEvent.Resize,
          AppEvent.DownloadUpdate,
          AppEvent.DownloadDone {
    record Input(KeyEvent key) implements AppEvent {}

    record Tick() implements AppEvent {}

    record Resize() implements AppEvent {}

    record DownloadUpdate(int workerId, int downloadId, double progress) implements AppEvent {}

    record DownloadDone(int workerId, int downloadId) implements AppEvent {}
  }

  record Download(int id, int size) {}

  static final class DownloadInProgress {
    final int id;
    final long startedAtNanos;
    double progress;

    DownloadInProgress(int id, long startedAtNanos) {
      this.id = id;
      this.startedAtNanos = startedAtNanos;
      this.progress = 0.0;
    }
  }

  static final class Downloads {
    final java.util.ArrayDeque<Download> pending;
    final TreeMap<Integer, DownloadInProgress> inProgress = new TreeMap<>();

    Downloads(java.util.ArrayDeque<Download> pending) {
      this.pending = pending;
    }

    Optional<Download> next(int workerId) {
      Download d = pending.pollFirst();
      if (d == null) return Optional.empty();
      inProgress.put(workerId, new DownloadInProgress(d.id(), System.nanoTime()));
      return Optional.of(d);
    }
  }

  static final class Worker {
    final int id;
    final BlockingQueue<Download> tx;

    Worker(int id, BlockingQueue<Download> tx) {
      this.id = id;
      this.tx = tx;
    }
  }

  public static void main(String[] args) throws IOException {
    JNI.enableRawMode();
    try {
      CrosstermBackend backend = new CrosstermBackend(JNI);
      try (Terminal<CrosstermBackend> terminal =
          Terminal.withOptions(backend, new TerminalOptions(Viewport.inline(8)))) {
        BlockingQueue<AppEvent> rx = new LinkedBlockingQueue<>();
        startInputHandling(rx);
        java.util.List<Worker> workers = startWorkers(rx);
        Downloads downloads = makeDownloads();

        for (Worker w : workers) {
          Optional<Download> d = downloads.next(w.id);
          d.ifPresent(w.tx::add);
        }

        run(terminal, workers, downloads, rx);
      }
    } finally {
      JNI.disableRawMode();
    }
  }

  private static void startInputHandling(BlockingQueue<AppEvent> tx) {
    Thread t =
        new Thread(
            () -> {
              long tickRateNanos = 200_000_000L;
              long lastTick = System.nanoTime();
              while (true) {
                long elapsed = System.nanoTime() - lastTick;
                long remaining = Math.max(0L, tickRateNanos - elapsed);
                Duration timeout =
                    new Duration(remaining / 1_000_000_000L, (int) (remaining % 1_000_000_000L));
                if (JNI.poll(timeout)) {
                  Event ev = JNI.read();
                  if (ev instanceof Event.Key keyEv) {
                    tx.add(new AppEvent.Input(keyEv.keyEvent()));
                  } else if (ev instanceof Event.Resize) {
                    tx.add(new AppEvent.Resize());
                  }
                }
                if (System.nanoTime() - lastTick >= tickRateNanos) {
                  tx.add(new AppEvent.Tick());
                  lastTick = System.nanoTime();
                }
              }
            },
            "inline-input-handler");
    t.setDaemon(true);
    t.start();
  }

  private static java.util.List<Worker> startWorkers(BlockingQueue<AppEvent> tx) {
    java.util.List<Worker> out = new ArrayList<>(4);
    for (int id = 0; id < 4; id++) {
      final int workerId = id;
      BlockingQueue<Download> workerRx = new ArrayBlockingQueue<>(64);
      Thread t =
          new Thread(
              () -> {
                try {
                  while (true) {
                    Download download = workerRx.take();
                    int remaining = download.size();
                    while (remaining > 0) {
                      long wait = Math.min(remaining, 10);
                      try {
                        Thread.sleep(wait * 10);
                      } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                      }
                      remaining = Math.max(0, remaining - 10);
                      int progress = (download.size() - remaining) * 100 / download.size();
                      tx.add(new AppEvent.DownloadUpdate(workerId, download.id(), progress));
                    }
                    tx.add(new AppEvent.DownloadDone(workerId, download.id()));
                  }
                } catch (InterruptedException ie) {
                  Thread.currentThread().interrupt();
                }
              },
              "inline-worker-" + id);
      t.setDaemon(true);
      t.start();
      out.add(new Worker(workerId, workerRx));
    }
    return out;
  }

  private static Downloads makeDownloads() {
    Random rng = new Random();
    java.util.ArrayDeque<Download> pending = new java.util.ArrayDeque<>(NUM_DOWNLOADS);
    for (int id = 0; id < NUM_DOWNLOADS; id++) {
      int size = rng.nextInt(1000); // [0, 1000)
      pending.add(new Download(id, size));
    }
    return new Downloads(pending);
  }

  private static void run(
      Terminal<CrosstermBackend> terminal,
      java.util.List<Worker> workers,
      Downloads downloads,
      BlockingQueue<AppEvent> rx)
      throws IOException {
    boolean redraw = true;
    while (true) {
      if (redraw) {
        terminal.draw(frame -> render(frame, downloads));
      }
      redraw = true;

      AppEvent ev;
      try {
        ev = rx.poll(60, TimeUnit.SECONDS);
        if (ev == null) {
          // No events for a long time — exit gracefully.
          return;
        }
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
        return;
      }

      if (ev instanceof AppEvent.Input input) {
        if (input.key().code() instanceof KeyCode.Char ch && ch.c() == 'q') {
          return;
        }
      } else if (ev instanceof AppEvent.Resize) {
        terminal.autoresize();
      } else if (ev instanceof AppEvent.Tick) {
        // tick — fall through to redraw
      } else if (ev instanceof AppEvent.DownloadUpdate update) {
        DownloadInProgress dl = downloads.inProgress.get(update.workerId());
        if (dl != null) {
          dl.progress = update.progress();
        }
        redraw = false;
      } else if (ev instanceof AppEvent.DownloadDone done) {
        DownloadInProgress dl = downloads.inProgress.remove(done.workerId());
        if (dl != null) {
          long elapsedMs = (System.nanoTime() - dl.startedAtNanos) / 1_000_000L;
          int finalDownloadId = done.downloadId();
          terminal.insertBefore(
              1,
              buf ->
                  Paragraph.of(
                          Line.from(
                              Span.from("Finished "),
                              Span.styled(
                                  "download " + finalDownloadId,
                                  Style.empty().withAddModifier(Modifier.BOLD)),
                              Span.from(" in " + elapsedMs + "ms")))
                      .render(buf.area, buf));
        }
        Optional<Download> nextDl = downloads.next(done.workerId());
        if (nextDl.isPresent()) {
          workers.get(done.workerId()).tx.add(nextDl.get());
        } else if (downloads.inProgress.isEmpty()) {
          terminal.insertBefore(1, buf -> Paragraph.of("Done !").render(buf.area, buf));
          return;
        }
      }
    }
  }

  private static void render(Frame frame, Downloads downloads) {
    Rect area = frame.area();
    Block block = Block.empty().withTitle(Line.from("Progress").centered());
    frame.renderWidget(block, area);

    Layout vertical =
        Layout.vertical(new Constraint.Length(2), new Constraint.Length(4)).withMargin(1);
    Layout horizontal =
        Layout.horizontal(new Constraint.Percentage(20), new Constraint.Percentage(80));
    Rect[] verticalRects = area.layout(vertical, 2);
    Rect progressArea = verticalRects[0];
    Rect main = verticalRects[1];
    Rect[] mainRects = main.layout(horizontal, 2);
    Rect listArea = mainRects[0];
    Rect gaugeArea = mainRects[1];

    // total progress
    int done = NUM_DOWNLOADS - downloads.pending.size() - downloads.inProgress.size();
    LineGauge progress =
        LineGauge.empty()
            .withFilledStyle(Style.empty().withFg(Color.BLUE))
            .withLabel(done + "/" + NUM_DOWNLOADS)
            .withRatio((double) done / (double) NUM_DOWNLOADS);
    frame.renderWidget(progress, progressArea);

    // in progress downloads
    java.util.ArrayList<ListItem> items = new ArrayList<>(downloads.inProgress.size());
    for (DownloadInProgress dl : downloads.inProgress.values()) {
      long elapsedMs = (System.nanoTime() - dl.startedAtNanos) / 1_000_000L;
      Line line =
          Line.from(
              Span.from(DOT),
              Span.styled(
                  String.format(" download %2d", dl.id),
                  Style.empty().withFg(Color.LIGHT_GREEN).withAddModifier(Modifier.BOLD)),
              Span.from(" (" + elapsedMs + "ms)"));
      items.add(ListItem.of(line));
    }
    List list = List.of(items);
    frame.renderWidget(list, listArea);

    int i = 0;
    for (DownloadInProgress dl : downloads.inProgress.values()) {
      Gauge gauge =
          Gauge.empty()
              .withGaugeStyle(Style.empty().withFg(Color.YELLOW))
              .withRatio(dl.progress / 100.0);
      int top = gaugeArea.top();
      int row = saturatingAdd(top, i);
      if (row > area.bottom()) {
        i += 1;
        continue;
      }
      frame.renderWidget(gauge, new Rect(gaugeArea.left(), row, gaugeArea.width(), 1));
      i += 1;
    }
  }

  private static int saturatingAdd(int a, int b) {
    long r = (long) a + (long) b;
    if (r > Integer.MAX_VALUE) return Integer.MAX_VALUE;
    if (r < 0) return 0;
    return (int) r;
  }
}
