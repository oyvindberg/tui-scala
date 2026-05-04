package jatatui.examples.demo2.tabs;

import static jatatui.examples.demo2.Theme.THEME;

import jatatui.core.buffer.Buffer;
import jatatui.core.layout.Constraint;
import jatatui.core.layout.HorizontalAlignment;
import jatatui.core.layout.Layout;
import jatatui.core.layout.Margin;
import jatatui.core.layout.Rect;
import jatatui.core.text.Line;
import jatatui.examples.demo2.Colors;
import jatatui.widgets.Borders;
import jatatui.widgets.Clear;
import jatatui.widgets.RatatuiMascot;
import jatatui.widgets.RatatuiMascot.MascotEyeColor;
import jatatui.widgets.block.Block;
import jatatui.widgets.block.Padding;
import jatatui.widgets.paragraph.Paragraph;
import jatatui.widgets.paragraph.Wrap;

/// Mirrors `apps/demo2/src/tabs/about.rs`.
///
/// The "About" tab. Shows the Ratatui mascot on the left and the crate description on the right,
/// over an Okhsv color swatch backdrop.
public final class AboutTab {

  /// Selected row in the (otherwise unused) about-tab list. Used only to toggle the mascot's eye
  /// color between default and red.
  private int rowIndex;

  private AboutTab() {
    this.rowIndex = 0;
  }

  /// Constructs a new [AboutTab] with default state.
  public static AboutTab defaultTab() {
    return new AboutTab();
  }

  /// Decrement the row index (saturating).
  public void prevRow() {
    if (rowIndex > 0) rowIndex--;
  }

  /// Increment the row index (saturating).
  public void nextRow() {
    if (rowIndex < Integer.MAX_VALUE) rowIndex++;
  }

  /// Render this tab into the given area of the buffer. Mirrors `impl Widget for AboutTab`.
  public void render(Rect area, Buffer buf) {
    new Colors.RgbSwatch().render(area, buf);
    Layout layout = Layout.horizontal(new Constraint.Length(34), new Constraint.Min(0));
    Rect[] split = area.layout(layout, 2);
    Rect logoArea = split[0];
    Rect description = split[1];
    renderCrateDescription(description, buf);
    MascotEyeColor eyeState = (rowIndex % 2 == 0) ? MascotEyeColor.Default : MascotEyeColor.Red;
    Rect mascotArea = logoArea.inner(new Margin(2, 0));
    RatatuiMascot.newMascot().withEye(eyeState).render(mascotArea, buf);
  }

  private static void renderCrateDescription(Rect outerArea, Buffer buf) {
    Rect area = outerArea.inner(new Margin(2, 4));
    Clear.instance().render(area, buf);
    Block.empty().withStyle(THEME.content).render(area, buf);
    Rect inner = area.inner(new Margin(2, 1));
    String text =
        "- cooking up terminal user interfaces -\n"
            + "\n"
            + "    Ratatui is a Rust crate that provides widgets (e.g. Paragraph, Table) and"
            + " draws them to the screen efficiently every frame.";
    Paragraph.of(text)
        .withStyle(THEME.description)
        .withBlock(
            Block.empty()
                .withTitle(Line.from(" Ratatui "))
                .withTitleAlignment(HorizontalAlignment.Center)
                .withBorders(Borders.TOP)
                .withBorderStyle(THEME.descriptionTitle)
                .withPadding(new Padding(0, 0, 0, 0)))
        .withWrap(new Wrap(true))
        .render(inner, buf);
  }
}
