package jatatui.components.sparkline;

import jatatui.core.style.Style;
import jatatui.react.Element;
import java.util.List;
import java.util.Optional;

/// Static factories for the React-style sparkline component.
///
/// Mirrors the convention of [jatatui.react.Components] — import statically:
/// `import static jatatui.components.sparkline.Components.*;`
///
/// All factories return an [Element] backed by a `pureComponent`, so callers get
/// React.memo-style memoization for free as long as they pass equal (ideally identical)
/// [SparklineProps] across renders.
public final class Components {
  private Components() {}

  /// Build a sparkline from explicit props.
  public static Element sparkline(SparklineProps props) {
    return Sparkline.sparkline(props);
  }

  /// Convenience: title + data, no fixed max, default style.
  public static Element sparkline(String title, List<Long> data) {
    return Sparkline.sparkline(new SparklineProps(title, data, Optional.empty(), Style.empty()));
  }

  /// Convenience: title + data + fixed max, default style.
  public static Element sparkline(String title, List<Long> data, long max) {
    return Sparkline.sparkline(new SparklineProps(title, data, Optional.of(max), Style.empty()));
  }

  /// Convenience: title + data + style, no fixed max.
  public static Element sparkline(String title, List<Long> data, Style style) {
    return Sparkline.sparkline(new SparklineProps(title, data, Optional.empty(), style));
  }
}
