package jatatui.react.examples.barchart;

import static jatatui.components.barchart.BarCharts.horizontalBarChart;
import static jatatui.components.barchart.BarCharts.verticalBarChart;
import static jatatui.react.Components.column;
import static jatatui.react.Components.component;
import static jatatui.react.Components.fill;
import static jatatui.react.Components.length;
import static jatatui.react.Components.row;
import static jatatui.react.Components.text;

import jatatui.components.barchart.BarEntry;
import jatatui.core.style.Color;
import jatatui.core.style.Style;
import jatatui.react.Element;
import jatatui.react.ReactApp;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import tui.crossterm.KeyCode;

/// React-style demo of the [jatatui.components.barchart.BarChartComponent].
///
/// Two bar charts side-by-side over the same dataset (one vertical, one horizontal). Press
/// number keys 1-7 to highlight the matching day (Mon..Sun) — selecting a new day produces a
/// fresh `List<BarEntry>` and triggers a real re-render. Pressing the same number again
/// clears the highlight.
public final class BarChartExample {

  private static final List<String> LABELS =
      List.of("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");
  private static final List<Long> VALUES = List.of(8L, 3L, 7L, 4L, 9L, 6L, 2L);

  public static void main(String[] args) throws IOException {
    ReactApp.run(app());
  }

  static Element app() {
    return component(
        ctx -> {
          var highlighted = ctx.useState(() -> Optional.<Integer>empty());

          for (int i = 0; i < LABELS.size(); i++) {
            int day = i;
            char ch = (char) ('1' + i);
            ctx.onKey(
                new KeyCode.Char(ch),
                () ->
                    highlighted.update(
                        cur ->
                            cur.isPresent() && cur.get() == day
                                ? Optional.empty()
                                : Optional.of(day)));
          }

          Optional<Integer> currentHighlight = highlighted.get();
          List<BarEntry> entries = buildEntries(currentHighlight);

          Style barStyle = Style.empty().withFg(Color.CYAN);
          String hint =
              currentHighlight.isPresent()
                  ? " Highlighting: "
                      + LABELS.get(currentHighlight.get())
                      + " (1-7 to change, Esc to quit) "
                  : " Press 1-7 to highlight a day, Esc to quit ";

          return column(
                  length(1, text(hint, Style.empty().withFg(Color.GRAY))),
                  fill(
                      1,
                      row(
                          fill(1, verticalBarChart(" Vertical ", entries, barStyle)),
                          fill(1, horizontalBarChart(" Horizontal ", entries, barStyle)))))
              .withSpacing(1)
              .withMargin(new jatatui.core.layout.Margin(2, 1));
        });
  }

  /// Build an immutable `List<BarEntry>` for the given (optional) highlighted day. Each call
  /// produces a fresh list — the component memoizes on `props.equals(...)`, which short-circuits
  /// when the highlight is unchanged but recomputes when it changes.
  static List<BarEntry> buildEntries(Optional<Integer> highlighted) {
    Style highlightStyle = Style.empty().withFg(Color.YELLOW).withBg(Color.BLUE);
    List<BarEntry> out = new ArrayList<>(LABELS.size());
    for (int i = 0; i < LABELS.size(); i++) {
      String label = LABELS.get(i);
      long value = VALUES.get(i);
      if (highlighted.isPresent() && highlighted.get() == i) {
        out.add(BarEntry.of(label, value, highlightStyle));
      } else {
        out.add(BarEntry.of(label, value));
      }
    }
    return List.copyOf(out);
  }
}
