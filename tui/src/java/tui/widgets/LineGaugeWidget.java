package tui.widgets;

import java.util.Optional;
import tui.Buffer;
import tui.Position;
import tui.Rect;
import tui.Spans;
import tui.Style;
import tui.Symbols;
import tui.Widget;
import tui.internal.Ranges;
import tui.internal.Saturating;

/// A compact widget to display a task progress over a single line.
public final class LineGaugeWidget implements Widget {
  public final Optional<BlockWidget> block;
  public final GaugeWidget.Ratio ratio;
  public final Optional<Spans> label;
  public final Symbols.line.Set lineSet;
  public final Style style;
  public final Style gaugeStyle;

  public LineGaugeWidget(
      Optional<BlockWidget> block,
      GaugeWidget.Ratio ratio,
      Optional<Spans> label,
      Symbols.line.Set lineSet,
      Style style,
      Style gaugeStyle) {
    this.block = block;
    this.ratio = ratio;
    this.label = label;
    this.lineSet = lineSet;
    this.style = style;
    this.gaugeStyle = gaugeStyle;
  }

  public static LineGaugeWidget empty() {
    return new LineGaugeWidget(
        Optional.empty(),
        GaugeWidget.Ratio.Zero,
        Optional.empty(),
        Symbols.line.NORMAL,
        Style.DEFAULT,
        Style.DEFAULT);
  }

  public LineGaugeWidget withBlock(BlockWidget b) {
    return new LineGaugeWidget(Optional.of(b), ratio, label, lineSet, style, gaugeStyle);
  }

  public LineGaugeWidget withRatio(GaugeWidget.Ratio r) {
    return new LineGaugeWidget(block, r, label, lineSet, style, gaugeStyle);
  }

  public LineGaugeWidget withLabel(Spans l) {
    return new LineGaugeWidget(block, ratio, Optional.of(l), lineSet, style, gaugeStyle);
  }

  public LineGaugeWidget withLineSet(Symbols.line.Set s) {
    return new LineGaugeWidget(block, ratio, label, s, style, gaugeStyle);
  }

  public LineGaugeWidget withStyle(Style s) {
    return new LineGaugeWidget(block, ratio, label, lineSet, s, gaugeStyle);
  }

  public LineGaugeWidget withGaugeStyle(Style s) {
    return new LineGaugeWidget(block, ratio, label, lineSet, style, s);
  }

  @Override
  public void render(Rect area, Buffer buf) {
    buf.setStyle(area, style);
    Rect gaugeArea;
    if (block.isPresent()) {
      BlockWidget b = block.get();
      Rect innerArea = b.inner(area);
      b.render(area, buf);
      gaugeArea = innerArea;
    } else {
      gaugeArea = area;
    }

    if (gaugeArea.height() < 1) {
      return;
    }

    Spans labelVal = label.orElseGet(() -> Spans.nostyle((int) (ratio.value() * 100.0) + "%"));
    Position pos = buf.setSpans(gaugeArea.left(), gaugeArea.top(), labelVal, gaugeArea.width());
    int col = pos.x();
    int row = pos.y();
    int start = col + 1;
    if (start >= gaugeArea.right()) {
      return;
    }

    int end =
        start
            + (int)
                Math.floor(
                    (double) Saturating.saturatingSubUnsigned(gaugeArea.right(), start)
                        * ratio.value());
    Ranges.range(
        start,
        end,
        c -> buf.get(c, row)
            .setSymbol(lineSet.horizontal())
            .setStyle(
                new Style(
                    gaugeStyle.fg(),
                    Optional.empty(),
                    gaugeStyle.addModifier(),
                    gaugeStyle.subModifier())));
    Ranges.range(
        end,
        gaugeArea.right(),
        c -> buf.get(c, row)
            .setSymbol(lineSet.horizontal())
            .setStyle(
                new Style(
                    gaugeStyle.bg(),
                    Optional.empty(),
                    gaugeStyle.addModifier(),
                    gaugeStyle.subModifier())));
  }
}
